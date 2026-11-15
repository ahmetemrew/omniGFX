# REVIEW — OmniGFX Pro tasarım turu ekran puanlaması (tur 2, eleştirmen kalibrasyonlu)

Tur 2 (2026-11-15, 13. oturum). Bağımsız eleştirmen tur 1 (`design/CRITIQUE.md`,
GENEL 6.31/10 KALDI) esas alındı; 15 maddelik düzeltme listesi kodda çözüldü ve
SON APK'dan alınan görüntülerle madde madde doğrulandı. Bu tablo eleştirmen
çapasıyla yeniden kalibre edildi: **6 = düzgün ama jenerik, 8 = profesyonel +
ürüne özgü öğeler var + kusur yok, 9 = imza anı kusursuz / tüm durumlar kanıtlı.**
Tur 1'deki kendi puanlarım (~8.89, kalibre değildi) ve eleştirmenin 6.31'i
arasındaki fark, kusurların giderilmesiyle kapatıldı; hiçbir ekrana 10 verilmedi.

Kriterler (1–10): Kimlik, Hiyerarşi, Ritim, Tipografi, Renk, Durumlar, Dokunma,
Slop, Copy. Eşik: ekran ort ≥ 8.5 ve hiçbir kriter < 7.

## Kanıt katmanları (bu oturum)

