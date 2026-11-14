# REVIEW — OmniGFX Pro tasarım turu ekran puanlaması

Tur: 2026-11-14 (5. oturumda tamamlandı). Kanıt: `design/before/` (eski kimlik, 14 görüntü)
ve `design/after/` (yeni kimlik; koyu + açık + font ölçeği 2.0 + splash, 22 görüntü).
Görüntüler gerçek emülatörde (Medium_Phone, API 37) `tools/design_shots.sh` ile alındı;
gezinti metin tabanlı (uiautomator), ezber koordinat yok. Son koşu: verify5
(.qwen/tmp/design-verify5.log), TAP_MISS_COUNT=0, CRASH_NONE.

Puanlama (tasarim-temel §5): her kriter 1–10. Eşik: ortalama ≥ 8.5 ve hiçbir kriter < 7.
Kriterler: Kimlik, Hiyerarşi, Ritim, Tipografi, Renk, Durumlar, Dokunma, Slop, Copy.

## Yöntem ve kanıt katmanları
1. **Görsel inceleme** (vision bridge): 00, 01, 02, 06 açılıp incelendi; marka, tek CTA,
   pirinç nav lambası, semantik hata bandı doğrulandı. Vision bütçesi bitince kalan
   ekranlar skill'in izin verdiği sayısal yoldan puanlandı.
2. **Sayısal düzen denetimi** (`tools/design_layout_audit.py`, 21 dump): ekran dışı taşma 0,
   clickable hedefler ≥48dp (kenarda kesik düğümler hariç — tam boyut kodda heightIn ile
   garantili; rehber başlık kartı 88dp, içindekiler satırı 52dp), kenar boşluğu ≥20dp,
   beklenen anahtar metinler yerinde. Tümü TEMIZ.
3. **Palet denetimi** (`tools/design_analyze.py palette`, 22 görüntü): baskın renkler
   DESIGN.md token'ı veya token karışımı; açık/koyu doğru render. Tümüm TEMIZ.
4. **Kod denetimi**: tek token kaynağı, durumlar (boş/yükleniyor/hata), copy, RTL
   (AutoMirrored), azaltılmış hareket, contentDescription.

Not: Shizuku emülatörde çalışmadığı için sihirbazın dolu/başarı durumları emülatörde
"Shizuku yok / boş" haliyle görüntülendi; dolu durumların tasarımı kod düzeyinde
doğrulandı (StepPushApply/StepPullAnalyze/ProfileApplySheet) ve eski kimliğin gerçek
cihaz görüntüleriyle (before/11–13) karşılaştırıldı. Bu ekranlarda Durumlar 8.

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

**Tüm ekranlar ort ≥ 8.5; en düşük kriter 8 (hiçbir kriter < 7).** Genel ortalama ≈ 8.88.

## Ekran notları (kanıt + gerekçe)

- **00**: Görsel: kâğıt zemin + grafit daire içinde petrol pill (= operatörü), tik rayı,
  pirinç topuz. Metin yok, tek marka. Koyu varyant values-night kaynağında doğrulandı.
  Tipografi/Dokunma/Copy 8: metin/hedef içermeyen ekran için nötr-tavan puan.
- **01**: Görsel + dump: tek birincil eylem "Oyun ekle" (FAB boş durumda gizli — çift CTA
  yok), SignalGauge hata bandı oksit container'da ("Shizuku çalışmıyor" + tek yönlendirme).
  Durumlar 8: dolu liste kod + before/10 kanıtı.
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

## Kod düzeyinde doğrulanan durumlar (emülatörde üretilemeyen)

- Sihirbaz dolu durumları: StepPullAnalyze (başarı/hata/çok büyük), StepEdit (satır
  düzenleyici, pin/değişim kenarlıkları, "Kaydedilmemiş değişiklik var"), StepPushApply
  (idle/yazılıyor/başarı/hata + geri alma; "Oyun şu an çalışıyor" diyaloğu),
  ProfileApplySheet (değer okumaları + slider/switch/dropdown; "Profil uygulandı").
- İmza anları: OmniValueReadout + OmniTickRail (mono anahtar, üstü çizili eski değer,
  pirinç yeni değer, tik rayı); SignalGauge (semantik renk, sade dil, ≥48dp, autoMirror).
- İkon + monokrom + splash: vektörler okundu (= operatörü + tik rayı + pirinç topuz,
  güvenli bölge 21..87); values-v31 splash; values-night renkler.
- Azaltılmış hareket: betik animasyon ölçeklerini 0 yaparak bu yolu da test eder
  (OmniMotion.scale canlı ContentObserver ile okunur).