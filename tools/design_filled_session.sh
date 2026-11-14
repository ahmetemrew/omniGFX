#!/usr/bin/env bash
# Dolu durum görüntü oturumu — TEK flock -o çağrısı içinde çalıştırılır:
#   flock -o /home/ahmete/.qwen-orch/build.lock bash tools/design_filled_session.sh
# Emülatörü başlatır, Shizuku'yu ADB modunda çalıştırır, demo config seed eder,
# dolu durum görüntülerini alır ve emülatörü kapatır.
set -u
cd "$(dirname "$0")/.."
PKG=com.basitce.gfx
APK=app/build/outputs/apk/debug/app-debug.apk
SHIZUKU_APK=.qwen/tmp/shizuku.apk
SHIZUKU_PKG=moe.shizuku.privileged.api
mkdir -p .qwen/tmp design/after

[ -f "$APK" ] || { echo "APK YOK: $APK"; exit 1; }
[ -f "$SHIZUKU_APK" ] || { echo "SHIZUKU APK YOK: $SHIZUKU_APK"; exit 1; }

REAL=$(adb devices | awk 'NR>1 && $2=="device" && $1 !~ /^emulator-/ {print $1; exit}')
if [ -n "$REAL" ]; then
  echo "REAL DEVICE: $REAL"
  export ANDROID_SERIAL="$REAL"
else
  EMU="$HOME/Android/Sdk/emulator/emulator"
  PORT=5560
  export ANDROID_SERIAL="emulator-$PORT"
  # Kardeş oturumların emülatörü kapanana dek bekle (en çok 15 dk): iki AVD
  # aynı anda açılırsa RAM baskısı LMK ile iki oturumun uygulamasını da öldürür.
  for _ in $(seq 1 450); do
    [ -z "$(adb devices | awk 'NR>1 && $1 ~ /^emulator-/ && $2=="device" {print $1}')" ] && break
    sleep 2
  done
  "$EMU" -avd Medium_Phone -port "$PORT" -no-window -no-audio -no-boot-anim \
    -gpu swiftshader_indirect -no-snapshot >.qwen/tmp/emulator-filled.log 2>&1 &
  EMU_PID=$!
  cleanup() {
    adb -s "emulator-$PORT" emu kill >/dev/null 2>&1
    for _ in $(seq 1 30); do kill -0 "${EMU_PID:-}" 2>/dev/null || break; sleep 1; done
    kill -9 "${EMU_PID:-}" 2>/dev/null
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
    tail -20 .qwen/tmp/emulator-filled.log
    exit 1
  fi
  echo "EMULATOR BOOTED"
fi

# ── Shizuku kur + servisi ADB modunda başlat ──
adb install -r -g "$SHIZUKU_APK" || { echo "SHIZUKU INSTALL FAIL"; exit 1; }
adb shell monkey -p "$SHIZUKU_PKG" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
sleep 8
START_SH="/sdcard/Android/data/$SHIZUKU_PKG/start.sh"
if ! adb shell "test -f '$START_SH'" >/dev/null 2>&1; then
  # v13.6'de resmi başlatma yolu native libshizuku.so'yu doğrudan çalıştırmaktır
  # (sürüm notlarındaki komutun birebir eşleniği). Eski app_process+CLASSPATH yolu
  # librish.so'yu bulamayıp UnsatisfiedLinkError ile çöküyor.
  echo "start.sh cihazda yok; libshizuku.so başlatma betiği üretiliyor"
  printf '#!/system/bin/sh\npm_path=$(pm path %s) && apk_path=${pm_path#package:} && $(dirname $apk_path)/lib/*/libshizuku.so\n' "$SHIZUKU_PKG" > .qwen/tmp/shizuku-start.sh
  adb push .qwen/tmp/shizuku-start.sh /data/local/tmp/omni-start.sh >/dev/null 2>&1
  START_SH="/data/local/tmp/omni-start.sh"
fi
adb shell "nohup sh '$START_SH' >/data/local/tmp/omni-shizuku.log 2>&1 &"
SERVER_UP=0
for _ in $(seq 1 30); do
  if adb shell ps -A 2>/dev/null | grep -qE 'shizuku_server|app_process'; then SERVER_UP=1; break; fi
  sleep 2
done
if [ "$SERVER_UP" != "1" ]; then
  echo "SHIZUKU SERVER FAIL"
  adb shell cat /data/local/tmp/omni-shizuku.log 2>/dev/null | tail -20
  exit 1
fi
echo "SHIZUKU SERVER UP"

# ── OmniGFX kur + demo config seed et ──
adb install -r -g "$APK" || { echo "INSTALL FAIL"; exit 1; }
# Önceki koşudan kalma oyun/profil verisi boş durum görüntülerini bozmasın.
adb shell pm clear "$PKG" >/dev/null 2>&1
adb shell mkdir -p /sdcard/Download
adb push tools/design_demo_config.ini /sdcard/Download/UserCustom.ini >/dev/null 2>&1 \
  || { echo "SEED FAIL"; exit 1; }

bash tools/design_shots_filled.sh
SHOTS_RC=$?
echo "FILLED_SHOTS_RC=$SHOTS_RC"

adb logcat -d > .qwen/tmp/emu-logcat-filled.txt 2>/dev/null
CRASH_RC=0
if grep -E "FATAL EXCEPTION|ANR in $PKG" .qwen/tmp/emu-logcat-filled.txt > .qwen/tmp/crash-hits-filled.txt; then
  head -10 .qwen/tmp/crash-hits-filled.txt
  echo "CRASH_FOUND"
  CRASH_RC=1
else
  echo "CRASH_NONE"
fi
echo "FILLED_SESSION_DONE"
[ "$SHOTS_RC" -eq 0 ] && [ "$CRASH_RC" -eq 0 ] || exit 1
