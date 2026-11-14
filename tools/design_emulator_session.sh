#!/usr/bin/env bash
# Tasarım görüntü oturumu — TEK flock -o çağrısı içinde çalıştırılır:
#   flock -o /home/ahmete/.qwen-orch/build.lock bash tools/design_emulator_session.sh
# Gerçek cihaz bağlıysa onu kullanır; değilse emülatörü başlatır, iş bitince kapatır.
set -u
cd "$(dirname "$0")/.."
PKG=com.basitce.gfx
APK=app/build/outputs/apk/debug/app-debug.apk
mkdir -p .qwen/tmp design/after

REAL=$(adb devices | awk 'NR>1 && $2=="device" && $1 !~ /^emulator-/ {print $1; exit}')
if [ -n "$REAL" ]; then
  echo "REAL DEVICE: $REAL"
  export ANDROID_SERIAL="$REAL"
else
  EMU="$HOME/Android/Sdk/emulator/emulator"
  PORT=5560
  export ANDROID_SERIAL="emulator-$PORT"
  "$EMU" -avd Medium_Phone -port "$PORT" -no-window -no-audio -no-boot-anim \
    -gpu swiftshader_indirect -no-snapshot >.qwen/tmp/emulator.log 2>&1 &
  EMU_PID=$!
  cleanup() {
    adb -s "emulator-$PORT" emu kill >/dev/null 2>&1
    for _ in $(seq 1 30); do kill -0 "$EMU_PID" 2>/dev/null || break; sleep 1; done
    kill -9 "$EMU_PID" 2>/dev/null
  }
  trap cleanup EXIT TERM INT

  adb wait-for-device
  BOOTED=0
  for _ in $(seq 1 150); do
    if [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; then
      BOOTED=1; break
    fi
    sleep 2
  done
  if [ "$BOOTED" != "1" ]; then
    echo "EMULATOR BOOT FAIL"
    tail -20 .qwen/tmp/emulator.log
    exit 1
  fi
  echo "EMULATOR BOOTED"
fi

[ -f "$APK" ] || { echo "APK YOK: $APK"; exit 1; }
adb install -r -g "$APK" || { echo "INSTALL FAIL"; exit 1; }

# Açılış ekranı: siyah kareleri atlayan doğrulamalı yakalama (tools/design_splash.py)
python3 tools/design_splash.py "$PKG" design/after/00-splash.png \
  && echo "SHOT 00-splash" || echo "SHOT FAIL 00-splash"
sleep 3
adb shell am force-stop "$PKG"

bash tools/design_shots.sh "$APK"
SHOTS_RC=$?
echo "SHOTS_RC=$SHOTS_RC"

# Çökme / ANR taraması (grep'in kendi çıkış kodu esas; boru hattı değil)
adb logcat -d > .qwen/tmp/emu-logcat.txt 2>/dev/null
CRASH_RC=0
if grep -E "FATAL EXCEPTION|ANR in $PKG" .qwen/tmp/emu-logcat.txt > .qwen/tmp/crash-hits.txt; then
  head -10 .qwen/tmp/crash-hits.txt
  echo "CRASH_FOUND"
  CRASH_RC=1
else
  echo "CRASH_NONE"
fi
echo "EMU_SESSION_DONE"
[ "$SHOTS_RC" -eq 0 ] && [ "$CRASH_RC" -eq 0 ] || exit 1
