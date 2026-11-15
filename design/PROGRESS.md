# Tasarım Turu — İlerleme Hafızası

Proje: OmniGFX Pro (Shizuku tabanlı oyun config düzenleyici, Kotlin + Compose, 6 modül)
Tur başladı: 2026-11-14. Bu dosya turun TEK hafızasıdır; her anlamlı adımda güncellenir.

## Durum: F20 breadcrumb örtü padding düzeltmesi + doğrulama (2026-11-15, 17. oturum)

Brief yeniden koştu (sıfır context). CRITIQUE.md tur 2 (GENEL 7.33 KALDI) esas.
16. oturumun verify21'i hâlâ emu kuyruğunda (kardeş oturumlar aktif); APK'yı
kurulum ANINDA okuduğu için düzeltme + build şimdi yapıldı, verify21 SON kodu çekecek.

### 17. oturum bulguları ve yapılanlar
- [x] Skill'ler okundu (tasarim-temel → tasarim-compose). CRITIQUE.md + PROGRESS.md +
      REVIEW.md okundu. Tur 2'nin 8 düzeltmesi kodda grep/read ile doğrulandı
      (HomeScreen LazyColumn, GuideScreen widthIn+softWrap, CalibrationGauge
      10/14/18dp+outline, ShizukuSetup koşullu birincil, OmniSectionHeader tek sol
      grup, splash fit_tag/assert_safe, readoutPrevious, trailingLabel).
