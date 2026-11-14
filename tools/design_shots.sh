#!/usr/bin/env bash
# OmniGFX tasarım turu görüntü betiği.
# KULLANIM: tek flock -o çağrısı içinde, emülatör/cihaz zaten açıkken:
#   bash tools/design_shots.sh [APK]
# Gezinti metin tabanlıdır (uiautomator dump); ezber koordinat YOK.
set -u
PKG=com.basitce.gfx
APK="${1:-app/build/outputs/apk/debug/app-debug.apk}"
OUT=design/after
mkdir -p "$OUT"
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT

adb wait-for-device
for _ in $(seq 1 90); do
  [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ] && break
  sleep 2
done

# Paylaşılan AVD'de kardeş oturumların bırakabileceği ekran override'ını sıfırla.
# (Gezinme modu zorlanmaz: kalibre görüntüler AVD varsayılanıyla alındı; zorlama
# üst bar dokunuşlarını kaydırabiliyor.)
adb shell wm size reset >/dev/null 2>&1
adb shell wm density reset >/dev/null 2>&1
sleep 3

adb install -r -g "$APK" || { echo "INSTALL FAIL"; exit 1; }
adb shell pm clear "$PKG" >/dev/null
# Deterministik görüntüler + azaltılmış hareket yolunun testi:
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0

DUMPS=.qwen/tmp/ui-dumps
mkdir -p "$DUMPS"
shot() { # $1 = dosya adı (uzantısız); görüntü + sayısal hizalama kanıtı (UI dump)
  adb shell screencap -p /sdcard/omni_shot.png
  adb pull /sdcard/omni_shot.png "$OUT/$1.png" >/dev/null 2>&1 \
    && echo "SHOT $1" || echo "SHOT FAIL $1"
  adb shell uiautomator dump /sdcard/omni_ui.xml >/dev/null 2>&1
  adb pull /sdcard/omni_ui.xml "$DUMPS/$1.xml" >/dev/null 2>&1
}

tap() { # $1 = text/content-desc alt dizesi; en küçük eşleşen düğüme dokunur
  local xy attempt
  for attempt in 1 2; do
  adb shell uiautomator dump /sdcard/omni_ui.xml >/dev/null 2>&1
  adb pull /sdcard/omni_ui.xml "$TMP/ui.xml" >/dev/null 2>&1
  xy=$(python3 - "$TMP/ui.xml" "$1" <<'PY'
import re, sys
xml = open(sys.argv[1], encoding='utf-8', errors='replace').read()
needle = sys.argv[2]
best = None
for m in re.finditer(r'<node[^>]*>', xml):
    s = m.group(0)
    t = re.search(r'text="([^"]*)"', s)
    d = re.search(r'content-desc="([^"]*)"', s)
    hit = (t and needle in t.group(1)) or (d and needle in d.group(1))
    if not hit:
        continue
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', s)
    if not b:
        continue
    x1, y1, x2, y2 = map(int, b.groups())
    area = (x2 - x1) * (y2 - y1)
    if area <= 0:
        continue
    if best is None or area < best[0]:
        best = (area, (x1 + x2) // 2, (y1 + y2) // 2)
if best:
    print(best[1], best[2])
PY
)
    [ -n "$xy" ] && break
    sleep 1.5
  done
  if [ -n "$xy" ]; then
    adb shell input tap $xy
    echo "TAP '$1' -> $xy"
  else
    echo "TAP MISS '$1'"
    MISSES=$((MISSES + 1))
  fi
  sleep 1.5
}
MISSES=0

back() { adb shell input keyevent KEYCODE_BACK; sleep 1.2; }
start_app() { adb shell am force-stop "$PKG"; adb shell am start -n "$PKG/.MainActivity" >/dev/null; sleep 6; }

# ── KOYU TEMA ──
adb shell cmd uimode night yes
sleep 1
start_app
shot 01-oyunlar-bos-koyu
tap "Profiller"
shot 02-profiller-bos-koyu
tap "Diğer işlemler"; sleep 1; tap "Yedekler"
shot 03-yedekler-bos-koyu
start_app; tap "Profiller"
tap "Diğer işlemler"; sleep 1; tap "Profil analizi"
shot 04-marketplace-bos-koyu
start_app; tap "Profiller"
tap "Diğer işlemler"; sleep 1; tap "İçe / dışa aktar"
shot 05-aktarim-koyu
start_app
tap "Ayarlar"
shot 06-ayarlar-koyu
tap "Shizuku"
shot 07-shizuku-kurulum-koyu
tap "Nasıl yapılır?"
shot 07b-shizuku-rehber-diyalog-koyu
tap "Kapat"
back
tap "Rehber"
shot 08-rehber-liste-koyu
tap "Shizuku kurulumu"
shot 09-rehber-bolum-koyu
# Rehber bölümü yerinde açılan akordeon: back ana ekrana döner, ikinci back
# uygulamadan çıkarır. Deterministik sıfırlama için yeniden başlat.
start_app
tap "Oyun ekle"
shot 10-uygulama-secici-koyu
tap "Uygulama ara…"
adb shell input text "settings"
sleep 1.5
shot 11-uygulama-arama-koyu
tap "Settings"
sleep 2.5
shot 12-sihirbaz-shizuku-yok-koyu
back; back

# ── AÇIK TEMA ──
adb shell cmd uimode night no
sleep 1
start_app
shot 13-oyunlar-bos-acik
tap "Profiller"
shot 14-profiller-bos-acik
tap "Ayarlar"
shot 15-ayarlar-acik
tap "Rehber"
shot 16-rehber-acik
back
tap "Oyunlar"
tap "Oyun ekle"
shot 17-uygulama-secici-acik
back

# ── BÜYÜK FONT (2.0) ──
adb shell settings put system font_scale 2.0
adb shell cmd uimode night yes
sleep 1
start_app
shot 18-oyunlar-buyukfont-koyu
tap "Ayarlar"
shot 19-ayarlar-buyukfont-koyu
tap "Rehber"
shot 20-rehber-buyukfont-koyu
back

# ── temizlik: cihazı varsayılan duruma döndür ──
adb shell settings put system font_scale 1.0
adb shell settings put global window_animation_scale 1
adb shell settings put global transition_animation_scale 1
adb shell settings put global animator_duration_scale 1
adb shell am force-stop "$PKG"
echo "TAP_MISS_COUNT=$MISSES"
echo "DESIGN_SHOTS_DONE"
[ "$MISSES" -eq 0 ] || exit 1