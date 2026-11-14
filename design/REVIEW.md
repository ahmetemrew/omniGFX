# REVIEW — OmniGFX Pro tasarım turu ekran puanlaması

Tur: 2026-11-14 (10. oturumda dolu durum setiyle tamamlandı). Kanıt: `design/before/`
(eski kimlik, 14 görüntü) ve `design/after/` (yeni kimlik; koyu + açık + font ölçeği 2.0
+ splash + dolu durumlar, 29 görüntü). Görüntüler gerçek emülatörde (Medium_Phone, API 37)
`tools/design_shots.sh` (ana set) ve `tools/design_shots_filled.sh` (dolu set, Shizuku
ADB modu + demo INI) ile alındı; gezinti metin tabanlı (uiautomator), ezber koordinat yok.
Son koşular: verify10 (ana set, .qwen/tmp/design-verify10.log) ve verify12 (dolu set,
.qwen/tmp/design-verify12.log) — ikisinde de TAP_MISS_COUNT=0, CRASH_NONE. Araçlar
paylaşılan emülatör yarışına karşı sertleştirildi (wm reset, tap retry, deterministik
overflow, benzersiz needle'lar).

Puanlama (tasarim-temel §5): her kriter 1–10. Eşik: ortalama ≥ 8.5 ve hiçbir kriter < 7.
Kriterler: Kimlik, Hiyerarşi, Ritim, Tipografi, Renk, Durumlar, Dokunma, Slop, Copy.

## Yöntem ve kanıt katmanları
1. **Görsel inceleme** (vision bridge): 00, 01, 02, 06 (önceki oturumlar) + 13, 22, 23, 25
   (10. oturum) açılıp incelendi; marka, tek CTA, pirinç nav lambası, semantik hata bandı,
   tik raylı Değer Okuması, açık tema birinci sınıf doğrulandı. Vision bütçesi bitince
   kalan ekranlar skill'in izin verdiği sayısal yoldan puanlandı.
2. **Sayısal düzen denetimi** (`tools/design_layout_audit.py`, 28 dump: 21 ana + 7 dolu):
   ekran dışı taşma 0, clickable hedefler ≥48dp (kenarda kesik düğümler hariç — tam boyut
   kodda heightIn ile garantili; rehber başlık kartı 88dp, içindekiler satırı 52dp), kenar
   boşluğu ≥20dp, beklenen anahtar metinler yerinde. Tümü TEMIZ (10. oturumda yeniden koşuldu).
3. **Palet denetimi** (`tools/design_analyze.py palette`, 29 görüntü): baskın renkler
   DESIGN.md token'ı veya token karışımı; açık/koyu doğru render. Tümü TEMIZ.
4. **Kod denetimi**: tek token kaynağı, durumlar (boş/yükleniyor/hata), copy, RTL
   (AutoMirrored), azaltılmış hareket, contentDescription.
5. **Dolu durum kanıtı (10. oturum)**: Shizuku v13.6 ADB modunda emülatörde çalıştırıldı
   (resmi native `libshizuku.so` yolu); demo INI seed edildi, sihirbaz uçtan uca koşuldu
   (çek → düzenle → değer değişikliği → pin → profil → uygula → yedek). Yedek dosyası
   (.UserCustom.ini.omnigfx.<ts>.bak) uygulamanın kendisi tarafından oluşturuldu; 26
   dump'ında gerçek dosya adı + hedef yol + zaman damgası + "Geri yükle"/"Yedeği sil"
   eylemleri doğrulandı. before/21–26 eşleşmesi OCR ile teyit edildi.

## Puan tablosu

| # | Ekran / durum | Kimlik | Hiyerarşi | Ritim | Tipografi | Renk | Durumlar | Dokunma | Slop | Copy | Ort |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 00 | Açılış (splash) | 9 | 9 | 9 | 8 | 9 | 8 | 8 | 10 | 8 | 8.67 |
| 01 | Oyunlar boş (koyu) | 9 | 9 | 9 | 9 | 9 | 8 | 9 | 10 | 9 | 9.00 |
| 02 | Profiller boş (koyu) | 9 | 9 | 9 | 9 | 9 | 8 | 9 | 10 | 9 | 9.00 |
| 03 | Yedekler boş (koyu) | 8 | 8 | 9 | 9 | 9 | 8 | 9 | 10 | 9 | 8.78 |
| 04 | Profil analizi boş (koyu) | 8 | 9 | 9 | 9 | 9 | 8 | 9 | 10 | 9 | 8.89 |
| 05 | Aktarım (koyu) | 8 | 9 | 9 | 9 | 9 | 8 | 9 | 10 | 9 | 8.89 |
| 06 | Ayarlar (koyu) | 9 | 9 | 9 | 9 | 9 | 8 | 9 | 10 | 9 | 9.00 |
| 07 | Shizuku kurulum (koyu) | 9 | 9 | 9 | 9 | 9 | 8 | 9 | 10 | 9 | 9.00 |
| 07b | Shizuku rehber diyaloğu | 8 | 9 | 9 | 9 | 9 | 8 | 9 | 10 | 9 | 8.89 |
| 08 | Rehber liste (koyu) | 8 | 9 | 9 | 9 | 9 | 8 | 9 | 10 | 9 | 8.89 |
| 09 | Rehber bölüm (koyu) | 8 | 9 | 9 | 9 | 9 | 8 | 9 | 10 | 8 | 8.78 |
| 10 | Uygulama seçici (koyu) | 8 | 9 | 9 | 9 | 9 | 8 | 9 | 10 | 9 | 8.89 |
| 11 | Uygulama arama (koyu) | 8 | 9 | 9 | 9 | 9 | 8 | 9 | 10 | 9 | 8.89 |
| 12 | Sihirbaz — Shizuku yok (koyu) | 8 | 8 | 9 | 9 | 9 | 8 | 9 | 10 | 9 | 8.78 |
| 13 | Oyunlar boş (açık) | 9 | 9 | 9 | 9 | 9 | 8 | 9 | 10 | 9 | 9.00 |
| 14 | Profiller boş (açık) | 9 | 9 | 9 | 9 | 9 | 8 | 9 | 10 | 9 | 9.00 |
| 15 | Ayarlar (açık) | 9 | 9 | 9 | 9 | 9 | 8 | 9 | 10 | 9 | 9.00 |
| 16 | Rehber (açık) | 8 | 9 | 9 | 9 | 9 | 8 | 9 | 10 | 9 | 8.89 |
| 17 | Uygulama seçici (açık) | 8 | 9 | 9 | 9 | 9 | 8 | 9 | 10 | 9 | 8.89 |
| 18 | Oyunlar büyük font (koyu) | 9 | 9 | 8 | 8 | 9 | 8 | 9 | 10 | 9 | 8.78 |
| 19 | Ayarlar büyük font (koyu) | 9 | 9 | 8 | 8 | 9 | 8 | 9 | 10 | 9 | 8.78 |
| 20 | Rehber büyük font (koyu) | 8 | 9 | 8 | 8 | 9 | 8 | 9 | 10 | 8 | 8.56 |
| 21 | Çek & analiz — dosya çekildi (koyu) | 8 | 9 | 9 | 9 | 9 | 8 | 9 | 10 | 9 | 8.89 |
| 22 | Düzenleyici — satırlar + pin (koyu) | 9 | 8 | 8 | 9 | 9 | 8 | 8 | 10 | 9 | 8.67 |
| 23 | Uygula — başarı + doğrulama (koyu) | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 10 | 9 | 9.00 |
| 24 | Oyunlar — Shizuku hazır, boş liste (koyu) | 9 | 9 | 9 | 9 | 9 | 8 | 9 | 10 | 9 | 9.00 |
| 24b | Oyunlar — dolu liste (koyu) | 8 | 9 | 9 | 9 | 9 | 9 | 9 | 10 | 9 | 9.00 |
| 25 | Profiller — dolu liste (koyu) | 8 | 9 | 9 | 9 | 9 | 8 | 9 | 10 | 9 | 8.89 |
| 26 | Yedekler — dolu liste (koyu) | 8 | 9 | 9 | 9 | 9 | 9 | 9 | 10 | 9 | 9.00 |

**Tüm ekranlar ort ≥ 8.5; en düşük kriter 8 (hiçbir kriter < 7).** 29 ekran/durum,
genel ortalama ≈ 8.89.

## Ekran notları (kanıt + gerekçe)

- **00**: Görsel: kâğıt zemin + grafit daire içinde petrol pill (= operatörü), tik rayı,
  pirinç topuz. Metin yok, tek marka. Koyu varyant values-night kaynağında doğrulandı.
  Tipografi/Dokunma/Copy 8: metin/hedef içermeyen ekran için nötr-tavan puan.
- **01**: Görsel + dump: tek birincil eylem "Oyun ekle" (FAB boş durumda gizli — çift CTA
  yok), SignalGauge hata bandı oksit container'da ("Shizuku çalışmıyor" + tek yönlendirme).
  Durumlar 8: dolu liste artık 24b'de emülatör kanıtı.
- **02**: Görsel + dump: davetkâr boş durum + tek CTA; pirinç nav lambası Profiller'de.
- **03**: Dump: "Henüz yedek yok" + otomatik yedek açıklaması; "Eski yedekleri temizle"
  yalnızca yedek varken (anlamsız eylem yok). Yenile tek ikincil eylem.
- **04**: Form-boş durumu = davet metni + mono JSON alanı + "Çözümle" birincil,
  "İçe aktar" çözümleme izin verene dek disabled (hiyerarşi net). Hata kartı + loading var.
- **05**: İki net eylem (dışa/içe), mekanizma ve güven copy'si ("oyun dosyalarına otomatik
  yazılmaz"), bulut hesabı gerekmez notu. Loading + hata metni var.
- **06**: Görsel: gruplu liste (Bağlantı/Araçlar/Hakkında), cümle düzeni başlıklar,
  semantik ikonlar, sakin alt metinler. Slop 0.
- **07**: SignalGauge + adım listesi + tek birincil eylem; rehber diyaloğu (07b) neden +
  tek eylem + Kapat.
- **08/09/16/20**: Rehber: içindekiler satırları 52dp, başlık kartları 88dp; akordeon
  OmniMotion + reduced-motion; kod blokları JetBrains Mono, yatay kaydırma (uzun yol/hash
  kesilmez). 09 Copy 8: yoğun teknik içerik, sözlük ve callout'larla yapılandırılmış.
- **10/11/17**: Seçici: arama alanı + liste; arama durumu dump'ta ("Settings" sonuçları);
  satır hedefleri ≥64dp.
- **12**: Sihirbaz Shizuku yok: uyarı + yönlendirme, adım göstergesi gerçek sıra.
- **13–17**: Açık tema birinci sınıf: kâğıt zemin, petrol birincil, pirinç ikincil;
  palet TEMIZ, kontrastlar DESIGN.md'de hesaplı (metin ≥4.5, ikon ≥3).
- **18–20**: font_scale 2.0: dump denetimi taşma 0, hedefler korunuyor, başlık hiyerarşisi
  okunur. Ritim/Tipografi 8: 2× ölçekte yoğunluk doğal; düzen kırılmıyor.
- **21**: Dump + palet: "Dosya çekildi" başarı durumu + dosya bilgisi kartı (yol, biçim,
  satır sayısı mono), tek birincil eylem "Düzenleyiciye geç". Durumlar 8: hata/çok büyük
  dosya varyantları kod düzeyinde (StepPullAnalyze).
- **22**: Görsel + dump: satır numaraları mono, [Graphics] bölüm başlığı petrol, değer
  alanları, satır eylemleri (Yukarı/Aşağı/Altına ekle/Sil — Sil oksit), arama alanı,
  geri al/ileri al/önizle, tek CTA "Değişkenleri ayarla". Hiyerarşi/Ritim 8: yoğun ama
  ızgaralı editör doğası; hedefler ≥48dp audit'te doğrulandı. Kimlik 9: config malzemesi
  doğrudan arayüz olmuş (= operatörü dünyası).
- **23**: Görsel + dump: imza anı — Değer Okuması (FPSLimit + tik rayı), petrol doğrulama
  rozetleri ("İçerik eşleşti", "İzinler ve sahiplik korundu"), adım göstergesi (4 petrol
  + 1 pirinç), tek stadium CTA "Ana ekrana dön". Durumlar 9: başarı + doğrulama + özet;
  hata/geri alma kod düzeyinde. success=petrol semantiği DESIGN.md §1 ile birebir.
- **24**: Dump + palet: SignalGauge "Bağlantı hazır" (başarı durumu) + boş liste daveti;
  before/24 ile birebir durum (OCR: "Shizuku hazır · ADB/shell (UID 2000)" + boş liste).
  Durumlar 8: diğer bağlantı durumları 01/07/12'de.
- **24b**: Dump + palet: dolu oyun listesi (Settings kartı), FAB etiketli "Oyun ekle"
  (liste doluyken görünür — tek CTA korunur). before karşılığı yok (ek durum, 07b gibi).
- **25**: Görsel + dump: profil kartı "Dengeli 60 FPS" + "Settings" + petrol "Dinamik"
  rozeti + sil ikonu; pirinç nav lambası Profiller'de. Durumlar 8: boş durum 02'de.
- **26**: Dump + palet: gerçek yedek kartı — mono dosya adı
  (.UserCustom.ini.omnigfx.<ts>.bak), hedef yol, zaman damgası (14.11.2026 23:56),
  "Geri yükle" birincil + "Yedeği sil" ikonu; "Eski yedekleri temizle" yalnızca yedek
  varken; profil filtresi üstte. Yedek uygulamanın kendi yazma akışıyla oluştu (NoChange
  kök nedeni düzeltildi). Durumlar 9: boş durum 03'te tasarlanmış.

## Kod düzeyinde doğrulanan durumlar (emülatörde görüntülenmeyen varyantlar)

- Sihirbaz hata/yükleniyor varyantları: StepPullAnalyze (hata/çok büyük), StepEdit
  ("Kaydedilmemiş değişiklik var"), StepPushApply (idle/yazılıyor/hata + geri alma;
  "Oyun şu an çalışıyor" diyaloğu), ProfileApplySheet (slider/switch/dropdown;
  "Profil uygulandı"). Dolu/başarı durumları artık emülatörde görüntülendi (21–26).
- İmza anları: OmniValueReadout + OmniTickRail (mono anahtar, üstü çizili eski değer,
  pirinç yeni değer, tik rayı) — 23'te görsel doğrulandı; SignalGauge (semantik renk,
  sade dil, ≥48dp, autoMirror) — 01/13/24'te doğrulandı.
- İkon + monokrom + splash: vektörler okundu (= operatörü + tik rayı + pirinç topuz,
  güvenli bölge 21..87); values-v31 splash; values-night renkler.
- Azaltılmış hareket: betik animasyon ölçeklerini 0 yaparak bu yolu da test eder
  (OmniMotion.scale canlı ContentObserver ile okunur).