- [x] **YENİ KUSUR (tur 3, P0-4 devamı) — örtü 12dp KISA:** 16. oturumun dinamik
      örtü genişliği `first.offset + first.size` idi; layoutInfo koordinatları
      LazyRow'ın viewport'una göre, örtü ise dış Box'ın solundan başlıyor.
      LazyRow'ın `padding(horizontal = spacing.md)` (12dp) farkı kadar örtü kısa
      kalıp kısmi segmentin sağ ucunu sızdırıyordu. Düzeltme: `startPadPx +
      first.offset + first.size` (LocalDensity ile px'e çevrilen spacing.md).
      `StepSelectFileScreen.kt` BreadcrumbRow.
- [x] **BUILD 23 (07:01):** flock -o ile assembleDebug → BUILD SUCCESSFUL 28s,
      GRADLE_RC=0. APK 30.9 MB 07:01 (breadcrumb padding düzeltmesi dahil).
- [x] **TEST+LINT 23 (07:04):** flock -o ile testDebugUnitTest lintDebug →
      BUILD SUCCESSFUL 49s, GRADLE_RC=0; **76 test 0 fail 0 error 0 skip**;
      lint tüm modüllerde **0 hata**. (clean'siz — verify21'in APK'sı korundu.)
- [x] **verify21 TAMAMLANDI (07:53–08:00:54, build23 APK 07:53:38 kuruldu):**
      ana set TAP_MISS_COUNT=0, SHOTS_RC=0, CRASH_NONE, SESSION_RC=0; dolu set
      SHIZUKU PERMISSION OK (1. deneme), FILLED_TAP_MISS_COUNT=0,
      FILLED_SHOTS_RC=0, CRASH_NONE, FILLED_SESSION_RC=0. 29 görüntü taze.
- [x] Sayısal audit (verify21 seti): layout **28/28 TEMİZ**; palet **29/29 TEMİZ**;
      parity **14/14 SAME_SIDE** (02 oran 2.03 = meşru içerik: davet butonu).
- [x] Vision (4 kare, transkripsiyon köprüsü): 12 breadcrumb "… sdcard › Android ›
      data › com.android.settings" temiz; 26 "Yedek geçmişi 2" tek satır + sayaç
      yapışık + Sil solda/Geri yükle sağda; 22 üstü çizili 60 › pirinç 90 + canlı
      tik rayları (0–100/0–100/0–4) + tek menü; 24 üç gözlü yuva (2 petrol +
      1 çerçeveli) Pause çağrışımı yok.
- [x] Piksel kanıtı: 12 örtü bölgesinde (x104–148) glif SIZINTISI YOK — padding
      düzeltmesi çalışıyor (dump: kök ayraç 115–133 örtü altında, sdcard 149'dan
      başlıyor). 00 etiket bbox 273px ≤ wordmark 275px, merkez farkı 1px (kırpma
      yok). 18 "Oyun ekle" alt barın 299px üstünde; 19 "Sürüm" 171px üstünde;
      20 "01"/"02" 76px tek satır.
- [x] **YENİ DÜZELTME (tur 3, P1-8 tamamlama):** pasif BİRİNCİL buton metni tam
      parlak onSurfaceVariant idi (#A8B6B5); ikincil %38 kullanıyor, eleştirmen
      #647673 önermişti. OmniPrimaryButton disabledContentColor →
      onSurfaceVariant %38 (blend #5B6869 ≈ öneri; M3 pasif dili + tutarlılık).
      Etkilenen ekranlar: 04/07/12 (pasif birincil içeren tek ekranlar).
- [x] **BUILD 24 (08:09):** assembleDebug 20s GRADLE_RC=0.
- [x] **TAM CLEAN GATE (build25, 08:10–08:12):** flock -o ile clean assembleDebug
      testDebugUnitTest lintDebug assembleRelease → **BUILD SUCCESSFUL 2m,
      GRADLE_RC=0** (572 görev); **76 test 0 fail 0 error 0 skip**; lint **0 hata**.
      debug APK 30.6 MB 08:10; release unsigned 3.4 MB 08:12.
- [x] **verify22 TAMAMLANDI (09:03:13, build25 APK):** ana set 22 görüntü
      TAP_MISS_COUNT=0, SHOTS_RC=0, CRASH_NONE, SESSION_RC=0. Emülatör trap ile
      kapatıldı (kalan qemu port 5570 kardeş oturuma ait). Dolu set (21–26+24b)
      verify21'den geçerli: build23→25 farkı yalnız pasif birincil metin alfası;
      dolu ekranların hiçbirinde pasif birincil yok (tüm CTA'lar enabled).
- [x] **SON set denetimleri:** layout **28/28 TEMİZ**, palet **29/29 TEMİZ**,
      parity **14/14 SAME_SIDE**. Piksel: 04 Çözümle #5B6869, 07 İzin iste
      #5B6869, 12 Dosyayı çek ve çözümle #5B6869 (%38 blend ✓); 07 Tekrar
      denetle #06231F/petrol ✓; 04 İçe aktar #4B5556 ✓; 12 örtü sızıntısı YOK ✓.
- [x] DESIGN.md karar 13 (tek pasif dili) + 14 (örtü koordinat geometrisi);
      NOTES.md madde 11 padding notu; REVIEW.md **tur 3** yazıldı: 29 ekran
      genel **8.63**, en düşük ekran 8.56, en düşük kriter 8; eleştirmen tur 2
      çapasıyla madde madde Δ tablosu + kanıt eşlemesi.
- [x] material-3-expressive skill okundu; ilkeler mevcut sistemle örtüşüyor
      (şekil kontrastı, 2 imza anı, dinamik renk bilinçli kapalı, reduced-motion,
      semantik token, etiketler korunuyor).
- [x] **commit + push + CI YEŞİL + QWEN_DESIGN_DONE.md:** d1439d8 fix(ui) 17
      dosya · aa389bc fix(design) 6 dosya · ae33bfd docs(design) 40 dosya.
      Push 7400b19..ae33bfd (fast-forward, force yok; sır taraması temiz).
      **CI run 43684202424 conclusion=success, headSha=ae33bfd.**
      QWEN_DESIGN_DONE.md kökte (gitignore'lı). Tur TAMAM.

## Durum: F19 bağımsız doğrulama + breadcrumb kök düzeltmesi (2026-11-15, 16. oturum)

Brief yeniden koştu (sıfır context). CRITIQUE.md tur 2 (GENEL 7.33 KALDI) esas.
15. oturumun 8 düzeltmesi kodda bağımsız doğrulandı; verify20'nin ana seti
(00–20) SON APK'dan taze ve temiz; dolu set (21–26+24b) hâlâ verify19'dan
(04:41, tur 2 düzeltmelerinden ÖNCEki APK) → yeniden çekilecek.

### 16. oturum bulguları ve yapılanlar
- [x] Skill'ler okundu (tasarim-temel → tasarim-compose). CRITIQUE.md + PROGRESS.md okundu.
- [x] 8 düzeltme kodda doğrulandı: HomeScreen LazyColumn; GuideScreen widthIn(min=24)+
      softWrap=false; StepSelectFileScreen "…" örtüsü; OmniTypography.readoutPrevious;
      CalibrationGauge 10/14/18dp + surfaceContainerHighest dolgu/outline çerçeve;
      ShizukuSetupScreen durum-koşullu birincil; OmniValueReadout.trailingLabel;
      StepPushApply "Uygulandı" etiketi.
- [x] **BUILD GATE (bu oturum, 06:02, build21.log):** flock -o ile clean assembleDebug
      testDebugUnitTest lintDebug assembleRelease → **BUILD SUCCESSFUL 2m2s, GRADLE_RC=0**
      (572 görev). **76 test 0 fail 0 error 0 skip**; lint tüm modüllerde **0 hata**.
      APK 30.5 MB 06:02; release unsigned 06:03.
- [x] verify20 ana set (05:48–05:52, design-verify20.log): TAP_MISS_COUNT=0, SHOTS_RC=0,
      CRASH_NONE, SESSION_RC=0. Layout audit **21/21 TEMİZ**; palet **22/22 TEMİZ**;
      before/after **14/14 SAME_SIDE** (02 parlaklık oranı 2.04 = meşru içerik: davet butonu).
- [x] Vision (4 kare): 00 splash "CALIBRATION SUITE" TAM (P0-1 ✓); 20 büyük font
      "01"/"02" tek satır + alt başlık tam (P0-3 ✓); 18 "Oyun ekle" alt bar üstünde
      tam + 3 gözlü gauge (P0-2 ✓); 12 breadcrumb "…" öneki var AMA kırpık Root
      glifi sızıyor (yeni kusur).
- [x] Piksel kanıtı (dump enabled alanı Compose'da GÜVENİLMEZ — disabled butonlar
      true görünür): 07 "Tekrar denetle" petrol dolgulu birincil, "İzin iste"
      #2C393A zemin + soluk metin (P1-8 ✓); 04 "İçe aktar" #4B5556 soluk metin +
      görünür çerçeve (P1-8 ✓).
- [x] **YENİ KUSUR + KÖK DÜZELTME (tur 3, P0-4 devamı):** breadcrumb "…" örtüsü
      sabit genişlikliydi (metin+2×12dp); kaydırma konumuna göre kısmi kök öğenin
      sağ ucu örtünün dışında kalıp kırpık glif sızdırıyordu (PNG'de "… t › sdcard",
      dump'ta Root [114,132]). Düzeltme: örtü genişliği DİNAMİK — layoutInfo'dan
      ilk görünür öğenin sağ ucu (offset+size); ayraç öğe SONUNA taşındı (derin
      kaydırmada satır çıplak "›" ile başlayamaz); örtü fillMaxHeight + clipToBounds.
      build21b: BUILD SUCCESSFUL 25s.
- [x] **Dolu oturum kök nedeni (verify20 FAIL, logcat kanıtlı):** Shizuku'nun izin
      veritabanı AVD userdata'sında koşular arası kalıyor (Shizuku hiç pm clear
      edilmiyordu; verify19'da verilen izin hâlâ geçerli). Servis çalışırken
      checkSelfPermission()=GRANTED → CONNECTED → canContinue=true → kurulum
      ekranı LaunchedEffect ile KENDİNİ anında popBackStack ediyor (logcat:
      row tap +40ms nav back callback, +130ms Activity callback'ine dönüş).
      Grant akışı açılır açılmaz kapanan ekranda "Tekrar denetle" arıyordu.
      Düzeltme: design_filled_session.sh Shizuku'yu da pm clear ediyor;
      grant_shizuku ana ekranda "Bağlantı hazır" görürse akışı atlıyor.
- [x] **BUILD GATE 2 (bu oturum, 06:37, build22.log):** flock -o ile clean
      assembleDebug testDebugUnitTest lintDebug assembleRelease → **BUILD
      SUCCESSFUL 2m15s, GRADLE_RC=0**; **76 test 0 fail 0 error 0 skip**;
      lint **0 hata**. APK 30.5 MB 06:37 (breadcrumb düzeltmesi dahil).
- [ ] verify21 (PID 2100832, nohup setsid, log design-verify21.log): ana + dolu
      oturum, 29 görüntü SON APK'dan; emu kuyruğunda (kardeş emülatörler aktif).
- [ ] Sayısal audit + palet + parity (yeni set) + vision (imza anları 22/23/24/26).
- [ ] REVIEW.md tur 3 + commit/push/CI + QWEN_DESIGN_DONE.md.

## Durum: F18 eleştirmen turu 2 düzeltmeleri (2026-11-15, 15. oturum)

Brief yeniden koştu (sıfır context). CRITIQUE.md artık **tur 2** (2026-11-15 05:03,
design/critic/round-2.md ile birebir aynı, md5 doğrulandı): **GENEL 7.33, KALDI**
(eşik: ort ≥8.0, hiçbir ekran <7.0, sıfır Bitmişlik kusuru). Tur 1'in 15 maddesi
çözülmüş; tur 2 yeni kusurlar buldu. 8 madde (P0: 5, P1: 3) bu oturumda çözülüyor.

### Tur 2 çözümleri (madde madde, CRITIQUE.md 2026-11-15 05:03)

**P0-1 — Splash "ALIBRATION SUIT":** Kök neden sayısal: eski etiket 11sp+1.2track
≈131dp genişlik, baseline y=181'de 160dp maskenin kirişi ~104dp → C/E kırpılıyordu.
`gen_splash_vector.py` yeniden yazıldı: wordmark baseline 156, etiket baseline 169;
`fit_tag()` en büyük puntoyu 0.12em track ile seçiyor (genişlik ≤ wordmark VE
kiriş−32dp); `assert_safe()` tüm glif bbox köşelerini r≤78'de doğruluyor. Sonuç:
**word=88.9dp, tag=87.9dp, size=7.25, track=0.87, kenar payı 19.3dp** — üretici
assert'leri geçti, iki tema vektörü yeniden üretildi. Eleştirmenin 9.5–10sp önerisi
kendi genişlik kısıtıyla çelişiyordu; sert kısıtlar (kırpma yok + ≤wordmark + ≥16dp
pay) esas alındı, karar DESIGN.md'ye not edildi.

**P0-2 — 18/19 büyük font kesilmesi:** HomeScreen gövdesi tek LazyColumn'a taşındı
(boş durum artık kaydırılabilir; eskiden sabit Column'du, buton alt bar altında
kesiliyordu). Alt pay: boş 24dp, dolu liste 72dp (FAB yüksekliği). OmniEmptyState
fontScale>1.15'te dikey padding 32→16dp, aralık 12→8dp, eylem üstü 8→4dp.
OmniListRow dikey padding büyük fontta 12→8dp (heightIn 48dp korunuyor).
SettingsScreen grup arası büyük fontta 24→16dp, üst boşluk 20→12dp; zaten
verticalScroll + 40dp alt pay vardı.

**P0-3 — 20/09 rehber kırılması:** Sıra numarası `width(24.dp)` sabitti → 2.0
fontta "01" dikey kırılıyordu; `widthIn(min=24.dp)` + `maxLines=1` + `softWrap=false`.
Hero büyük fontta kompakt (padding 20→16, paragraf üstü 16→8). Açılan kart
`animateScrollToItem(index+1)` ile görünür alana giriyor (azaltılmış harekette
anında scrollToItem) — içerik alt kenarda kesik başlamıyor. Alt 32dp +
navigationBarsPadding zaten mevcuttu.

**P0-4 — 12 breadcrumb + hata rengi:** TextButton'un 58dp asgari genişliği "data"
segmentinde ~24dp anlamsız boşluk üretiyordu → eşit 4dp yatay padding'li, 48dp
yükseklikli düz clickable hedefler; ayraçlar önde (2dp padding → segment+ayraç
arası 6dp). Kök kaydırılınca başa sabit "…" öneki (derivedStateOf, remember'lı);
satır asla çıplak "›" ile başlamıyor. OmniErrorCard eylemi OmniTextButton (petrol)
yerine 36dp hap OutlinedButton: onErrorContainer metin + error %35 çerçeve.

**P0-5 — 26 başlık + kart çukuru:** OmniSectionHeader'da başlık ve Spacer ayrı
weight(1f) alınca kalan genişlik yarı yarıya bölünüyor, "Yedek geçmişi" iki satıra
kırılıp "1" ortada yüzüyordu → başlık+sayaç tek sol grup (weight 1f, başlık
maxLines=1 ellipsis, sayaç 8dp yapışık), eylem sağda intrinsic. BackupRow: Sil
üst köşeden alt satırın soluna alındı (48dp IconButton), Geri yükle sağda birincil,
arada 1dp outlineVariant ayraç — sol alt çukur dengelendi.

**P1-6 — 22/23 Değer Okuması birliği:** Yeni `OmniMonoType.readoutPrevious`
(19sp Medium) her iki ekranda üstü çizili eski değer + "›" + 28sp Bold yeni değer.
22'nin rayları ölüydü çünkü görüntü anında henüz pin yoktu (fraction=null);
pinsiz sayısal satırlara da türetilmiş ölçek geldi (`derivedScaleMax`: 2→4, 90→100,
75→80…) → canlı iğne + aktif tikler + min/max mikro etiketleri. OmniValueReadout'taki
14dp kalem ikonu kaldırıldı → `trailingLabel` parametresi; 23 "Uygulandı" etiketi
taşıyor (idle özet etiketsiz). 22 alt barı: 1dp outlineVariant üst sınır + tam opak
surfaceContainerLow + 16dp dikey pay.

**P1-7 — 24/24b Pause benzetmesi:** CalibrationGauge eşit 4×16dp çubuklardan
kademeli 10/14/18dp gözlere geçti (4.5dp genişlik, 3dp aralık, alt hizalı) — iki
yanan göz artık "||" değil, 2/3 kademeli seviye. Pasif gözler outlineVariant
(#2E3B3A, 1.25:1) yerine surfaceContainerHighest (#2C393A) dolgu + 1dp outline
(#647673) çerçeve: yuva her koşulda "3 gözlü" okunuyor. Renkler token'dan;
eleştirmenin hex'leriyle birebir. KDoc + NOTES.md kararı güncellendi.

**P1-8 — 07/04 durum-çelişkili vurgu:** ShizukuSetupScreen durum paneli:
`shizukuAvailable` (CONNECTED||PERMISSION_REQUIRED) false iken birincil dolgulu
"Tekrar denetle", "İzin iste" disabled (surfaceContainerHighest zemin); servis
kalkınca "İzin iste" birincileşiyor, "Tekrar denetle" ikincileşiyor.
OmniSecondaryButton disabled metni onSurfaceVariant %38 + görünür outlineVariant
çerçeve → 04'te boş "İçe aktar" artık aktif gibi parlamıyor (enabled koşulu
zaten analysis.canImport'tı, görsel pasiflik eksikti).

### Doğrulama (bu oturum)
- [x] Build gate: flock -o ile clean+assembleDebug+testDebugUnitTest+lintDebug+
      assembleRelease → **BUILD SUCCESSFUL, GRADLE_RC=0** (build20c.log, 1m49s).
      **76 test 0 fail 0 error 0 skip**; lint 0 hata (app 1 warning: UnusedResources
      ic_launcher_foreground; feature-wizard 13 warning önceden mevcut SdCardPath/
      QueryPermissionsNeeded). APK 30.5 MB, 05:26. İlk koşuda 2 hata yakalandı ve
      düzeltildi: StepEditScreen Box import'u + derivedStateOf remember (lint).
- [ ] verify20: iki emülatör oturumu (design_verify17.sh sürücüsü, nohup setsid,
      log .qwen/tmp/design-verify20.log) — 29 görüntü SON APK'dan.
- [ ] Sayısal audit + palet + before/after denklik + vision (imza anları).
- [ ] REVIEW.md tur 3 + commit/push/CI + QWEN_DESIGN_DONE.md.

## Durum: F17 bağımsız yeniden doğrulama + teslim (2026-11-15, 14. oturum)

Brief yeniden koştu (sıfır context). CRITIQUE.md (tur 1, GENEL 6.31 KALDI) esas.
13. oturumun iki açık maddesi kapatıldı; TÜM kanıt bu oturumda yeniden üretildi.

### 14. oturum bulguları ve yapılanlar
- [x] Skill'ler okundu (tasarim-temel → tasarim-compose → material-3-expressive).
      CRITIQUE.md + PROGRESS.md + REVIEW.md okundu.
- [x] 13. oturumun main18 koşusu logdan doğrulandı: TAP_MISS_COUNT=0, SHOTS_RC=0,
      CRASH_NONE; 03 taze (04:28). 03 layout+palet denetimi TEMİZ → 29/29 tamam.
- [x] **BUILD GATE (bu oturum, 04:35, build19.log):** flock -o ile clean
      assembleDebug testDebugUnitTest lintDebug assembleRelease → **BUILD
      SUCCESSFUL 18s (cache), GRADLE_RC=0** (572 görev). **76 test, 0 fail,
      0 error, 0 skip** (XML sayımı). lint: **0 error**, 1 warning
      (UnusedResources ic_launcher_foreground). APK 30.5 MB, 04:35.
- [x] **verify19 (bu oturum, 04:35–04:44, design-verify19.log):** design_verify17.sh
      sürücüsüyle iki emülatör oturumu, SON APK'dan:
      • Ana set (00–20): **TAP_MISS_COUNT=0, SHOTS_RC=0, CRASH_NONE, SESSION_RC=0.**
      • Dolu set (21–26+24b): Shizuku SERVER UP, izin 1. denemede, FPSLimit 60→90 +
        Brightness 80→75, gerçek yazma + yedek → **FILLED_TAP_MISS_COUNT=0,
        FILLED_SHOTS_RC=0, CRASH_NONE, FILLED_SESSION_RC=0.**
      Emülatörler trap ile kapatıldı (port 5560); adb'deki emulator-5570 kardeş
      oturuma ait, dokunulmadı.
- [x] Sayısal denetim (verify19 görüntüleri): layout audit **28/28 dump TEMİZ**;
      palet **29/29 PNG TEMİZ** (koyu #121A1B / açık #F4F7F6, scrim türevleri dahil).
- [x] before/after denkliği: 14/14 çift SAME_SIDE (köşe pikselleri birebir
      #121A1B); 4 çiftteki parlaklık farkı (02/21/23/25, Δ≤16) tema değil meşru
      içerik değişimi (eklenen Uygula/okuma/başlık) — tema tarafı + durum denk.
- [x] Görsel (vision, 3 kare): 03 (Yenile üst bar ikonu, davetkâr boş durum),
      01 (3 segment gösterge + Bağla hapı + ikincil Oyun ekle + OmniTabHeader),
      25 (Uygula birincil + "UserCustom.ini · 2 değişken" + Dinamik çipi + menü).
- [x] Kod disiplini: Color(0x tema dışı 0; fontSize tema dışı 0; gradyan 0;
      yasaklı copy 0 (tek KDoc yorumu, kullanıcı metni değil).
- [x] Sürüm uyumu teyit: AGP 9.5.1 / Kotlin 2.4.21 / Gradle 9.9.1; CI JDK 17 +
      testDebugUnitTest lintDebug assembleDebug assembleRelease (check.yml).
- [x] commit + push + CI yeşil + QWEN_DESIGN_DONE.md:
      a06776a fix(ui) 27 dosya · bc35939 fix(design) 7 dosya · 4100b60 docs(design)
      39 dosya. Push 6253185..4100b60 (fast-forward, force yok; sır taraması temiz,
      keystore/APK/QWEN_* yok). **CI YEŞİL: run 43664735297, verify=success.**
      QWEN_DESIGN_DONE.md kökte (gitignore'lı). Tur TAMAM — 29 ekran ort 8.63,
      en düşük kriter 8; build+test+lint+release yeşil; iki oturumda CRASH_NONE.

## Durum: F16 dolu durum kök nedenleri + verify17 (2026-11-15, 13. oturum)

Brief yeniden koştu (sıfır context). CRITIQUE.md (tur 1, GENEL 6.31 KALDI) esas.
12. oturumun kod düzeltmeleri + build kanıtı doğrulandı; dolu durum görüntüleri
GEÇERSİZ bulundu (after/21–26 + 24b invalid PNG'lerle doluydu).

### 13. oturum bulguları ve yapılanlar
- [x] Skill'ler okundu (tasarim-temel → tasarim-compose). CRITIQUE.md + PROGRESS.md okundu.
- [x] verify15 (02:25): build YEŞİL, 22 ana görüntü TAP_MISS=0 CRASH_NONE; ama dolu
      oturum FILLED_TAP_MISS_COUNT=13. Sonra design-filled16 (03:04–03:19) dolu
      PNG'leri 31 MISS'lik daha kötü koşuyla ezmiş. İki koşu da Shizuku izin
      aşamasında farklı noktalarda kırılmış.
- [x] Kök neden 1 (filled16): font_scale 2.0 kalıntısı — ayarlar satırı y=504 yerine
      y=738'de, kurulum ekranı açılmadı, izin hiç istenmedi. Düzeltme: her iki
      görüntü betiği başlangıçta `settings put system font_scale 1.0`.
- [x] Kök neden 2 (verify15): editörde IME onay ikonunu örtüyor; kök Column'da
      imePadding YOK → düzenlenen satır/CTA klavye altında kalıyor. Düzeltme:
      SetupWizardScreen köküne `.imePadding()` (tüm adımlar; UX düzeltmesi,
      işlevsellik aynı). Betikte düzenleme kapatma artık IME Done (ENTER) ile —
      klavye altındaki ikona dokunma yok.
- [x] Betik sertleştirme (design_shots_filled.sh): grant_shizuku() en çok 3 deneme,
      ana ekranda "Bağlantı hazır" DOĞRULAMALI (wait_quiet, MISS kirletmez);
      kurulum denetimi 30 s; edit_value() düzenleme açılmasını doğrular + ENTER +
      kapanmayı doğrular; CTA tap'i IME korumalı retry.
- [x] **BUILD GATE (bu oturum, 03:33):** flock -o ile clean assembleDebug
      testDebugUnitTest lintDebug assembleRelease → **BUILD SUCCESSFUL 2m12s,
      GRADLE_RC=0** (572 görev). **76 test, 0 fail, 0 error, 0 skip.** lint:
      1 warning (UnusedResources ic_launcher_foreground), **0 error**.
      APK: app/build/outputs/apk/debug/app-debug.apk (30.5 MB, 03:33).
- [x] verify17 (log design-verify17.log): dolu oturum **FILLED_TAP_MISS_COUNT=0,
      CRASH_NONE, FILLED_SESSION_RC=0** — izin 1. denemede, ENTER ile düzenleme,
      imePadding sayesinde CTA geçişi temiz; 21–26+24b SON APK'dan geçerli.
      Ana oturumda TAP_MISS=2 (ilk overflow start_app korumasızdı → 03 launcher).
- [x] main18 (log design-main18.log): düzeltilmiş design_shots.sh ile ana set
      yeniden (ilk overflow erişimine start_app+Profiller eklendi).
- [x] Sayısal denetim (yeni görüntüler): layout audit 28/29 TEMİZ (03 main18'i
      bekliyor); palet 28/29 TEMİZ. Audit araçları düzeltildi: long-clickable
      substring sahte pozitifi, yatay kesik dilim (breadcrumb Root), content-desc
      kesik imzası, scrim türevleri palet katmanı, 26 needle ".bak".
- [x] Eleştirmen maddeleri dump kanıtı: 17/17 kontrol OK (İçindekiler yok, 01 sıra
      no, bottom sheet, › + Shizuku'yu bağla, Korunuyor + teknik detay gizli,
      Uygula 89×48dp, Bağla hapı, Panodan yapıştır, 1/5 sayacı…).
- [x] Görsel (vision): 00 splash (kadran + wordmark; etiket bbox piksel eşleşmesi
      tam), 22 editör (60›90 pirinç okuma + tik rayı + tek menü), 23 başarı
      (üstü çizili 60 → pirinç 90, min–max ray, mühür yalnız final).
- [x] 07b scrim piksel ölçümü: (6,9,9) = bg×0.35 → tam %65.
- [x] before/after parlaklık denkliği: 13/14 çift denk (tek fark geçersiz 03).
- [x] REVIEW.md tur 2 yazıldı: eleştirmen çapasıyla kalibre (6=jenerik, 8=ürüne
      özgü+kusursuz, 9=imza kusursuz). **29 ekran ≥8.56, genel 8.63, en düşük
      kriter 8.** 15 madde → kanıt eşleme tablosu REVIEW.md'de. Tur 1'in 6.31'i
      esas alındı; şişirme yok (tek 9.00: 23, imza final).
- [x] material-3-expressive skill okundu; checklist doğrulamalarla örtüşüyor
      (dinamik renk bilinçli kapalı — DESIGN.md §1).
- [x] Sürüm uyumu teyit: AGP 9.5.1 / Kotlin 2.4.21 / KSP 2.3.12 / Hilt 2.60.1 /
      Gradle 9.9.1 / JVM 17 / CI JDK 17 (AS 2026.1.4 destek aralığında).
- [x] main18 sonucu: 03 taze + TAP_MISS=0 (log doğrulandı; 14. oturumda 03
      layout+palet TEMİZ, verify19 ile yeniden üretildi).
- [x] commit + push + CI yeşil + QWEN_DESIGN_DONE.md → 14. oturumda kapatıldı.

## Durum: F15 eleştirmen düzeltmelerinin bağımsız doğrulaması (2026-11-15, 12. oturum)

Brief yeniden koştu (sıfır context). CRITIQUE.md (tur 1, GENEL 6.31 KALDI) esas alındı.
11. oturumun 15 maddelik düzeltmesi working tree'de mevcut; bu oturum işi DOĞRULUYOR.

### 12. oturum bulguları ve yapılanlar
- [x] Skill'ler okundu (tasarim-temel → tasarim-compose). CRITIQUE.md + PROGRESS.md okundu.
- [x] 15 düzeltmenin TAMAMI kodda doğrulandı (grep + read):
      P0-1 StepEdit ValueReadoutRow + ValueEditor + RowActionsMenu, ConfigNode.originalValue
      (ConfigTreeBuilder 5 nokta), StepPushApply previous=originalValue + min/maxLabel;
      P0-2 ProfileRow "Uygula" + dosya/değişken özeti; P0-3 GuideScreen TableOfContents yok;
      P0-4 ShizukuGuideDialog ModalBottomSheet + scrim 0.65; P0-5 breadcrumb animateScrollToItem
      + "›" + connectShizuku; P0-6 FileIdentityCard "Korunuyor" + teknik detay açılır;
      P0-7 BackupManager Refresh TopAppBar actions; P1-8 OmniTabHeader 3 sekmede;
      P1-9 SignalGauge 3 segment + "Bağla"/"İzin ver"; P1-10 FAB + gameProfiles;
      P2-13 Çözümle enabled=false + Panodan yapıştır; P2-14 ic_splash_brand (drawable+night,
      fontTools glif yolları) + themes.xml; P2-15 adım sayacı "N / 5".
- [x] 11. oturum build kanıtı: build11b.log BUILD SUCCESSFUL (39s), test11.log BUILD
      SUCCESSFUL (1m6s) → F14 kodu derleniyor, testler yeşil.
- [x] Kilit açlığı kök nedeni + çözüm: flock shim'i betik metninde adb görünce tüm
      komutu emu.lock kuyruğuna sokuyor; verify11 (karma gate) 45 dk açlıktan öldü.
      Çözüm (memory "karma gate'i build-only + emülatör çağrısına böl"): verify11
      kaldırıldı, `tools/design_verify_split.sh` yazıldı — build AYRI flock (gradlew'de
      adb yok → boş build slotu), emülatör oturumu AYRI flock (emu kuyruğu). Sürücü
      flock ile sarılmaz (iç flock self-deadlock). nohup setsid, log design-verify15.log.
- [x] **BUILD DOĞRULANDI (verify15, build.lock.2):** clean assembleDebug testDebugUnitTest
      lintDebug assembleRelease → **BUILD SUCCESSFUL 2m5s, GRADLE_RC=0** (572 görev).
      **76 test, 0 fail, 0 error, 0 skip** (XML sayımı). lint: 1 issue, **0 error**.
      APK: app/build/outputs/apk/debug/app-debug.apk (30.5 MB, 01:40 taze).
- [ ] Emülatör oturumu (emu kuyruğunda): 22 ana + 7 dolu görüntü, CRASH_NONE.
- [ ] Yeni görüntülerle sayısal audit + palet + before/after denklik (parlaklık/köşe).
- [ ] REVIEW.md eleştirmen kalibrasyonuyla yeniden puanlama (madde madde delta).
- [ ] commit + push + CI yeşil + QWEN_DESIGN_DONE.md.

## Durum: F14 eleştirmen turu 1 düzeltmeleri (2026-11-15, 11. oturum)

Bağımsız eleştirmen (design/CRITIQUE.md, 2026-11-15 00:20) 29 ekranı puanladı:
**GENEL 6.31/10, KALDI** (eşik ≥8.0). Kendi REVIEW.md puanlarım (~8.89) kalibre
değil; eleştirmen puanları esas alındı. 15 maddelik düzeltme listesi (P0: 7, P1: 4,
P2: 4) bu oturumda çözülüyor. Madde madde çözüm aşağıda "Eleştiri çözümleri"nde.

### Plan
- [ ] P0-1 ValueReadout düzenleyiciye (22) + başarı ekranı eski→yeni (23) + min/max etiketleri
- [ ] P0-2 Profil kartına "Uygula" birincil butonu + dosya/değişken özeti (25)
- [ ] P0-3 Rehber: İçindekiler kartı kaldır, akordeon doğrudan, clipping düzelt (08/09/16/20)
- [ ] P0-4 Shizuku rehberi ModalBottomSheet + scrim 0.65 + numaralı adımlar (07b)
- [ ] P0-5 Breadcrumb otomatik sona kaydır + "›" + Shizuku hata eylemi (12)
- [ ] P0-6 SELinux/UID/octal varsayılan gizli, "Korunuyor" rozeti + teknik detay açılır (21)
- [ ] P0-7 Yedekler: Yenile üst bara, kart tarihi başlık + yol + kısaltılmış kimlik (03/26)
- [ ] P1-8 Üç ana sekmede aynı sol hizalı başlık şablonu (OmniTabHeader)
- [ ] P1-9 SignalGauge 3 kalibrasyon segmenti + "Bağla" hapı + Oyun ekle ikincil (01/13/24)
- [ ] P1-10 FAB stadium + primaryContainer tonu, oyun kartına profil özeti (24b)
- [ ] P1-11 Ara adım hero kaldır (21), mühür yalnız finalde (23)
- [ ] P2-12 Rehber ikonları tek stil + 01..09 sıra numarası
- [ ] P2-13 Çözümle boşken disabled + Panodan yapıştır + mono placeholder (04)
- [ ] P2-14 Splash: kalibrasyon kadranı + Barlow wordmark (fontTools ile gerçek glif yolları)
- [ ] P2-15 Sihirbaz adım sayacı "1 / 5" + segment 4dp/2dp
- [ ] Betik güncellemeleri (editör akışı değişti), build+test+lint, emülatör görüntüleri,
      REVIEW.md yeniden puanlama, commit+push+CI, QWEN_DESIGN_DONE.md

### Eleştiri çözümleri (madde madde, CRITIQUE.md 2026-11-15 00:20)

**P0-1 — Değer Okuması düzenleyiciye (22) + başarı eski→yeni (23):**
`ConfigNode.originalValue` eklendi (UI modeli; DB şeması değişmedi), `ConfigTreeBuilder`
5 üretim noktasında dosyadan çekilen ilk değeri yazıyor. `StepEditScreen` yeniden yazıldı:
KEY_VALUE satırları artık Değer Okuması — monoKey anahtar, değişimde üstü çizili eski
değer + › + pirinç yeni değer (monoReadout 28sp), sayısal satırlarda 14dp tik rayı
(pinli SLIDER ise min/max'tan gerçek konum). Okumaya dokununca satır yerinde düzenleme
alanına dönüşüyor (FocusRequester + IME Done + "Değeri kaydet"/"Vazgeç"); canlı güncelleme
ve undo/redo davranışı korundu. Kart başına 4 metin düğmesi ("Yukarı Aşağı Altına ekle Sil")
kaldırıldı → tek MoreVert menüsü ("Satır işlemleri"). Kart ~96dp. Başarı ekranı
(`StepPushApplyScreen.VariableReadouts`) "eski" tarafı artık pin anı değil dosya değeri
(originalValue); compact kaldırıldı (28sp okuma), ray uçlarına min–max mikro etiketleri
(`OmniValueReadout.minLabel/maxLabel`) eklendi. Dolu betik FPSLimit 60→90 + Brightness
80→75 yapıyor → 23'te gerçek 60→90 geçişi görünür.

**P0-2 — Profil kartında "Uygula" (25):** `ProfileRow`'a 36dp stadium birincil "Uygula"
(`OmniPrimaryButton.compact`) eklendi; Sil taşma menüsüne alındı (swipe-to-delete korunuyor).
Karta monoMicro dosya özeti: "UserCustom.ini · 1 değişken" (userValuesJson'dan gerçek sayım;
ham profilde yalnız dosya adı). Dokunma hedefi kartın tamamı ≥72dp.

**P0-3 — Rehber çift liste + kesilme (08/09/16/20):** `TableOfContents` bileşeni tamamen
kaldırıldı; akordeon bölümleri hero'nun hemen altında 12dp aralıkla başlıyor. contentPadding
top 12dp / bottom 32dp + navigationBarsPadding. Bölüm satırları doğal sarmaya bırakıldı
(büyük fontta kesilme yok). İlk durum: tüm bölümler kapalı (08 temiz liste, 09 açılmış bölüm).

**P0-4 — Shizuku rehberi (07b):** `ShizukuGuideDialog` AlertDialog → ModalBottomSheet
(tam genişlik, üst köşe 16dp, surfaceContainerHigh, scrim %65). Adımlar 24dp numaralı
rozetlerle; "Shizuku başlatma rehberi" Adım 2 içinde 36dp OutlinedButton (8dp radius).
Altta "Shizuku'yu indir" birincil + "Kapat" (betik uyumu korundu).

**P0-5 — Breadcrumb + hata (12):** LazyRow son segmente otomatik kayıyor
(LaunchedEffect + animateScrollToItem), ayraç "›" (monoMicro). Shizuku bağlı değilken
hata kartı: "Dosya gezgini için Shizuku bağlantısı gerekli. Shizuku'yu başlatıp tekrar
dene." + "Shizuku'yu bağla" eylemi (`SetupWizardViewModel.connectShizuku`: izin gerekiyorsa
requestPermission, değilse Shizuku uygulamasını aç). "Shizuku process oluşturulamadı."
jargonu ViewModel'de "Shizuku servisi yanıt vermedi." olarak eşlendi.

**P0-6 — SELinux/UID/octal (21):** `FileIdentityCard` varsayılanı insani: "Orijinal dosya
izinleri ve güvenlik kimliği yedeklendi — yazma sırasında korunur" + petrol "Korunuyor"
çipi. Ham değerler (Sahip/İzinler/Güvenlik etiketi, monoMicro) yalnızca "Teknik detayları
göster" (labelMedium, ExpandMore) ile açılır.

**P0-7 — Yedekler (03/26):** "Yenile" yüzen buton → TopAppBar sağ ikon eylemi
(contentDescription "Yenile"). "Eski yedekleri temizle" liste başlığı satırının sağında
(OmniSectionHeader action). Kart başlığı insanca tarih "14 Kasım 2026 · 23:56"
(titleMedium SemiBold, Locale tr), 2. satır hedef yol (monoKey), 3. satır "#<ts> · .bak"
(monoMicro, outline). Sil ikonu sağ üst köşede; "Geri yükle" alt sağda birincil.

**P1-8 — Üç sekme başlığı:** Yeni `OmniTabHeader` (sola hizalı headlineMedium 26sp +
bodyMedium alt başlık, paddingStart 20 / paddingTop 16, sağda eylemler). Oyunlar
("Oyunlar" / "Ayarlarını değiştir, yedeğini al, güvenle yaz."), Profiller ("Profiller" /
"Kayıtlı ayar profillerin" + MoreVert), Ayarlar ("Ayarlar" / "Bağlantı, araçlar ve rehber").
Ortalı OmniTopBar yalnız alt ekranlarda kaldı.

**P1-9 — SignalGauge + CTA hiyerarşisi:** Hazır şebeke ikonu / artan çubuklar gitti;
3 eşit kalibrasyon segmenti (4×16dp, 2dp radius, 3dp aralık). Bağlantı yok: 1 oksit +
2 errorContainer sönük + sağda "Bağla" hapı (primaryContainer); izin gerekli: 1 pirinç +
"İzin ver"; ADB sınırlı: 2 petrol + 1 sönük; root tam: 3 petrol. Panel nötr
surfaceContainerLow. Shizuku çalışmıyorken "Oyun ekle" OmniSecondaryButton'a düşürüldü
(birincil vurgu "Bağla" hapında).

**P1-10 — FAB + oyun kartı (24b):** ExtendedFAB stadium (OmniRadius.full) +
primaryContainer/onPrimaryContainer tonuna çekildi. Oyun kartına 3. mikro satır:
"1 profil · Dengeli 60 FPS" (labelSmall, primary). `HomeViewModel.gameProfiles`
(ProfileDao akışından gerçek sayım + en son profil adı; veri modeli değişmedi).

**P1-11 — 21/23 hero tekrarı:** 21'deki 96dp hero kutusu kaldırıldı; "Dosya çekildi"
başlığı + kompakt durum satırı (CheckCircle 20dp + "Dosya okundu · 128 B · 11 satır",
success rengi). Mühür/hero yalnız 23'te (final). Betik "Dosya çekildi" needle'ı korundu.

**P2-12 — Rehber ikonları:** Tek stil: 36dp kutu, 8dp radius, surfaceContainerHigh zemin,
onSurfaceVariant ikon; rastgele renkli kutular ve GuideSection.accent kaldırıldı.
Bölüm başına 01..11 monoMicro sıra numarası (gerçek sıra — meşru). Genişleme kenarlığı
primary. Karşılaştırma bloklarının anlamlı SUCCESS/ERROR aksanları korundu.

**P2-13 — Profil analizi (04):** "Çözümle" boş girdide disabled (surfaceContainerHighest +
%38 içerik). Alanın üstünde "Panodan yapıştır" çipi (ContentPaste 18dp + labelMedium,
LocalClipboardManager). Placeholder mono: `{ "name": "...", "rules": [...] }`.

**P2-14 — Splash (00):** `tools/gen_splash_vector.py` gerçek Barlow SemiBold + JetBrains
Mono glif dış hatlarını fontTools ile Android path'e çeviriyor (elle çizme taklit yok).
72dp kalibrasyon kadranı (petrol halka + 24 tik + pirinç ibre/göbek), altında 22sp
"OmniGFX" + 11sp "CALIBRATION SUITE". Açık/koyu iki kaynak (drawable + drawable-night);
windowSplashScreenAnimatedIcon bu kilide bağlandı, daire arka plan kaldırıldı (kırpma yok).
API 26-30 windowBackground layer-list ile aynı kilidi gösteriyor.

**P2-15 — Sihirbaz sayacı:** TopAppBar sağında monoMicro "1 / 5" .. "5 / 5"; ray segmentleri
aktif 4dp / pasif 2dp. Semantics off-by-one düzeltildi ("Adım 2/5" → "Adım 1/5").

## Durum: F13 tam yeniden doğrulama + dolu durum teslimi (2026-11-14, 10. oturum)

### 10. oturum bulguları ve yapılanlar
- [x] Skill'ler okundu (tasarim-temel → tasarim-compose). CRITIQUE.md YOK (teyit).
- [x] 9. oturumun filled4 kuyruk süreci (PID 1679209) emu.lock'ta bekliyordu;
      after/21–26 hâlâ eski kısmi koşudan kalma GEÇERSIZ PNG'lerdi (24b yok).
      Yedekli koşu sonlandırıldı; yerine TEK kapsamlı doğrulama kuyruğa alındı:
      `nohup setsid flock -o build.lock bash tools/design_verify_full.sh`
      (PID 1696388, log .qwen/tmp/design-verify10.log). Sıra: clean assembleDebug
      testDebugUnitTest lintDebug assembleRelease → 22 ana görüntü → dolu durum
      oturumu (21–26 + 24b, düzeltilmiş betik). Tüm görüntüler SON APK'dan.
- [x] Kilit durumu: MobAI-draw sig13 emülatör oturumu kilidi tutuyordu; verify10
      emu.lock kuyruğunda (ledmanagerpro round2b de kuyrukta).
- [x] colors.xml'den brand_paper/brand_on_paper silinmiş (22:52, oturum öncesi);
      kaynakta referans YOK (yalnız bayat build intermediates) → build'i kırmez,
      mevcut değişiklik korundu. webp foreground silinmeleri güvenli: adaptive
      ikon @drawable vektörlerine referans veriyor (mipmap-anydpi/ic_launcher.xml).
- [x] Bağımsız kod denetimi (10. oturum): Color(0x tema dışı 0; fontSize tema dışı 0;
      yasaklı copy 0 (tek KDoc yorumu); emoji 0; sabit dp padding 0; Brush/gradyan 0.
- [x] Sürüm uyumu: AGP 9.5.1 / Kotlin 2.4.21 / Gradle 9.9.1 / KSP 2.3.12 / Hilt 2.60.1;
      CI JDK 17 + platforms;android-37.0 + build-tools;36.0.0 (API 37 kapıları sağlanır).
      gh auth: ahmetemrew ✓.
- [x] verify10 sonucu: clean assembleDebug testDebugUnitTest lintDebug assembleRelease
      BUILD SUCCESSFUL 1m42s GRADLE_RC=0; 76 test 0 fail; 22 ana görüntü + splash
      TAP_MISS_COUNT=0 CRASH_NONE VERIFY_ALL_RC=0 (log .qwen/tmp/design-verify10.log).
      Dolu oturum: Shizuku SERVER UP, 21/22/23/24/24b/25 geçerli; **26 WAIT MISS
      'omnigfx'** → FILLED_SESSION_RC=1. Emülatörler kapatıldı, CRASH_NONE.
- [x] **26 kök nedeni (kod kanıtı):** betik FPSLimit'i DEĞERİNİ DEĞİŞTİRMEDEN
      pinliyordu → serialized == original → PmpPipeline NoChange yolundan döner
      (PmpPipeline.kt ~L97, yedek oluşturmadan ÖNCE) → dosya yazılmaz, .bak
      oluşmaz, başarı ekranı yine de "Dosya uygulandı" gösterir. 23 dump'ında
      FPSLimit=60 (değişmemiş) kanıt. Çözüm: betik editörde Brightness 80→75
      yapıyor (tap_scroll "80" + MOVE_END + 2×DEL + "75"); updateNodeValue
      rawLine'ı değiştirmediği için profil "Dinamik" kalır, gerçek yazma + yedek
      oluşur. tools/design_shots_filled.sh güncellendi.
- [x] **Copy kalıntıları bulundu ve düzeltildi (25 mesaj, 7 dosya):** önceki copy
      geçişi strings.xml + ekranları kapsamış, ViewModel/engine mesajları atlanmış.
      "backup/restore/push/path/patch/package name/exception" → Türkçe:
      ConfigRollbackManager (9), BackupManagerViewModel (5), ProfileEngine (7),
      ProfileTemplateAnalyzer (6), SetupWizardViewModel (2), PmpVerifier (1),
      PmpPipeline (1). Test bağımlılığı yok (tarama temiz). İşlevsellik aynı,
      yalnız metin.
- [x] verify11: clean build+test+lint+release BUILD SUCCESSFUL 2m51s GRADLE_RC=0
      (572 görev, copy düzeltmeleri derlendi). AMA dolu oturumda `tap "Shizuku"`
      Ayarlar'daki bağlantı kartı metnine denk geldi (485,402 — satır değil),
      izin akışı çalışmadı → 'Bağlantı hazır' MISS → kaskad. Koşu durduruldu
      (geçersiz 21/22/24 PNG'leri üretildi; sonraki koşu tazeleyecek).
- [x] Betik sertleştirme: `tap "Shizuku"` → `tap "Dosya erişimi için gerekli"`
      (OmniListRow alt başlığı, benzersiz needle; iki yerde). SettingsScreen.kt
      satır yapısından doğrulandı.
- [x] verify12 YEŞİL: clean assembleDebug testDebugUnitTest lintDebug assembleRelease
      BUILD SUCCESSFUL GRADLE_RC=0; 76 test 0 fail. Dolu oturum: needle düzeltmesi
      çalıştı ('Dosya erişimi için gerekli' → satır), 'Bağlantı hazır' OK, değer
      değişikliği 80→75 OK, gerçek yazma + yedek oluştu → **WAIT OK 'omnigfx',
      SHOT 26, FILLED_TAP_MISS_COUNT=0, CRASH_NONE, FILLED_SESSION_RC=0**
      (log .qwen/tmp/design-verify12.log). Emülatör trap ile kapatıldı.
- [x] 21–26+24b geçerlilik: 7 dump TEMIZ (layout audit), 7 PNG TEMIZ (palet);
      26 dump'ında gerçek .bak adı + hedef yol + 14.11.2026 23:56 + Geri yükle /
      Yedeği sil. before/21–26 OCR eşleşmesi teyit. Ana set: 21 dump + 22 PNG TEMIZ
      (verify10, bu oturumda yeniden denetlendi). Görsel: 13/22/23/25 açıldı
      (vision bütçesi 5. görüntüde tükendi → 26 sayısal yoldan).
- [x] REVIEW.md: 7 dolu ekran puanlandı (21: 8.89, 22: 8.67, 23: 9.00, 24: 9.00,
      24b: 9.00, 25: 8.89, 26: 9.00). 29 ekran, genel ≈8.89, en düşük kriter 8.
      Eski "Shizuku emülatörde çalışmadı" notu kaldırıldı; kanıt katmanları güncellendi.
- [x] commit + push + CI yeşil + QWEN_DESIGN_DONE.md güncelleme.
      Commit'ler: 6c0cff7 fix(ui) copy · 98204e7 chore(res) ikon · c90ef91 fix(design)
      dolu durum araçları · 63cbe23 docs(design) kanıt+puan. Push fb4a4d4..63cbe23
      (fast-forward, force yok, sır/APK yok). **CI YEŞİL: run 43640757953,
      conclusion=success** (verify job, JDK 17). QWEN_DESIGN_DONE.md yeniden yazıldı
      (tüm sonuçlar bu oturumda doğrulandı). Tur TAMAM — 29 ekran, ort ≈8.89,
      en düşük kriter 8; build+test+lint+release yeşil; CRASH_NONE.

## Durum: F12 dolu durum görüntüleri — kök neden + betik düzeltmesi (2026-11-14, 9. oturum)

### 9. oturum bulguları ve yapılanlar
- [x] Skill'ler okundu (tasarim-temel → tasarim-compose → material-3-expressive).
- [x] CRITIQUE.md YOK (bağımsız eleştirmen dosyası bulunmuyor).
- [x] verify9 logdan doğrulandı: VERIFY_ALL_RC=0, TAP_MISS_COUNT=0, CRASH_NONE (22 ana
      görüntü). APK 21:31 build'i güncel (o tarihten sonra Kotlin değişikliği yok).
- [x] **after/21–26 GEÇERSİZ bulundu** (8. oturum kısmi koşularından kalma):
      23 ve 24 birebir aynı PNG (md5 eşit); 23/24/25 dump'ları "Çek ve çözümle"
      (adım 3) gösteriyor; 26 "Henüz yedek yok" (boş). Yalnız 21/22 geçerli.
- [x] Kök neden (kod + dump kanıtı): editördeki ilk "Sabitle" eşleşmesi "[Graphics]"
      bölüm başlığı satırının pini; pin sayfasının "Değişkeni kaydet" düğmesi ekran
      dışında kalınca dump'a girmiyor → tap_optional bulamıyor → sayfa açık kalıyor →
      sonraki dokunuşlar boşa → `back` sihirbazı adım 3'e düşürüyor → kaskad MISS.
- [x] tools/design_shots_filled.sh düzeltildi:
      • `dismiss_ime()`: BACK yalnız mInputShown=true ise (IME yokken gezinmeyi bozmaz).
      • Pin editörden değil adım 4'ten: "Değişkenleri ayarla" → profil adı "Dengeli 60 FPS"
        → "FPSLimit" satırı → sayfa → `tap_scroll "Değişkeni kaydet"` (sayfa içi kaydırma).
      • Her geçişte wait_for kapısı: "Profil adı", "Değişkeni yapılandır",
        "Sabitlenen değişkenler", "Dosyayı yaz ve uygula", "Dosya uygulandı", "Settings",
        "Dengeli 60 FPS", "omnigfx".
- [x] design_filled_session.sh: kardeş emülatör bekleme 120s → 15 dk (RAM baskısı:
      kardeş ledmanagerpro emülatörü aktif, 6 GB available + swap yoğun; iki AVD
      aynı anda LMK riski).
- [x] **before/24 eşleşme hatası bulundu ve düzeltildi** (OCR kanıtı): before/24
      "Shizuku hazır · ADB/shell (UID 2000)" bandı + BOŞ liste gösteriyor; before/01
      "Shizuku izni gerekli" bandı. Slug'daki "hazir" = bağlantı durumu, dolu liste değil.
      → after/24 sihirbazdan ÖNCE çekiliyor (pm clear + "Bağlantı hazır" wait_for);
      dolu liste kanıtı **24b-oyunlar-dolu-koyu** (07b gibi before karşılığı olmayan ek
      durum). SCREENS.md tablosu + audit EXPECTED (24/24b) güncellendi.
- [x] design_filled_session.sh: `pm clear` (temiz veri) + cleanup EMU_PID guard.
- [x] Vision bütçesi 4 görüntüde tükendi (before/23, 26, 24, 25) → kalan kanıt OCR
      (tesseract) + sayısal audit + palet. before/23=başarı, 25=dolu profil, 26=dolu
      yedek doğrulandı; docs/device-test PNG'leri eski kimlik ama farklı ekranlar.
- [ ] filled4 koşusu: nohup + flock -o, PID 1679209, log .qwen/tmp/design-filled4.log.
- [ ] 21–26+24b geçerlilik denetimi (dump başlıkları + audit + palet) + REVIEW.md puanlama.
- [ ] Kendi doğrulamam: clean assembleDebug testDebugUnitTest lintDebug (+release).
- [ ] commit + push + CI + QWEN_DESIGN_DONE.md güncelleme.

## Durum: F11 doğrulama + dolu durum Shizuku kök neden düzeltmesi (2026-11-14, 8. oturum)

### 8. oturum bulguları ve yapılanlar
- [x] Skill'ler okundu (tasarim-temel → tasarim-compose → material-3-expressive).
- [x] verify9 ana oturum YEŞİL: clean assembleDebug testDebugUnitTest lintDebug
      assembleRelease → BUILD SUCCESSFUL, GRADLE_RC=0; 76 test 0 fail; 22 ana görüntü
      TAP_MISS_COUNT=0, CRASH_NONE (.qwen/tmp/design-verify9.log).
- [x] Bağımsız kod denetimi: Color(0x yalnız OmniColor.kt; fontSize yalnız
      OmniTypography.kt; yasaklı copy 0 (tek yorum satırı); emoji 0; sabit dp padding 0.
- [x] Kontrastlar bağımsız yeniden hesaplandı: 22 çift, tümü eşikleri geçti
      (14.50, 8.78, 9.36, 8.58, 3.68, 15.32, ... DESIGN.md ile birebir).
- [x] Sayısal audit: 21 dump TEMIZ (RC=0); palet 22 görüntü TEMIZ.
- [x] Görsel: 01 (koyu) + 13 (açık) + 18 (büyük font) açılıp incelendi — kimlik,
      tek CTA, pirinç nav lambası, semantik ikonlar; açık tema birinci sınıf.
- [x] **Dolu durum Shizuku kök nedeni ÇÖZÜLDÜ** (7. oturumda SHIZUKU SERVER FAIL):
      1) v13.6'da resmi başlatma yolu native `libshizuku.so`'yu doğrudan çalıştırmak
         (sürüm notlarındaki komut). Eski app_process+CLASSPATH yolu `librish.so`'yu
         bulamayıp UnsatisfiedLinkError ile çöküyordu (logcat crash trace ile kanıtlandı).
         design_filled_session.sh artık official libshizuku.so komutunu üretiyor.
      2) İzin diyaloğu Shizuku task'inde açılıyor → Allow sonrası start_app ile
         OmniGFX'e deterministik dönüş; izin bloğu best-effort (wait_quiet, MISS saymaz).
      3) Uygulama seçicide "Settings" arama olmadan görünmüyor → "Uygulama ara…" +
         input text "settings" adımı eklendi.
      4) Pin ikonu AdvancedPinSheet'i açıyor; shot 22 artık sayfadan ÖNCE alınıyor,
         sonra "Değişkeni kaydet" (etiket node.key ile hazır) pin'i kaydedip kapatıyor.
- [x] Shizuku servisi emülatörde ADB modunda gerçekten çalışıyor: sihirbaz demo INI'yı
      çekti ("Dosya çekildi" WAIT OK), düzenleyici açıldı — filled3'te doğrulandı.
- [ ] filled4: düzeltilmiş betikle 6 dolu durum görüntüsü (21–26) + REVIEW.md puanlama.
- [ ] commit + push + CI + QWEN_DESIGN_DONE.md.

## Durum: F11 bağımsız yeniden doğrulama + dolu durumlar (2026-11-14, 7. oturum)

### 7. oturum hedefi
Brief yeniden koştu: önceki iddialara güvenme, kendin doğrula. CRITIQUE.md YOK.
Eksik tespit: before/ setindeki 6 dolu durumun (eski 10–15) after/ karşılığı yoktu ve
before/ numaraları after/ slug haritasıyla çelişiyordu (06/07 ters, 10–15 farklı ekran).

### Yapılanlar
- [x] Skill'ler okundu (tasarim-temel → tasarim-compose).
- [x] before/ yeniden numaralandırıldı (git mv): 06↔07 düzeltildi, 08→rehber-liste,
      10–15 → 21–26 (after/ slug haritasıyla birebir). İçerik vision ile doğrulandı
      (06=Shizuku kurulum, 07=Ayarlar, 08=rehber liste; eski kimlik mor/gradyan/şablon).
- [x] Dolu durumlar için Shizuku ADB modu yolu açıldı: tools/design_shots_filled.sh,
      tools/design_filled_session.sh, tools/design_verify_full.sh, tools/design_demo_config.ini.
      Shizuku v13.6.0 APK .qwen/tmp/shizuku.apk (gitignore'lı, commit edilmez).
      Akış: Shizuku servisi (adb start.sh) → izin → sihirbaz (Settings + demo INI) →
      çek/düzenle/pin/profil/uygula → oyunlar/profiller/yedekler dolu.
- [x] Bağımsız kod denetimi: Color(0x yalnız OmniColor.kt (60); fontSize yalnız
      OmniTypography.kt (17); yasaklı copy 0 (tek yorum satırı); emoji 0.
- [x] Kontrastlar bağımsız yeniden hesaplandı: 12 çift, tümü eşikleri geçti
      (DESIGN.md değerleriyle birebir: 14.50, 8.43, 8.78, 9.36, 8.58, 3.68, 15.32, ...).
- [x] DESIGN.md bayat cümle düzeltildi (app/ui/theme silinmişti, "eşler" yazıyordu).
- [x] CI workflow doğrulandı: setup-android packages düzeltilmiş (tools yok, android-37.0).
- [ ] verify9: clean build + test + lint + release + 22 ana görüntü + 6 dolu durum
      görüntüsü (flock kuyruğunda; kardeş Medium_Phone emülatörü bekleniyor).
- [ ] REVIEW.md: 6 yeni ekran puanlanacak; before/after eşleşme tablosu güncellenecek.
- [ ] commit + push + CI + QWEN_DESIGN_DONE.md.

## Durum: F9 tam doğrulama YEŞİL + F10 teslim (2026-11-14, 6. oturum)

### 6. oturum bulguları ve yapılanlar
- [x] Skill'ler okundu (tasarim-temel → tasarim-compose → material-3-expressive).
- [x] Bağımsız doğrulama (verify6): flock -o ile clean assembleDebug testDebugUnitTest
      lintDebug assembleRelease → BUILD SUCCESSFUL, GRADLE_RC=0 (572 görev), testler 0 fail.
- [x] verify6 emülatör: CRASH_NONE ama TAP_MISS=12 — paylaşılan Medium_Phone AVD'sinde
      kardeş oturumların bıraktığı ekran/gezinme durumu dump koordinatlarını kaydırmış
      (Profiller sekmesi 2337'de, dokunuş boşa; shots 02–09 launcher). Tasarım regresyonu DEĞİL.
- [x] Kök neden + araç sertleştirme (tools/design_shots.sh, design_emulator_session.sh):
      • wm size/density reset (kardeş override'ını sıfırlar) — alt sekme koordinatlarını düzeltti.
      • navigation_mode ZORLANMADI (zorlama üst bar overflow dokunuşunu launcher'a kaydırıyordu).
      • tap() MISS'te bir kez yeniden dener (Compose popup yakalama gecikmesi).
      • Overflow akışı deterministik: her erişim öncesi start_app + Profiller (kaskad hatası yok).
      • Emülatör oturumu aktif kardeş emülatörü kapanana dek bekler (offline sayılmaz).
      • start_app settle 4s→6s (RAM baskısı altında yavaş boot).
- [x] verify8 (sertleştirilmiş araçlar): TAP_MISS_COUNT=0, SHOTS_RC=0, CRASH_NONE.
      22 görüntü taze ve geçerli; overflow menüsü (Yedekler/Profil analizi/İçe-dışa aktar) doğru açıldı.
- [x] Sayısal kanıt (verify8): layout audit 21 dump TEMIZ (RC=0); palet 22 görüntü TEMIZ
      (koyu #121A1B / açık #F4F7F6, petrol/pirinç/oksit token'ları). Ekran belirteçleri doğru.
- [x] Görsel: 01 (koyu) + 13 (açık) açılıp incelendi — kalibrasyon tezgâhı kimliği, tek CTA,
      pirinç nav lambası, semantik ikonlar; açık tema birinci sınıf.
- [x] Kod denetimi: Color(0x yalnız OmniColor.kt; fontSize yalnız OmniTypography.kt;
      yasaklı copy 0; emoji 0.
- [x] F8 REVIEW.md: 22 ekran ort 8.56–9.00, genel ≈8.88, en düşük kriter 8 (hiçbiri <7).
- [x] F10: 4 anlamlı commit (feat ui, docs design, fix design araçları, docs kanıt) +
      push origin main (fast-forward, force yok, sır/APK yok) + CI YEŞİL
      (run 43569944917, ✓ verify 5m15s) + QWEN_DESIGN_DONE.md yazıldı. Tur TAMAM.

## Durum (önceki): F8 puanlama + F9 tam doğrulama (2026-11-14, 5. oturum)

### 5. oturum bulguları ve yapılanlar
- [x] Skill'ler okundu (tasarim-temel → tasarim-compose → material-3-expressive).
- [x] 4. oturumun verify3 koşusu 07:31'de shot 02'den sonra KESİLMİŞ (tool timeout):
      after/03–20 eski build'den (06:43), 10–12 geçersiz launcher görüntüleri.
      → Tam doğrulama yeniden başlatıldı: nohup + flock -o kuyruğu
      (tools/design_verify_all.sh, log .qwen/tmp/design-verify4.log).
      Kilitte uzun orchestration oturumu + 2 emülatör oturumu önde.
- [x] Vision bridge bu oturumda ÇALIŞIYOR (transcription): 00 splash (açık zemin +
      marka: grafit daire, petrol pill, tik rayı + pirinç topuz), 01 ve 02 incelendi.
- [x] Sayısal analiz (design_analyze.py): 00/01/02 palet TEMİZ; 01/02 dump hedef TEMİZ.
- [x] Kod denetimi: nav aktif göstergesi secondaryContainer (pirinç lamba, bilinçli,
      kontrast 8.58); touchTarget 48dp heightIn ile; AutoMirrored 41 kullanım (RTL);
      mutlak Left/Right hizalama 0; strings.xml yasaklı copy 0; OmniTheme tek kapı;
      splash v31 + night kaynakları tutarlı; AGP 9.5.1 / Kotlin 2.4.21 / Gradle 9.9.1.
- [x] F9 verify4: BUILD SUCCESSFUL (warm cache, 572 görev), 76 test 0 fail,
      lintDebug koştu, assembleRelease yeşil; emülatör: 22 shot, TAP_MISS=0, CRASH_NONE.
      after/03–20 tazelendi; 10–12 artık geçerli (palet TEMIZ).
- [x] Sayısal audit: 21 dump TEMIZ (taşma 0, hedef ≥48dp, kenar ≥20dp, copy yerinde);
      22 palet TEMIZ. 42dp "KUCUK_HEDEF" sahte pozitifi: listede kenarda kesik kart;
      audit kenar-kesik düğümleri atlar (tam boyut kodda: rehber başlık 88dp).
- [x] Copy cilası: yedek boş başlığı "Yedek yok" → "Henüz yedek yok" (aile tutarlılığı).
- [x] F8: REVIEW.md dolduruldu — 22 ekran, ort 8.56–9.00, genel ≈8.88, en düşük kriter 8.
      Kanıt: vision (00/01/02/06) + sayısal audit (tümü) + palet (tümü) + kod.
- [ ] F9 son: verify5 (copy değişikliği sonrası) kuyrukta → taze 03 + yeşil build.
- [ ] F10: commit (2 anlamlı: kod+resimler; design/+tools/) + push + CI + QWEN_DESIGN_DONE.md.

## Durum (önceki): F8 puanlama + F9 yeniden doğrulama (2026-11-14, 4. oturum)

### 4. oturum bulguları ve yapılanlar
- [x] 3. oturumun verify2 logu incelendi: BUILD SUCCESSFUL (1m40s, GRADLE_RC=0),
      76 unit test yeşil (11 XML, fail=0). AMA:
      1) `CRASH_FOUND` satırı eski betik sürümünden kalma SAHTE POZİTİF şüphesi:
         betik 06:48'de (koşu 06:43'te bitti) düzeltilmiş; güncel emu-logcat.txt'de
         0 FATAL / 0 ANR. Kesin hüküm için yeni koşu bekleniyor.
      2) Shots 10–12 GEÇERSİZ: rehber bölümü yerinde açılan akordeon → `back; back`
         uygulamadan çıkarmış, TAP MISS'ler launcher'da görüntü üretmiş.
- [x] design_shots.sh düzeltildi: shot 09 sonrası `back; back` yerine `start_app`
      (deterministik ana ekran); TAP_MISS sayacı + miss varsa exit 1.
- [x] design_emulator_session.sh: CRASH_FOUND artık exit 1; SHOTS_RC de exit'e bağlı.
      design_verify_all.sh: SESSION_RC'yi dışarı taşıyor (önceki VERIFY_EXIT=0
      crash'ı gizliyordu).
- [x] Kod iddiaları yeniden doğrulandı: Color(0x tema dışı 0, fontSize tema dışı 0,
      emoji 0. Gezinti metinleri kaynakla eşleşiyor ("Uygulama ara…", "Oyun ekle").
- [x] Görsel inceleme başladı (vision bridge): 00 splash SİYAH KARE (geçersiz),
      01/02/03 incelendi. Bulgular ve DÜZELTMELER:
      1) Splash 0.6s'de çizilmemiş kare yakalıyordu → tools/design_splash.py:
         siyah kareleri atlar, alt şerit analiziyle gerçek splash'i doğrular.
      2) Oyunlar boş: hem boş durum butonu hem FAB "Oyun ekle" (çift CTA) →
         FAB yalnızca liste doluyken (MainScreen gamesEmpty + HomeScreen
         onEmptyStateChange callback).
      3) Profiller boş: davet butonu yoktu → "Oyun ekle" birincil butonu eklendi
         (mevcut akışa gezinme, yeni özellik değil).
      4) Yedekler boş: "Eski yedekleri temizle" anlamsızdı → yalnızca yedek
         varken gösteriliyor.
- [x] shot() artık her görüntüyle uiautomator dump da kaydediyor
      (.qwen/tmp/ui-dumps/NN.xml) — sayısal hizalama/hedef boyutu kanıtı.
- [x] İmza bileşenleri kod düzeyinde doğrulandı: OmniValueReadout + OmniTickRail
      (mono anahtar, üstü çizili eski değer, pirinç yeni değer, tik rayı),
      SignalGauge (semantik renk, sade dil, contentDescription, ≥48dp, autoMirror).
      StepPushApply copy: aktif fiil, özürsüz hata, "İçerik eşleşti" sade doğrulama.
      İkon vektörü ürün malzemesinden (= operatörü + tik rayı + pirinç topuz),
      gradyan 0; nav geçişleri yönlü + reduced-motion.
- [x] Copy birleştirme: 23 ilerleme metnindeki "..." → "…" (7 dosya; Barlow'da
      glif doğrulanmıştı, test bağımlılığı yok). Kalıntı 0.
- [x] Ek denetimler: IconButton'larda contentDescription dolu (21/21), null'lar
      dekoratif satır içi ikonlarda; ekranlarda sabit dp padding 0; font 7 dosya
      464 KB yerinde; strings.xml İngilizce/ünlem kalıntısı 0; adaptive ikon +
      monokrom tüm varyantlarda; enableEdgeToEdge aktif; OmniPrimaryButton
      stadium→12dp morph + loading + touchTarget; CI JVM 17 hizalı, son main
      koşusu yeşil (43490782401).
- [x] Palet analizi (tools/design_analyze.py palette): 18 geçerli görüntünün
      TAMAMI TEMIZ — baskın renkler DESIGN.md token'ı veya token karışımı;
      koyu #121A1B / açık #F4F7F6 doğru render.
- [x] F9 build: **BUILD SUCCESSFUL in 2m22s, GRADLE_RC=0** (07:31, clean
      assembleDebug testDebugUnitTest lintDebug assembleRelease; 572 görev).
      feature-wizard testleri taze koştu (cache invalid): 9/9 yeşil; core-engine
      67 test FROM-CACHE. Emülatör oturumu aynı flock içinde sürüyor.
      Log: .qwen/tmp/design-verify3.log.
- [ ] F8: after/ görüntülerini AÇIP incele + REVIEW.md puanla (şu an tümü boş "–").
      Vision bridge bütçesi tükendi → kalan ekranlar sayısal analiz (dump + PIL)
      ve kod incelemesiyle puanlanacak; vision dönünce görsel teyit.
- [ ] F10: commit + push + CI + QWEN_DESIGN_DONE.md.

## Durum (önceki): F8/F9 doğrulama sürüyor (2026-11-14, 3. oturum)

### 3. oturumda yapılanlar (bu tur)
- [x] Skill'ler yeniden okundu (tasarim-temel → tasarim-compose → material-3-expressive).
- [x] Kod düzeyinde tasarım iddiaları DOĞRULANDI:
      `Color(0x` yalnız OmniColor.kt (60 eşleşme, tek kaynak); `fontSize=` yalnız
      OmniTypography.kt; ekranlarda sabit `padding(N.dp)` 0; sabit Color.White/Black 0;
      eski Omni* dark token referansı 0; gradyan 0; emoji 0; "Hoş geldin/Harika/Oops/Veri yok" 0.
      GuideScreen'deki 5 "→" kod bloklarında (JetBrains Mono, bilinçli).
- [x] MainActivity (edge-to-edge + core-ui tema), OmniTheme (dinamik renk kapalı),
      NavHost yönlü geçişler + reduced-motion, MainScreen (anlamsal ikonlar, etiketli FAB),
      HomeScreen, SettingsScreen (gruplu liste), ShizukuSetupScreen, StepEditScreen,
      OmniTopBar (statusBarsPadding), OmniValueReadout/TickRail, SignalGauge incelendi — uygun.
- [x] tools/design_shots.sh: Shizuku rehber diyaloğu görüntüsü eklendi (07b).
- [x] tools/design_emulator_session.sh: splash yakalama + logcat FATAL/ANR taraması +
      boot fail logu eklendi; gerçek cihaz dalı korundu.
- [x] F9 ilk koşu: clean assembleDebug testDebugUnitTest lintDebug assembleRelease
      (tek flock, tools/design_verify_all.sh) → :app:compileDebugKotlin FAIL:
      1) GuideScreen.buildGuideSections() composable değilken MaterialTheme/OmniTheme
         renklerini okuyordu (17 hata). DÜZELTME: GuideAccent enum (PRIMARY/SECONDARY/
         SUCCESS/WARNING/INFO/ERROR) + @Composable color(); GuideSection.accent ve
         Comparison.leftAccent/rightAccent artık rol taşıyor, renk çizimde çözülüyor.
         Bonus: remember içinde bayatlayan tema rengi sorunu da kalktı.
      2) ProfileSyncScreen: ikon extension property'leri fully-qualified yazılmıştı
         (ArrowBack/FileUpload/FileDownload unresolved). DÜZELTME: normal importlar.
         Not: dosya bu arada yeniden yapılandırılmış (ViewModel aynı dosyaya taşınmış);
         yalnız ikon referanslarına dokunuldu.
- [ ] F9 ikinci koşu: flock kuyruğunda (bg, log .qwen/tmp/design-verify2.log).
- [ ] F8: emülatör oturumu (build bitince tek flock çağrısı) + after/ + REVIEW.md
- [ ] F10: commit + push + CI + QWEN_DESIGN_DONE.md
- Not: CI (check.yml) assembleRelease de çalıştırıyor → doğrulamaya eklenecek.
      Release imzası yalnız OMNIGFX_KEYSTORE_PATH env varsa; CI'da unsigned, sorun değil.

## Durum (önceki): F5/F6/F7 tamam, F8/F9 doğrulama sürüyor (2026-11-14, 2. oturum)

### 2. oturumda yapılanlar (bu tur)
- [x] GuideScreen copy: ~30 "→" oku düzeltildi (ayar yolu "›", terim "—",
      eylem dizileri cümleye çevrildi); kod bloklarındaki oklar mono fontta kaldı.
      "terminal/aç", "profil'e", "import et" gibi hatalar giderildi; başlıklar
      cümle düzenine çekildi. DESIGN.md §8'e glif kararı işlendi.
- [x] GuideScreen token göçü: ~45 sabit dp/radius → OmniSpacing/OmniRadius;
      accordion animasyonu OmniMotion + azaltılmış harekete saygılı.
- [x] StepSelectFileScreen token göçü (42 sabit dp) + copy ("ADB modu" →
      "Sınırlı erişim"); seçili/hata durumları container token'larına taşındı.
- [x] NavHost'a yönlü geçişler (slide+fade, OmniMotion, reduced-motion anında).
- [x] OmniPrimaryButton loading spinner rengi onPrimary'ye düzeltildi.
- [x] ShizukuGuideDialog copy + spacing token'ları.
- [x] design/SCREENS.md envanteri + tools/design_shots.sh (metin tabanlı gezinti).
- [x] design/NOTES.md yazıldı.
- [ ] F9: clean assembleDebug testDebugUnitTest lintDebug
      (1. koşu: feature-home'da `Icons.Outlined.SportsEsports` unresolved —
      modülde yalnızca material-icons-core vardı; **material-icons-extended
      eklendi**, 2. koşu flock kuyruğunda)
- [ ] F8: after/ görüntüleri + REVIEW.md puanlama
- [ ] F10: commit + push + CI + QWEN_DESIGN_DONE.md

### Önceki oturumdan doğrulanan durum
- Renkler: `Color(0x` yalnızca core-ui/theme/OmniColor.kt'da (tek kaynak).
- Ekranlarda fontSize/sp yok; eski Omni* dark token'larına referans yok.
- Emoji yok; "UID 2000/ROOT (UID 0)/ADB/shell" yalnızca kod yorumunda.
- İkon + monokrom + splash (values-v31, values-night) yeni kimlikle çizilmiş.
- ViewModel değişiklikleri tasarım kaynaklı (copy + initialValues + label);
  testler değişen metinlere bağımlı değil.

## Tamamlananlar
- [x] F0 BRIEF.md — ürün, kullanıcı, ana iş, dünya, kişilik, imza anları
- [x] F1 DESIGN.md — 6 isimli renk, açık/koyu roller, **kontrastlar hesaplandı**
      (tüm metin ≥4.5:1, ikon/kenar ≥3:1), tipografi, şekil hiyerarşisi, hareket, copy
- [x] Fontlar temin edildi ve TÜRKÇE KAPSAMI DOĞRULANDI:
      Barlow (Regular/Medium/SemiBold/Bold) + JetBrains Mono (Regular/Medium/Bold),
      OFL, Latin+Latin-ext alt küme, **7 dosya 464 KB**, `core-ui/res/font/`.
      Lisanslar `design/licenses/`. Barlow'da ok glifi yok → metinde ok kullanılmıyor.
- [x] F3 Tema katmanı TEK KAYNAK: `core-ui/theme/` OmniColor, OmniTypography,
      OmniShape, OmniSpacing, OmniMotion, OmniTheme.
      **app/ui/theme/ (Color.kt, Type.kt, OmniGfxTheme.kt) SİLİNDİ** — çelişkili
      ikinci palet buydu; MainActivity artık core-ui temasını kullanıyor.
- [x] F4 Bileşenler: OmniButton (stadium + basılı şekil morphu, 4 varyant),
      OmniCard/OmniEmphasisPanel/OmniSectionHeader, OmniEmptyState, OmniTopBar,
      OmniGameCard, OmniSlider, OmniSwitch, OmniDropdown,
      **OmniValueReadout + OmniTickRail (İMZA 1)**,
      **OmniShizukuBar → SignalGauge (İMZA 2)**,
      OmniIndicators (OmniStatusChip/OmniTone, OmniErrorCard, OmniLoading,
      OmniListRow, OmniCodeBlock)
- [x] Copy: `core-ui/res/values/strings.xml` baştan yazıldı — İngilizce kalıntılar
      (Backups/Marketplace/Sync/Restore/Delete/Analyze/Import/Path/Value/Dry Run…)
      Türkçeleştirildi, "UID 2000" gibi iç yapı terimleri kaldırıldı,
      "Hoş Geldin" → "Önce bağlantıyı kuralım".
- [x] MainScreen: tema farkındalıklı, ikonlar düzeltildi
      (Oyunlar=SportsEsports, Profiller=Tune), FAB → etiketli "Oyun ekle",
      sekme geçişi azaltılmış harekete saygılı.
- [x] HomeScreen: gereksiz "Oyunlarım" kartı kaldırıldı → bölüm başlığı + sayaç,
      liste `key` + `animateItem`, boş durum davetkâr.
- [x] **`:core:core-ui:compileDebugKotlin` RC=0, BUILD SUCCESSFUL in 16s**
      (flock -o ile, kilit ~420s beklendi).

## Ortam notu (önemli)
Kardeş proje `dersantasi` emülatörü KULLANIYORDU (kilit dolu, 6 bekleyen, canlı
uiautomator dump). Emülatöre dokunulmadı. Şu an cihaz yok, kilit sırayla alınıyor.
Ekran görüntüleri için kendi oturumum tek flock çağrısında başlatılıp kapatılacak.

## Sıradaki iş
F5: kalan ~20 ekran. Önce mekanik token göçü (sabit Omni* dark token →
MaterialTheme.colorScheme / OmniTheme.semantic), sonra ekran bazında tasarım.


## Okunan skill'ler
- [x] `.qwen/skills/tasarim-temel/SKILL.md`
- [x] `.qwen/skills/tasarim-compose/SKILL.md`
- [x] `.qwen/skills/material-3-expressive/SKILL.md`

## Faz planı
- [ ] F0 Keşif + BRIEF.md
- [ ] F1 DESIGN.md (token sistemi) + font/lisans
- [ ] F2 SCREENS.md envanteri + `design/before/` görüntüleri
- [ ] F3 Tema/token katmanı (tek kaynak)
- [ ] F4 Ortak bileşenler
- [ ] F5 Ekranlar (en çok kullanılana göre sıra)
- [ ] F6 İmza anı + ikon + splash
- [ ] F7 Copy geçişi
- [ ] F8 `design/after/` + REVIEW.md puanlama (>=8.5 ort, hiçbir kriter <7)
- [ ] F9 build + test + lint + emülatör doğrulama
- [ ] F10 commit + push + CI yeşil + QWEN_DESIGN_DONE.md

## Keşif bulguları (ilk okuma, kanıtlı)
1. **İki ayrı tema katmanı var, değerleri ÇELİŞİYOR:**
   `core-ui/theme/OmniColor.kt` (OmniBackground `#09090B`) ve
   `app/ui/theme/Color.kt` (OmniBackgroundDark `#0B0F14`).
   `OmniTypography` ve `OmniShapes` her ikisinde de kopya. → tek kaynağa indirilecek.
2. **Açık tema fiilen bozuk:** `MainScreen`, `OmniGameCard`, `OmniShizukuBar`
   renkleri `MaterialTheme.colorScheme` yerine sabit `Omni*` dark tokenlarından
   alıyor → light scheme seçilse bile ekranlar koyu kalıyor.
3. **AI-slop (tasarim-temel §2) mevcut haliyle:**
   - madde 2: neredeyse siyah zemin (`#09090B`) + fosforlu yeşil (`#00E5A8`) / mor (`#7C4DFF`)
   - madde 3: mor gradyan ikon placeholder (`OmniPrimary → OmniPrimaryGlow`)
   - madde 4: her kart aynı 20dp radius + aynı kenarlık
   - madde 5: `"Shizuku hazır • ADB/shell (UID 2000)` orta noktalı meta dizisi
   - madde 11: varsayılan mor ailesi, şablon paleti
4. **Tipografi:** özel font YOK (sistem Roboto), hiçbir stilde `lineHeight` yok,
   `FontWeight` ekranlarda `.copy()` ile tekrar tekrar eziliyor.
5. **Anlamsal ikon hatası:** alt barda Oyunlar=`Icons.Default.Home`,
   Profiller=`Icons.Default.Person`; boş durumda `Icons.Default.Settings`.
6. **Gereksiz kart:** HomeScreen'de sadece "Oyunlarım" başlığını saran `OmniCard`.
7. **Copy:** "UID 2000", "ROOT (UID 0)" gibi iç yapı terimleri son kullanıcıya gösteriliyor.

## Sıradaki iş
F0 BRIEF.md yaz, font temin edilebilirliğini doğrula (ağ + Latin-ext/Türkçe),
sonra F1 DESIGN.md.
