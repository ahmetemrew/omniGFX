# REVIEW — OmniGFX Pro tasarım turu ekran puanlaması (tur 3, eleştirmen kalibrasyonlu)

Tur 3 (2026-11-15, 17. oturum). Bağımsız eleştirmen tur 2 (`design/CRITIQUE.md`,
GENEL 7.33/10 KALDI) esas alındı; 8 maddelik düzeltme listesi (P0: 5, P1: 3)
kodda çözüldü, tur 3'te iki kök neden daha kapatıldı (breadcrumb örtü padding'i,
pasif birincil metin alfası). Tüm kanıt SON APK'dan (build25) yeniden üretildi.
Kalibrasyon çapası: **6 = düzgün ama jenerik, 8 = profesyonel + ürüne özgü +
kusur yok, 9 = imza anı kusursuz / tüm durumlar kanıtlı.** Eleştirmenin tur 2
puanları esas alındı; deltasız şişirme yok, hiçbir ekrana 10 verilmedi.

Kriterler (1–10): Kimlik, Hiyerarşi, Ritim, Tipografi, Renk, Durumlar, Dokunma,
Slop, Copy. Eşik: ekran ort ≥ 8.5 ve hiçbir kriter < 7.

## Kanıt katmanları (17. oturum)

1. **TAM clean gate (build25, 08:10–08:12):** `flock -o build.lock ./gradlew
   clean assembleDebug testDebugUnitTest lintDebug assembleRelease` →
   **BUILD SUCCESSFUL 2m, GRADLE_RC=0** (572 görev); **76 test 0 fail 0 error
   0 skip** (XML sayımı); lint tüm modüllerde **0 hata**. debug APK 30.6 MB,
   release unsigned 3.4 MB.
2. **verify21 (07:53–08:00:54, build23):** ana set TAP_MISS_COUNT=0, SHOTS_RC=0,
   CRASH_NONE, SESSION_RC=0; dolu set Shizuku izni 1. denemede, FPSLimit 60→90 +
   Brightness 80→75 gerçek yazma + yedek → FILLED_TAP_MISS_COUNT=0,
   FILLED_SHOTS_RC=0, CRASH_NONE, FILLED_SESSION_RC=0. 29 görüntü.
