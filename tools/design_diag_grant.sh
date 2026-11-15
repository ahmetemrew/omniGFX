#!/usr/bin/env bash
# Grant akışı teşhisi — TEK flock -o çağrısı içinde çalıştırılır:
#   flock -o /home/ahmete/.qwen-orch/build.lock bash tools/design_diag_grant.sh
# Dolu oturumun "kurulum ekranı açılmadı" hatasını kök nedenine kadar izler:
# her adımda dump'ın TÜM metinlerini basar, Shizuku logcat'ını toplar.
set -u
cd "$(dirname "$0")/.."
PKG=com.basitce.gfx
APK=app/build/outputs/apk/debug/app-debug.apk
SHIZUKU_APK=.qwen/tmp/shizuku.apk
SHIZUKU_PKG=moe.shizuku.privileged.api
EMU="$HOME/Android/Sdk/emulator/emulator"
PORT=5560
export ANDROID_SERIAL="emulator-$PORT"
mkdir -p .qwen/tmp

# Kardeş oturumların emülatörü kapanana dek bekle (en çok 15 dk): aynı AVD'de
# iki qemu (özellikle read-write + read-only) disk kilidi yarışı yaratır.
for _ in $(seq 1 450); do
  [ -z "$(adb devices | awk 'NR>1 && $1 ~ /^emulator-/ && $2=="device" {print $1}')" ] && break
  sleep 2
done

"$EMU" -avd Medium_Phone -port "$PORT" -no-window -no-audio -no-boot-anim \
  -gpu swiftshader_indirect -no-snapshot >.qwen/tmp/emulator-diag.log 2>&1 &
EMU_PID=$!
cleanup() {
  adb -s "emulator-$PORT" emu kill >/dev/null 2>&1
  for _ in $(seq 1 30); do kill -0 "${EMU_PID:-}" 2>/dev/null || break; sleep 1; done
  kill -9 "${EMU_PID:-}" 2>/dev/null
}
trap cleanup EXIT TERM INT

adb wait-for-device
for _ in $(seq 1 150); do
  [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ] && break
  sleep 2
done
echo "BOOTED"
adb shell wm size reset >/dev/null 2>&1
adb shell wm density reset >/dev/null 2>&1
adb shell settings put system font_scale 1.0
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
adb shell cmd uimode night yes

adb install -r -g "$SHIZUKU_APK" >/dev/null || { echo "SHIZUKU INSTALL FAIL"; exit 1; }
adb shell monkey -p "$SHIZUKU_PKG" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
sleep 8
printf '#!/system/bin/sh\npm_path=$(pm path %s) && apk_path=${pm_path#package:} && $(dirname $apk_path)/lib/*/libshizuku.so\n' "$SHIZUKU_PKG" > .qwen/tmp/shizuku-start.sh
adb push .qwen/tmp/shizuku-start.sh /data/local/tmp/omni-start.sh >/dev/null 2>&1
adb shell "nohup sh /data/local/tmp/omni-start.sh >/data/local/tmp/omni-shizuku.log 2>&1 &"
for _ in $(seq 1 30); do
  adb shell ps -A 2>/dev/null | grep -qE 'shizuku_server|app_process' && break
  sleep 2
done
echo "SERVER: $(adb shell ps -A | grep -cE 'shizuku_server|app_process') süreç"

adb install -r -g "$APK" >/dev/null || { echo "APK INSTALL FAIL"; exit 1; }
adb shell pm clear "$PKG" >/dev/null 2>&1
adb shell mkdir -p /sdcard/Download
adb push tools/design_demo_config.ini /sdcard/Download/UserCustom.ini >/dev/null 2>&1

texts() { # dump'ın tüm metinlerini bas
  adb shell uiautomator dump /sdcard/omni_ui.xml >/dev/null 2>&1
  adb pull /sdcard/omni_ui.xml .qwen/tmp/diag-ui.xml >/dev/null 2>&1
  python3 - .qwen/tmp/diag-ui.xml <<'PY'
import re, sys
xml = open(sys.argv[1], encoding='utf-8', errors='replace').read()
ts = [m.group(1) for m in re.finditer(r'text="([^"]+)"', xml)]
print("EKRAN:", " | ".join(dict.fromkeys(ts))[:600])
PY
}
tapxy() { # needle -> en küçük düğüm merkezi
  python3 - .qwen/tmp/diag-ui.xml "$1" <<'PY'
import re, sys
xml = open(sys.argv[1], encoding='utf-8', errors='replace').read()
best = None
for m in re.finditer(r'<node[^>]*>', xml):
    s = m.group(0)
    t = re.search(r'text="([^"]*)"', s)
    d = re.search(r'content-desc="([^"]*)"', s)
    hit = (t and sys.argv[2] in t.group(1)) or (d and sys.argv[2] in d.group(1))
    if not hit: continue
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', s)
    if not b: continue
    x1, y1, x2, y2 = map(int, b.groups())
    a = (x2-x1)*(y2-y1)
    if a > 0 and (best is None or a < best[0]): best = (a, (x1+x2)//2, (y1+y2)//2)
if best: print(best[1], best[2])
PY
}

adb shell am start -n "$PKG/.MainActivity" >/dev/null
sleep 8
echo "── Ana ekran:"; texts
XY=$(tapxy "Ayarlar"); echo "TAP Ayarlar $XY"; adb shell input tap $XY; sleep 2
echo "── Ayarlar:"; texts
XY=$(tapxy "Dosya erişimi için gerekli"); echo "TAP satır $XY"; adb shell input tap $XY
sleep 3
echo "── Satır sonrası 3s:"; texts
sleep 10
echo "── Satır sonrası 13s:"; texts
sleep 20
echo "── Satır sonrası 33s:"; texts
echo "── Shizuku logcat:"
adb logcat -d -s ShizukuStateManager ShizukuSetupViewModel Shizuku 2>/dev/null | tail -25
echo "── OmniGFX logcat (son 30):"
adb logcat -d 2>/dev/null | grep -iE 'basitce|shizuku' | tail -30
echo "DIAG_DONE"