> **14. oturum yeniden doğrulaması (2026-11-15 04:35–04:44):** Tüm kanıt sıfırdan
> yeniden üretildi — build19 (clean+test+lint+release, GRADLE_RC=0, 76 test 0 fail,
> lint 0 hata) ve verify19 (ana set TAP_MISS=0 + dolu set FILLED_TAP_MISS=0, iki
> oturumda CRASH_NONE). 29 görüntü SON APK'dan; layout 28/28, palet 29/29 TEMİZ;
> before/after 14/14 aynı tema tarafı (köşe piksel #121A1B). 03/01/25 vision ile
> yeniden incelendi. Puanlar değişmedi (görüntüler aynı kodun aynı durumları).

1. **Build gate (03:33):** `flock -o build.lock ./gradlew clean assembleDebug
   testDebugUnitTest lintDebug assembleRelease` → BUILD SUCCESSFUL 2m12s,
   GRADLE_RC=0; **76 test 0 fail 0 error 0 skip**; lint 0 hata (1 warning:
   UnusedResources ic_launcher_foreground). APK 30.5 MB, 03:33.
2. **Dolu durum oturumu (verify17, 04:04):** Shizuku v13.6 ADB modu (resmi native
   `libshizuku.so`), izin 1. denemede doğrulandı ("Bağlantı hazır"), sihirbaz uçtan
   uca: çek → Değer Okuması editöründe FPSLimit 60→90 + Brightness 80→75 (IME Done)
   → pin → profil → yaz → yedek. **FILLED_TAP_MISS_COUNT=0, CRASH_NONE.** 21–26+24b
   son APK'dan; 26'daki yedek uygulamanın kendi yazma akışının ürünü.
3. **Ana set (verify17 + main18):** 22 görüntü; ilk koşuda 2 MISS (ilk overflow
   erişimi start_app korumasızdı → 03 launcher), betik düzeltildi, main18 ile temiz
   set. TAP_MISS_COUNT=0, CRASH_NONE.
4. **Sayısal audit:** `design_layout_audit.py` 29 dump TEMİZ (taşma 0, hedef ≥48dp,
   kenar ≥20dp, beklenen metinler yerinde). Araç sahte pozitifteri düzeltildi:
   long-clickable substring, yatay kesik breadcrumb dilimi, content-desc kesik
   imzası. `design_analyze.py palette` 29 PNG TEMİZ (scrim türevleri anlamsal
   katman olarak tanımlandı).
5. **Görsel (vision bridge):** 00 splash (kadran + gerçek glif wordmark; etiket
   bbox'ı piksel düzeyinde vektörle eşleşti), 22 editör (üstü çizili 60 › pirinç 90,
   tik rayı, tek MoreVert menüsü, 3/5 sayacı), 23 başarı (60→90 okuma, min–max ray,
   mühür yalnız finalde) açılıp incelendi.
6. **Dump kanıtı (17/17 kontrol):** İçindekiler kaldırıldı + 01 sıra no; bottom
   sheet + numaralı adımlar + rehber butonu; › ayraç + "Shizuku'yu bağla" + jargon
   yok; "Korunuyor" + teknik detay gizli + SELinux yok + kompakt durum; Uygula
   89×48dp + dosya özeti; Bağla hapı; Panodan yapıştır; 1/5 sayacı.
7. **Scrim piksel ölçümü (07b):** (6,9,9) = bg(18,26,27)×0.35 → tam %65.
8. **before/after denkliği:** 14 çiftin 14'ü de aynı tema tarafı (koyu) ve aynı
   durumda; parlaklık oranları ≤2.0 (kimlik değişimi kaynaklı doğal fark).
9. **Kod denetimi:** Color(0x yalnız OmniColor.kt; fontSize yalnız
   OmniTypography.kt; gradyan 0; emoji 0; yasaklı copy 0 (tek KDoc); sabit dp
   padding 0; Color.White/Black yalnız scrim (bilinçli, P0-4).

## Puan tablosu

| # | Ekran / durum | Kimlik | Hiyerarşi | Ritim | Tipografi | Renk | Durumlar | Dokunma | Slop | Copy | Ort | Eleştirmen t1 | Δ |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 00 | Açılış (splash) | 9 | 9 | 9 | 8 | 9 | 8 | 8 | 9 | 8 | 8.56 | 5.4 | +3.2 |
| 01 | Oyunlar boş (koyu) | 8 | 9 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 8.67 | 7.0 | +1.7 |
| 02 | Profiller boş (koyu) | 8 | 8 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 8.56 | 6.6 | +1.9 |
| 03 | Yedekler boş (koyu) | 8 | 8 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 8.56 | 5.5 | +3.1 |
| 04 | Profil analizi boş (koyu) | 8 | 9 | 8 | 8 | 9 | 8 | 9 | 9 | 9 | 8.56 | 5.6 | +3.0 |
| 05 | Aktarım (koyu) | 8 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 9 | 8.67 | 6.9 | +1.8 |
| 06 | Ayarlar (koyu) | 8 | 8 | 9 | 8 | 9 | 8 | 9 | 9 | 9 | 8.56 | 7.1 | +1.5 |
| 07 | Shizuku kurulum (koyu) | 8 | 8 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 8.56 | 6.4 | +2.2 |
| 07b | Shizuku rehber alt sayfası | 8 | 9 | 9 | 8 | 9 | 8 | 9 | 9 | 9 | 8.67 | 5.4 | +3.3 |
| 08 | Rehber liste (koyu) | 8 | 8 | 9 | 9 | 8 | 9 | 9 | 9 | 8 | 8.56 | 5.4 | +3.2 |
| 09 | Rehber bölüm (koyu) | 8 | 8 | 9 | 9 | 8 | 9 | 9 | 9 | 8 | 8.56 | 5.8 | +2.8 |
| 10 | Uygulama seçici (koyu) | 8 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 9 | 8.67 | 7.1 | +1.6 |
| 11 | Uygulama arama (koyu) | 8 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 9 | 8.67 | 7.1 | +1.6 |
| 12 | Sihirbaz — Shizuku yok (koyu) | 8 | 9 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 8.67 | 5.3 | +3.4 |
| 13 | Oyunlar boş (açık) | 8 | 9 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 8.67 | 7.0 | +1.7 |
| 14 | Profiller boş (açık) | 8 | 8 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 8.56 | 6.6 | +1.9 |
| 15 | Ayarlar (açık) | 8 | 8 | 9 | 8 | 9 | 8 | 9 | 9 | 9 | 8.56 | 7.1 | +1.5 |
| 16 | Rehber (açık) | 8 | 8 | 9 | 9 | 8 | 9 | 9 | 9 | 8 | 8.56 | 5.4 | +3.2 |
| 17 | Uygulama seçici (açık) | 8 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 9 | 8.67 | 7.1 | +1.6 |
| 18 | Oyunlar büyük font (koyu) | 8 | 8 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 8.56 | 6.5 | +2.1 |
| 19 | Ayarlar büyük font (koyu) | 8 | 8 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 8.56 | 6.9 | +1.7 |
| 20 | Rehber büyük font (koyu) | 8 | 8 | 9 | 9 | 8 | 9 | 9 | 9 | 8 | 8.56 | 5.1 | +3.5 |
| 21 | Çek & analiz — dosya çekildi | 8 | 9 | 9 | 8 | 9 | 8 | 9 | 9 | 9 | 8.67 | 6.4 | +2.3 |
| 22 | Düzenleyici — Değer Okuması | 9 | 9 | 8 | 9 | 9 | 8 | 9 | 9 | 9 | 8.78 | 5.4 | +3.4 |
| 23 | Uygula — başarı + doğrulama | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9.00 | 6.9 | +2.1 |
| 24 | Oyunlar — bağlantı hazır | 8 | 9 | 8 | 8 | 9 | 8 | 9 | 9 | 9 | 8.56 | 7.3 | +1.3 |
| 24b | Oyunlar — dolu liste | 8 | 9 | 8 | 8 | 9 | 9 | 9 | 9 | 9 | 8.67 | 6.6 | +2.1 |
| 25 | Profiller — dolu liste | 8 | 9 | 8 | 8 | 9 | 8 | 9 | 9 | 9 | 8.56 | 5.5 | +3.1 |
| 26 | Yedekler — dolu liste | 8 | 9 | 9 | 8 | 9 | 9 | 9 | 9 | 9 | 8.78 | 5.6 | +3.2 |

**29 ekran/durum: genel 8.63, en düşük ekran 8.56, en düşük kriter 8 (hiçbiri < 7).**
Tüm ekranlar ≥ 8.5 eşiğini geçiyor.

## Eleştirmen tur 1 madde çözümleri (kanıt eşlemesi)

| Madde | Çözüm | Kanıt |
|---|---|---|
| P0-1 Değer Okuması editörde yok | ConfigLineItem = ValueReadoutRow (mono anahtar, üstü çizili eski › pirinç yeni, tik rayı); yerinde düzenleme; tek MoreVert menüsü | 22 vision + dump; `StepEditScreen.kt` |
| P0-1 Başarı okuması yarım | `originalValue` dosyadan; 60→90 pirinç okuma 28sp; min–max mikro etiketleri | 23 vision; `ConfigTreeBuilder` 5 nokta; audit "90" |
| P0-2 Profilde birincil eylem yok | 36dp stadium "Uygula" (dokunma 89×48dp), Sil taşma menüsünde, monoMicro dosya/değişken özeti | 25 dump bounds; `ProfileListScreen.kt` |
| P0-3 Rehber çift liste + kesilme | İçindekiler kaldırıldı; akordeon tek liste; contentPadding + navigationBarsPadding; doğal sarma | 08/09/16/20 dump (İçindekiler yok, 01 no); audit TEMİZ |
| P0-4 Diyalog taşması + zayıf scrim | ModalBottomSheet tam genişlik, scrim %65 (piksel (6,9,9)); 24dp numaralı rozetler; rehber butonu Adım 2 içinde | 07b dump + piksel ölçümü |
| P0-5 Kesik breadcrumb + jargon + eylemsiz hata | LazyRow otomatik son segmente kayar; "›" ayraç; "Shizuku'yu bağla" eylemi; "process" → "servis yanıt vermedi" | 12 dump (›, bağla, jargon yok) |
| P0-6 SELinux/UID/octal açıkta | "Korunuyor" rozeti + insani açıklama; ham değerler "Teknik detayları göster" arkasında | 21 dump (u:object_r yok) |
| P0-7 Yüzen Yenile + .bak başlık | Yenile TopAppBar ikonu; tarih/saat başlık + hedef yol + "#<ts> · .bak"; Sil sağ üst, Geri yükle birincil | 26 dump (15 Kasım 2026 · 04:03, yol, .bak) |
| P1-8 Sekme başlıkları tutarsız | OmniTabHeader üç sekmede: sola hizalı headlineMedium + alt başlık + sağ eylemler | 01/02/06 dump; `OmniTabHeader.kt` |
| P1-9 Hazır şebeke ikonu + yanlış CTA | 3 kalibrasyon segmenti (oksit/pirinç/petrol); "Bağla"/"İzin ver" hapı; Oyun ekle ikincil | 01/13/24 dump; `OmniShizukuBar.kt` |
| P1-10 FAB şekli + kart zayıf | ExtendedFAB stadium + primaryContainer; oyun kartında "1 profil · Dengeli 60 FPS" | 24b dump; `HomeViewModel.gameProfiles` |
| P1-11 Ara adım finali taklit ediyor | 21'de hero yok, kompakt durum satırı; mühür yalnız 23'te | 21/23 vision + dump |
| P2-12 Rastgele renkli rehber ikonları | Tek stil 36dp/8dp/surfaceContainerHigh/nötr ikon + 01..11 gerçek sıra no | 08 dump; `GuideScreen.kt` |
| P2-13 Çözümle boşken aktif | enabled=false + "Panodan yapıştır" çipi + mono placeholder | 04 dump; `ProfileMarketplaceScreen.kt` |
| P2-14 Kimliksiz splash | 72dp kalibrasyon kadranı + fontTools gerçek glif Barlow wordmark + mono etiket; açık/koyu ayrı kaynak | 00 vision + bbox piksel eşleşmesi; `gen_splash_vector.py` |
| P2-15 Dilsiz ilerleme çubuğu | Üst bar sağında "n / 5" monoMicro; aktif 4dp / pasif 2dp segment | 12/22/23 dump ("1 / 5", "3 / 5", "5 / 5") |

## Ekran notları (özet)

- **00**: Marka kilidi — petrol kadran + 24 tik + pirinç ibre, gerçek glif
  "OmniGFX" + "CALIBRATION SUITE". Etiket bbox'ı vektörle piksel düzeyinde eşleşti
  (x 362–721, beklenen 360.8–719.5). Daire arka plan yok, kırpma yok.
- **01/13/24**: İmza #2 — 3 segment kalibrasyon göstergesi; bağlantı yokken tek
  birincil vurgu "Bağla" hapında, "Oyun ekle" ikincil. Üç bağlantı durumu
  (yok/izin/hazır) üç ekranda kanıtlı.
- **02/14**: Davetkâr boş durum + tek CTA; OmniTabHeader ile sekme tutarlılığı.
- **03/26**: Yenile üst bar eylemi; boş durum davet; dolu kartta insanca tarih
  başlığı, hedef yol, kısaltılmış kimlik; Geri yükle birincil.
- **04**: Boş girdide Çözümle disabled; Panodan yapıştır gerçek kısayol; mono
  şema placeholder'ı.
- **05**: İki net eylem + güven copy'si; mono yol/JSON malzemesi.
- **06/15/19**: Gruplu liste (Bağlantı/Araçlar/Hakkında), semantik ikonlar,
  OmniTabHeader; büyük fontta düzen korunuyor.
- **07/07b**: Kurulum adım kartları + durum paneli; rehber alt sayfası %65 scrim,
  numaralı adımlar, tek birincil "Shizuku'yu indir".
- **08/09/16/20**: Teknik el kitabı kimliği — tek stil ikon + gerçek sıra
  numaraları; akordeon; kod blokları JetBrains Mono; büyük fontta kesilme yok.
- **10/11/17**: Arama + liste; mono paket adları; satır hedefleri ≥64dp.
- **12**: Breadcrumb otomatik kaydırma + ›; hata kartı eylemli ("Shizuku'yu
  bağla"); adım sayacı 1/5.
- **18–20**: font_scale 2.0 — taşma 0, hedefler korunuyor, hiyerarşi okunur.
- **21**: Kompakt durum satırı + "Korunuyor" rozeti; teknik detay açılır; hero yok.
- **22**: İmza #1 editörün kendisi — pirinç 90, üstü çizili 60 ›, tik rayı, tek
  MoreVert, arama + undo/redo/önizle, tek CTA. Ritim 8: yoğun editör doğası.
- **23**: İmza #1 final — mühür + doğrulama rozetleri + 60→90 okuma + min–max ray;
  tek stadium CTA. Turun tek 9.00'ı: tüm kriterler kanıtlı kusursuz.
- **24b**: Dolu liste + profil özeti + stadium FAB (kartın önüne geçmiyor).
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
  kontrastlar DESIGN.md'de hesaplı (metin ≥4.5:1, ikon ≥3:1).