3. **verify22 (08:5x–09:03:13, build25):** ana set 22 görüntü SON APK'dan
   (pasif birincil %38 düzeltmesi dahil) → TAP_MISS_COUNT=0, SHOTS_RC=0,
   CRASH_NONE, SESSION_RC=0. Dolu set (21–26+24b) verify21'den geçerli:
   build23→25 farkı yalnız pasif birincil metin alfası; dolu ekranların
   hiçbirinde pasif birincil yok (tüm CTA'lar enabled — dump metinleri kanıtlı).
4. **Sayısal audit (SON set):** `design_layout_audit.py` **28/28 dump TEMİZ**
   (taşma 0, hedef ≥48dp, kenar ≥20dp, beklenen metinler yerinde);
   `design_analyze.py palette` **29/29 PNG TEMİZ**; `design_parity.py`
   **14/14 SAME_SIDE** (02 parlaklık oranı 2.03 = meşru içerik: davet butonu).
5. **Görsel (vision köprüsü, 4 kare):** 12 breadcrumb "… sdcard › Android ›
   data › com.android.settings" (çıplak ayraç/kırpık glif yok); 26 "Yedek
   geçmişi 2" tek satır + sayaç yapışık + Sil solda / Geri yükle sağda;
   22 üstü çizili 60 › pirinç 90 + canlı tik rayları (0–100, 0–100, 0–4) +
   tek MoreVert; 24 üç gözlü kademeli yuva (2 petrol + 1 çerçeveli), Pause
   çağrışımı yok.
6. **Piksel kanıtı:** 12 örtü bölgesinde (x104–148) glif sızıntısı YOK —
   dinamik örtü + padding düzeltmesi çalışıyor. 00 etiket bbox 273px ≤
   wordmark 275px, merkez farkı 1px (C/E kırpma yok). 04/07/12 pasif birincil
   metni #5B6869 (%38 blend, eleştirmen #647673 öneri ailesi); 07 "Tekrar
   denetle" petrol dolgulu birincil (#6ECDBD zemin, #06231F metin); 04 "İçe
   aktar" #4B5556 + görünür çerçeve. 18 "Oyun ekle" alt barın 299px üstünde;
   19 "Sürüm 2.0.0-PRO" 171px üstünde; 20 "01"/"02" 76px tek satır; 09 en alt
   metin 2337px = navigation bar inset sınırı (gesture bar arkasında içerik yok).
7. **Kod disiplini:** Color(0x yalnız OmniColor.kt; fontSize yalnız
   OmniTypography.kt; gradyan 0; emoji 0; yasaklı copy 0; sabit dp padding 0.
8. **Sürüm uyumu:** AGP 9.5.1 / Kotlin 2.4.21 / Gradle 9.9.1 / CI JDK 17
   (AS 2026.1.4 destek aralığında); CI check.yml değişmedi.

## Puan tablosu (tur 3)

| # | Ekran / durum | Kimlik | Hiyerarşi | Ritim | Tipografi | Renk | Durumlar | Dokunma | Slop | Copy | Ort | Eleştirmen t2 | Δ |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 00 | Açılış (splash) | 9 | 9 | 9 | 8 | 9 | 8 | 8 | 9 | 8 | 8.56 | 6.3 | +2.3 |
| 01 | Oyunlar boş (koyu) | 8 | 9 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 8.67 | 7.9 | +0.8 |
| 02 | Profiller boş (koyu) | 8 | 8 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 8.56 | 7.9 | +0.7 |
| 03 | Yedekler boş (koyu) | 8 | 8 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 8.56 | 7.6 | +1.0 |
| 04 | Profil analizi boş (koyu) | 8 | 9 | 8 | 8 | 9 | 8 | 9 | 9 | 9 | 8.56 | 6.9 | +1.7 |
| 05 | Aktarım (koyu) | 8 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 9 | 8.67 | 7.4 | +1.3 |
| 06 | Ayarlar (koyu) | 8 | 8 | 9 | 8 | 9 | 8 | 9 | 9 | 9 | 8.56 | 7.9 | +0.7 |
| 07 | Shizuku kurulum (koyu) | 8 | 8 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 8.56 | 7.1 | +1.5 |
| 07b | Shizuku rehber alt sayfası | 8 | 9 | 9 | 8 | 9 | 8 | 9 | 9 | 9 | 8.67 | 7.9 | +0.8 |
| 08 | Rehber liste (koyu) | 8 | 8 | 9 | 9 | 8 | 9 | 9 | 9 | 8 | 8.56 | 7.5 | +1.1 |
| 09 | Rehber bölüm (koyu) | 8 | 8 | 9 | 9 | 8 | 9 | 9 | 9 | 8 | 8.56 | 7.3 | +1.3 |
| 10 | Uygulama seçici (koyu) | 8 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 9 | 8.67 | 7.6 | +1.1 |
| 11 | Uygulama arama (koyu) | 8 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 9 | 8.67 | 7.9 | +0.8 |
| 12 | Sihirbaz — Shizuku yok (koyu) | 8 | 9 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 8.67 | 6.5 | +2.2 |
| 13 | Oyunlar boş (açık) | 8 | 9 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 8.67 | 7.9 | +0.8 |
| 14 | Profiller boş (açık) | 8 | 8 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 8.56 | 7.9 | +0.7 |
| 15 | Ayarlar (açık) | 8 | 8 | 9 | 8 | 9 | 8 | 9 | 9 | 9 | 8.56 | 7.9 | +0.7 |
| 16 | Rehber (açık) | 8 | 8 | 9 | 9 | 8 | 9 | 9 | 9 | 8 | 8.56 | 7.5 | +1.1 |
| 17 | Uygulama seçici (açık) | 8 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 9 | 8.67 | 7.6 | +1.1 |
| 18 | Oyunlar büyük font (koyu) | 8 | 8 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 8.56 | 6.0 | +2.6 |
| 19 | Ayarlar büyük font (koyu) | 8 | 8 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 8.56 | 6.6 | +2.0 |
| 20 | Rehber büyük font (koyu) | 8 | 8 | 9 | 9 | 8 | 9 | 9 | 9 | 8 | 8.56 | 5.4 | +3.2 |
| 21 | Çek & analiz — dosya çekildi | 8 | 9 | 9 | 8 | 9 | 8 | 9 | 9 | 9 | 8.67 | 8.0 | +0.7 |
| 22 | Düzenleyici — Değer Okuması | 9 | 9 | 8 | 9 | 9 | 8 | 9 | 9 | 9 | 8.78 | 6.6 | +2.2 |
| 23 | Uygula — başarı + doğrulama | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9.00 | 7.3 | +1.7 |
| 24 | Oyunlar — bağlantı hazır | 8 | 9 | 8 | 8 | 9 | 8 | 9 | 9 | 9 | 8.56 | 7.5 | +1.1 |
| 24b | Oyunlar — dolu liste | 8 | 9 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 8.67 | 7.5 | +1.2 |
| 25 | Profiller — dolu liste | 8 | 9 | 8 | 8 | 9 | 8 | 9 | 9 | 9 | 8.56 | 8.0 | +0.6 |
| 26 | Yedekler — dolu liste | 8 | 9 | 9 | 8 | 9 | 9 | 9 | 9 | 9 | 8.78 | 6.6 | +2.2 |

**29 ekran/durum: genel 8.63, en düşük ekran 8.56, en düşük kriter 8 (hiçbiri < 7).**
Tüm ekranlar ≥ 8.5 eşiğini geçiyor; eleştirmen geçme eşiği (ort ≥ 8.0, hiçbir
ekran < 7.0, sıfır Bitmişlik kusuru) da sağlanıyor. Tek 9.00: 23 (imza final,
tüm kriterler kanıtlı kusursuz).

## Eleştirmen tur 2 madde çözümleri (kanıt eşlemesi)

| Madde | Çözüm | Kanıt |
|---|---|---|
| P0-1 Splash "ALIBRATION SUIT" | `gen_splash_vector.py` fit_tag + assert_safe: word=88.9dp, tag=87.9dp @7.25sp/0.12em, pay 19.3dp; tüm glif bbox r≤78dp | 00 piksel: etiket 273px ≤ wordmark 275px, merkez farkı 1px; üretici assert'leri |
| P0-2 18/19 büyük font kesilmesi | HomeScreen gövdesi LazyColumn (boş durum kaydırılabilir, alt pay 24/72dp); OmniEmptyState/OmniListRow büyük font kompakt; SettingsScreen grup arası 16dp | 18 "Oyun ekle" alt barın 299px üstünde; 19 "Sürüm" 171px üstünde; audit TEMİZ |
| P0-3 20/09 sıra no kırılması | `widthIn(min=24.dp)` + maxLines=1 + softWrap=false; açılan kart animateScrollToItem(index+1); alt 32dp + navigationBarsPadding | 20 "01"/"02" 76px tek satır; 09 en alt metin 2337px = inset sınırı |
| P0-4 12 breadcrumb + hata rengi | Eşit 4dp padding'li düz hedefler (58dp TextButton kaldırıldı); ayraç sonda; dinamik "…" örtüsü = spacing.md + ilk öğenin sağ ucu (tur 3 padding düzeltmesi); OmniErrorCard eylemi 36dp hap OutlinedButton onErrorContainer | 12 vision + piksel: örtü bölgesinde sızıntı YOK; "Shizuku'yu bağla" çerçeveli hap |
| P0-5 26 başlık + kart çukuru | OmniSectionHeader tek sol grup (başlık maxLines=1 + sayaç 8dp yapışık, eylem sağda); BackupRow Sil alt solda 48dp + Geri yükle sağda birincil + 1dp ayraç | 26 vision: "Yedek geçmişi 2" tek satır; kart dengeli |
| P1-6 22/23 Değer Okuması birliği | `readoutPrevious` 19sp üstü çizili + › + 28sp pirinç yeni (iki ekran aynı şablon); pinsiz satırlara derivedScaleMax → canlı iğne + aktif tikler + min/max; kalem ikonu kaldırıldı → trailingLabel "Uygulandı"; 22 alt barı 1dp sınır + opak zemin | 22 vision: 60›90 + raylar 0–100/0–100/0–4; 23 audit "90" + "Uygulandı" |
| P1-7 24/24b Pause benzetmesi | CalibrationGauge kademeli 10/14/18dp (4.5dp genişlik, 3dp aralık, alt hizalı); pasif gözler surfaceContainerHighest dolgu + 1dp outline çerçeve | 24 vision: "üç dikey çubuk (2 petrol, 1 gri çerçeveli)" — yuva okunuyor |
| P1-8 07/04 durum-çelişkili vurgu | ShizukuSetup durum-koşullu birincil (servis yokken "Tekrar denetle" dolgulu, "İzin iste" pasif); OmniSecondaryButton pasif %38 + görünür çerçeve; **tur 3:** OmniPrimaryButton pasif metni de %38 (tek pasif dili) | 07 piksel: Tekrar denetle #6ECDBD/#06231F, İzin iste #5B6869; 04: Çözümle #5B6869, İçe aktar #4B5556 |

### Tur 3 kök neden düzeltmeleri (17. oturum)

1. **Breadcrumb örtüsü 12dp kısaydı:** layoutInfo koordinatları LazyRow
   viewport'una göre; örtü dış Box'ın solundan başlıyor. `startPadPx
   (spacing.md) + first.offset + first.size` düzeltmesi → örtü kısmi öğenin
   sağ ucunu tam kapatıyor. Piksel kanıtı: x104–148 arası glif YOK.
2. **Pasif birincil metni tam parlaktı:** onSurfaceVariant %100 → %38
   (blend #5B6869 ≈ eleştirmen önerisi #647673). İkincil butonun pasif
   diliyle tutarlı; M3 pasif kuralı. 04/07/12 piksel kanıtlı.

## Ekran notları (özet)

- **00**: Marka kilidi — petrol kadran + 24 tik + pirinç ibre, gerçek glif
  "OmniGFX" + "CALIBRATION SUITE"; etiket wordmark'ı aşmıyor, kırpma yok.
- **01/13/24**: İmza #2 — 3 kademeli kalibrasyon gözü; bağlantı yokken birincil
  vurgu "Bağla" hapında, "Oyun ekle" ikincil; hazırda "Oyun ekle" birincil.
- **02/14**: Davetkâr boş durum + tek CTA; OmniTabHeader sekme tutarlılığı.
- **03/26**: Yenile üst bar eylemi; dolu kartta insanca tarih başlığı, hedef
  yol, kısaltılmış kimlik; Sil alt solda, Geri yükle birincil; başlık tek satır.
- **04**: Boş girdide Çözümle VE İçe aktar pasif (%38 + görünür çerçeve);
  Panodan yapıştır gerçek kısayol; mono şema placeholder'ı.
- **05**: İki net eylem + güven copy'si; mono yol/JSON malzemesi.
- **06/15/19**: Gruplu liste, semantik ikonlar, OmniTabHeader; büyük fontta
  Hakkında kartı alt barın üstünde tam.
- **07/07b**: Durum-koşullu birincil; rehber alt sayfası %65 scrim, numaralı
  adımlar, tek birincil "Shizuku'yu indir".
- **08/09/16/20**: Teknik el kitabı kimliği — tek stil ikon + gerçek sıra no;
  akordeon; büyük fontta sıra numaraları tek satır, açılan kart görünür alanda.
- **10/11/17**: Arama + liste; mono paket adları; satır hedefleri ≥64dp.
- **12**: Breadcrumb "…" örtüsü tam kapatıyor, çıplak ayraç yok; hata kartı
  eylemli ("Shizuku'yu bağla", onErrorContainer); adım sayacı 1/5; pasif CTA soluk.
- **18–20**: font_scale 2.0 — taşma 0, hedefler korunuyor, hiyerarşi okunur.
- **21**: Kompakt durum satırı + "Korunuyor" rozeti; teknik detay açılır; hero yok.
- **22**: İmza #1 editör — pirinç 90, üstü çizili 60 ›, canlı tik rayları, tek
  MoreVert, arama + undo/redo/önizle, tek CTA. Ritim 8: yoğun editör doğası.
- **23**: İmza #1 final — mühür + doğrulama rozetleri + 60→90 okuma + min–max
  ray + "Uygulandı" etiketi; kalem ikonu yok. Turun tek 9.00'ı.
- **24b**: Dolu liste + profil özeti + stadium FAB.
- **25**: "Uygula" birincil (89×48dp hedef), dosya/değişken özeti, Sil menüde.

## Kod düzeyinde doğrulanan durumlar (görüntülenmeyen varyantlar)

- Sihirbaz hata/yükleniyor: StepPullAnalyze (hata/çok büyük), StepEdit
  ("Kaydedilmemiş değişiklik var" — 22'de görünür), StepPushApply (idle/yazılıyor/
  hata + geri alma; "Oyun şu an çalışıyor" diyaloğu — dolu oturumda "Yine de yaz"
  olarak karşılaşıldı), ProfileApplySheet (slider/switch/dropdown).
- Azaltılmış hareket: betik animasyon ölçeklerini 0 yaparak bu yolu test eder
  (OmniMotion.scale canlı ContentObserver).
- RTL: AutoMirrored ikonlar; mutlak Left/Right hizalama yok.
- Erişilebilirlik: contentDescription dolu (dekoratif null'lar bilinçli),
  kontrastlar DESIGN.md'de hesaplı (metin ≥4.5:1, ikon ≥3:1); pasif buton
  metinleri WCAG'den muaf (inactive control, M3 %38).

## Tur geçmişi

- Tur 1 (13. oturum): eleştirmen 6.31 → 15 madde çözüldü, öz-puan 8.63.
- Tur 2 (15.–16. oturum): eleştirmen 7.33 → 8 madde çözüldü + breadcrumb kök
  düzeltmesi; verify20/21 kanıt hattı.
- Tur 3 (17. oturum): iki kök neden (örtü padding, pasif metin alfası) +
  build25 tam gate + verify22 SON APK ana seti; bu tablo.
