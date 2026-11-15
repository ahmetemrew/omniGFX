#!/usr/bin/env bash
# OmniGFX dolu durum görüntüleri (before/10–15 karşılıkları).
# ÖN KOŞUL: emülatör açık, OmniGFX + Shizuku kurulu, Shizuku servisi çalışıyor,
# demo config /sdcard/Download/UserCustom.ini olarak seed edilmiş.
# KULLANIM: tek flock -o çağrısı içinde (design_filled_session.sh bunu çağırır).
# Gezinti metin tabanlıdır (uiautomator dump); ezber koordinat YOK.
set -u
PKG=com.basitce.gfx
OUT=design/after
mkdir -p "$OUT"
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT

adb wait-for-device
adb shell wm size reset >/dev/null 2>&1
adb shell wm density reset >/dev/null 2>&1
# Kardeş/önceki koşudan kalma büyük font düzeni kaydırır (satır needle'ları
# farklı y'lere düşer); dolu oturum daima 1.0 ölçekle başlar.
adb shell settings put system font_scale 1.0
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
adb shell cmd uimode night yes
sleep 2

DUMPS=.qwen/tmp/ui-dumps
mkdir -p "$DUMPS"
MISSES=0

dump_ui() {
  adb shell uiautomator dump /sdcard/omni_ui.xml >/dev/null 2>&1
  adb pull /sdcard/omni_ui.xml "$TMP/ui.xml" >/dev/null 2>&1
}

shot() {
  adb shell screencap -p /sdcard/omni_shot.png
  adb pull /sdcard/omni_shot.png "$OUT/$1.png" >/dev/null 2>&1 \
    && echo "SHOT $1" || echo "SHOT FAIL $1"
  dump_ui
  cp "$TMP/ui.xml" "$DUMPS/$1.xml" 2>/dev/null
}

find_xy() { # $1 = needle; en küçük eşleşen düğümün merkezi
  python3 - "$TMP/ui.xml" "$1" <<'PY'
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
}

tap() { # $1 = needle; MISS'te bir kez yeniden dener
  local xy attempt
  for attempt in 1 2; do
    dump_ui
    xy=$(find_xy "$1")
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

tap_scroll() { # $1 = needle; görünmüyorsa kaydırıp dener
  local xy attempt
  for attempt in 1 2 3; do
    dump_ui
    xy=$(find_xy "$1")
    [ -n "$xy" ] && break
    adb shell input swipe 540 1800 540 900 300
    sleep 1
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

tap_any() { # $@ = aday needle'lar; ilk bulunanı dokunur, hiçbiri yoksa MISS
  local n xy
  for n in "$@"; do
    dump_ui
    xy=$(find_xy "$n")
    if [ -n "$xy" ]; then
      adb shell input tap $xy
      echo "TAP '$n' -> $xy"
      sleep 1.5
      return 0
    fi
  done
  echo "TAP MISS (any) '$*'"
  MISSES=$((MISSES + 1))
  sleep 1.5
  return 1
}

tap_optional() { # $@ = aday needle'lar; yoksa sessizce geçer (koşullu diyalog)
  local n xy
  dump_ui
  for n in "$@"; do
    xy=$(find_xy "$n")
    if [ -n "$xy" ]; then
      adb shell input tap $xy
      echo "TAP (optional) '$n' -> $xy"
      sleep 1.5
      return 0
    fi
  done
  return 1
}

wait_for() { # $1 = needle, $2 = saniye; dump'ta metin belirine dek bekler
  local i
  for i in $(seq 1 "$2"); do
    dump_ui
    if grep -q "$1" "$TMP/ui.xml" 2>/dev/null; then
      echo "WAIT OK '$1'"
      return 0
    fi
    sleep 1
  done
  echo "WAIT MISS '$1'"
  MISSES=$((MISSES + 1))
  return 1
}

wait_quiet() { # best-effort bekleme; MISS sayacını etkilemez (izin kurulumu)
  local i
  for i in $(seq 1 "$2"); do
    dump_ui
    if grep -q "$1" "$TMP/ui.xml" 2>/dev/null; then
      echo "WAIT OK '$1'"
      return 0
    fi
    sleep 1
  done
  echo "WAIT SKIP '$1'"
  return 1
}

back() { adb shell input keyevent KEYCODE_BACK; sleep 1.2; }
start_app() { adb shell am force-stop "$PKG"; adb shell am start -n "$PKG/.MainActivity" >/dev/null; sleep 6; }
dismiss_ime() { # IME açıksa kapat; değilse BACK gezinmeyi bozmasın
  if adb shell dumpsys input_method 2>/dev/null | grep -q 'mInputShown=true'; then
    adb shell input keyevent KEYCODE_BACK
    sleep 1.2
  fi
}

# ── Shizuku izni: ana ekranda "Bağlantı hazır" DOĞRULANANA dek en çok 3 deneme ──
# İzin diyaloğu Shizuku uygulamasının task'inde açılır; Allow sonrası OmniGFX'e
# force-stop + relaunch ile deterministik dönülür. Kurulum ekranının bağlantı
# denetimi yavaş olabilir (30 s); ara denemeler MISS sayacını kirletmez.
grant_shizuku() {
  local attempt
  for attempt in 1 2 3; do
    start_app
    # İzin zaten verilmişse (Shizuku DB kalıntısı) ana ekran "Bağlantı hazır"
    # gösterir; kurulum ekranı canContinue ile kendini kapatır, akış gerekmez.
    if wait_quiet "Bağlantı hazır" 5; then
      echo "SHIZUKU PERMISSION OK (zaten hazır, deneme $attempt)"
      return 0
    fi
    tap "Ayarlar"
    tap "Dosya erişimi için gerekli"
    if ! wait_quiet "Tekrar denetle" 30; then
      echo "GRANT: kurulum ekranı açılmadı (deneme $attempt)"
      back
      continue
    fi
    if grep -q "İzin iste" "$TMP/ui.xml" 2>/dev/null; then
      tap "İzin iste"
      if wait_quiet "Allow" 15 || wait_quiet "İzin ver" 5; then
        tap_any "Allow" "İzin ver" "ALLOW"
        sleep 3
      else
        echo "GRANT: izin diyaloğu görünmedi (deneme $attempt)"
      fi
    else
      tap_optional "Tekrar denetle"
      sleep 3
    fi
    start_app
    if wait_quiet "Bağlantı hazır" 20; then
      echo "SHIZUKU PERMISSION OK (deneme $attempt)"
      return 0
    fi
    echo "GRANT: bağlantı hazır değil (deneme $attempt)"
  done
  echo "SHIZUKU PERMISSION FAIL"
  return 1
}
grant_shizuku || { echo "FILLED_TAP_MISS_COUNT=$MISSES"; exit 1; }

# ── 24: Oyunlar — Shizuku hazır (before/24 ile aynı durum: boş liste + hazır
# bağlantı). Dolu liste sihirbazdan sonra 24b'de görüntülenir. ──
start_app
wait_for "Bağlantı hazır" 15
shot 24-oyunlar-hazir-koyu

# ── Sihirbaz: oyun ekle → hedef dosya → çek → düzenle → profil → uygula ──
tap "Oyun ekle"
wait_for "Uygulama ara" 10
tap "Uygulama ara…"
adb shell input text "settings"
sleep 1.5
tap "Settings"
wait_for "Kendin yol gir" 10
tap_scroll "/data/data/com.example"
adb shell input text "/sdcard/Download/UserCustom.ini"
sleep 1
dismiss_ime
tap_scroll "Dosyayı çek ve çözümle"
wait_for "Dosya çekildi" 25
shot 21-cek-analiz-koyu
tap "Düzenleyiciye geç"
wait_for "biçimi" 10
dismiss_ime
# Değer Okuması: değere dokun → yerinde düzenleme alanı açılır. İki gerçek
# değişiklik: FPSLimit 60→90 (üstte) ve Brightness 80→75 (altta). İçerik
# değişmezse pipeline NoChange yolundan döner; dosya yazılmaz, yedek oluşmaz
# ve 26 boş kalır. FPSLimit değişimi başarı ekranındaki eski→yeni okumasını
# (60→90) da görünür kılar.
# Needle olarak okumanın content-desc'i kullanılır ("FPSLimit: 60"): tur 2'den
# beri rayın min/max mikro etiketleri de değerle aynı metni taşıyabiliyor
# (60→tavan 60); çıplak değer needle'ı en küçük düğüm kuralıyla etikete
# denk gelip düzenlemeyi açmazdı. Desc benzersizdir.
# Kapatma, klavyenin örttüğü onay ikonuna dokunmakla DEĞİL IME Done (ENTER)
# ile yapılır: singleLine + ImeAction.Done → onEndEdit. Deterministik.
edit_value() { # $1 = eski değer (needle), $2 = yeni değer
  local try
  for try in 1 2; do
    tap_scroll "$1"
    wait_quiet "Değeri kaydet" 6 && break
    echo "EDIT: düzenleme açılmadı, tekrar ($1)"
  done
  adb shell input keyevent KEYCODE_MOVE_END
  adb shell input keyevent KEYCODE_DEL
  adb shell input keyevent KEYCODE_DEL
  adb shell input text "$2"
  sleep 1
  adb shell input keyevent KEYCODE_ENTER
  sleep 1.5
  dump_ui
  if grep -q "Değeri kaydet" "$TMP/ui.xml" 2>/dev/null; then
    adb shell input keyevent KEYCODE_ENTER
    sleep 1.5
  fi
  wait_for "$2" 8
}
edit_value "FPSLimit: 60" "90"
edit_value "Brightness: 80" "75"
dismiss_ime
sleep 1
# Görüntü için listenin başına dön: FPSLimit 60→90 okuması + tik rayları görünür.
adb shell input swipe 540 900 540 2000 300
sleep 1
adb shell input swipe 540 900 540 2000 300
sleep 1
shot 22-duzenleyici-koyu
dismiss_ime
# Adım 4: önce profil adı (alan üstte), sonra FPSLimit pin'i. Pin sayfasının
# "Değişkeni kaydet" düğmesi ekran dışında kalabilir → tap_scroll sayfa içinde
# kaydırarak bulur. Editördeki ilk "Sabitle" eşleşmesi bölüm başlığı satırına
# ait olduğundan pin oradan değil, adım 4'ün anahtar satırından yapılır.
tap "Değişkenleri ayarla"
if ! wait_quiet "Profil adı" 12; then
  # CTA klavye altında kaldıysa kapat ve bir kez daha dene.
  dismiss_ime
  sleep 1
  tap "Değişkenleri ayarla"
  wait_for "Profil adı" 12
fi
tap "Profil adı"
adb shell input text "Dengeli%s60%sFPS"
sleep 1
dismiss_ime
tap_scroll "FPSLimit"
wait_for "Değişkeni yapılandır" 8
tap_scroll "Değişkeni kaydet"
wait_for "Sabitlenen değişkenler" 10
tap "Kaydet ve cihaza yaz"
wait_for "Dosyayı yaz ve uygula" 15
tap "Dosyayı yaz ve uygula"
sleep 2
tap_optional "Yine de yaz" "Oyunu kapat ve yaz"
wait_for "Dosya uygulandı" 30
shot 23-uygula-basari-koyu
tap "Ana ekrana dön"
wait_for "Settings" 15
shot 24b-oyunlar-dolu-koyu

# ── Profiller dolu ──
tap "Profiller"
wait_for "Dengeli 60 FPS" 10
shot 25-profiller-dolu-koyu

# ── Yedekler dolu (uygulama sırasında oluşan .omnigfx.*.bak) ──
start_app
tap "Profiller"
tap "Diğer işlemler"; sleep 1; tap "Yedekler"
wait_for "Geri yükle" 15
shot 26-yedekler-dolu-koyu

# ── temizlik ──
adb shell settings put global window_animation_scale 1
adb shell settings put global transition_animation_scale 1
adb shell settings put global animator_duration_scale 1
adb shell am force-stop "$PKG"
echo "FILLED_TAP_MISS_COUNT=$MISSES"
echo "DESIGN_SHOTS_FILLED_DONE"
[ "$MISSES" -eq 0 ] || exit 1
