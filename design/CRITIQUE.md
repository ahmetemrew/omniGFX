# Eleştiri turu 2 (2026-11-15 05:03:57)

### Ön Değerlendirme: 1. Turdan (`round-1.md`) Bugüne Ne Değişti?

Birinci eleştiri turunda işaret edilen yapısal sorunların önemli bir kısmı bu sürümde gerçekten ele alınmış:
- **Çözülenler:** Üç ana sekmenin (`Oyunlar`, `Profiller`, `Ayarlar`) üst başlık şablonu eşitlenmiş; `25-profiller-dolu` ekranına birincil **"Uygula"** hap (stadium) butonu ve `UserCustom.ini · 2 değişken` teknik satırı eklenmiş; `21-cek-analiz` ekranındaki dev hero kutusu kompakt durum satırına indirilip ham UID/SELinux/izin dizeleri **"Korunuyor"** rozeti ve **"Teknik detayları göster ▾"** altına alınmış; `08-rehber-liste` ekranındaki mükerrer "İçindekiler" kutusu ve rastgele renkli ikonlar kaldırılıp `01..07` numaralı teknik dizine geçilmiş; `07b` diyaloğu tam genişlikli alt sayfaya dönüştürülmüş; `03-yedekler-bos` ekranında havada yüzen "Yenile" butonu üst bara taşınmış.

Ancak **Apple Design Award** titizliğinde ekranlara tek tek bakıldığında, yeni uygulamalar sırasında ortaya çıkan **doğrudan Bitmişlik (kesik/taşan metin, dikey kırılan rakam, yarıda kesilen buton)** kusurları ve **İmza Anı** tutarsızlıkları yüzünden ürün henüz geçme eşiğini (`≥ 8.0` ortalama, hiçbir ekran `< 7`, sıfır Bitmişlik kusuru) karşılamıyor:
1. **`00-splash.png`:** Marka alt başlığı olan `CALIBRATION SUITE` metninin solundaki `C` ve sağındaki `E` harfleri giyotin gibi kesilmiş; ekranda **`ALIBRATION SUIT`** yazıyor.
2. **Büyük Font (`18`, `19`, `20`) ekranlarında kırılma ve kesilmeler:** `18-oyunlar-buyukfont-koyu.png` ekranında alttaki **"Oyun ekle"** butonu kaydırma olmadığı için alt navigasyon çubuğu tarafından yatayda ortadan ikiye biçilmiş; `20-rehber-buyukfont-koyu.png` ekranında sol kenardaki iki haneli sıra numaraları (`01`, `02`) dar sütun yüzünden alt alta kırılıp **`0` / `1`** ve **`0` / `2`** şeklinde dikey dizilmiş.
3. **`12-sihirbaz-shizuku-yok-koyu.png` & `26-yedekler-dolu-koyu.png`:** Sihirbazdaki dosya yolu çubuğu soldan kesilip sahipsiz bir `› sdcard` ayracıyla başlıyor ve `data` kelimesinin iki yanında açıklanamayan `~24dp` boşluk var; Yedekler ekranında ise `"Yedek geçmişi"` başlığı yanında dev bir boşluk varken iki satıra (`Yedek / geçmişi`) kırılmış ve `1` rakamı ekranın ortasında tek başına yüzüyor.
4. **`22-duzenleyici-koyu.png` & `24-oyunlar-hazir-koyu.png` (İmza Anları):** Düzenleyicideki tik rayları (`FPSLimit=90`, `ResolutionScale=100`, `ShadowQuality=2`) değere göre hiç hareket etmeyen, aktif pirinç iğnesi ve min–max etiketleri olmayan ölü/statik birer tarak çizimi olarak kalmış (`23-uygula-basari` ekranındaki canlı tik rayıyla uyuşmuyor). Ana ekrandaki Shizuku göstergesinde ise (`24`, `24b`) 3. sönük segment (`#2E3B3A`) kart zemininde (`#1A2425`) görünmez olduğu için yanan 2 petrol çubuk (`||`) doğrudan **"Duraklat (Pause ⏸)"** ikonuna benziyor.

---

### 1) Ekran Başına Puan Tablosu (1–10)

> **Not:** *Kimlik* = `BRIEF.md`'deki "Kalibrasyon tezgâhı" dünyası; *Ritim* = Ritim/hizalama/boşluk; *AI-slop* = 10 puan şablon/slop izi yok demektir. Sıradan ama düzgün ekran = 6; 8+ yalnızca ürüne özgü ve kusursuz ekranlara verilmiştir.

| # | Ekran (`design/after/`) | Kimlik | Hiyerarşi | Ritim | Tipografi | Renk | Durumlar | Bitmişlik | AI-slop | **Ort.** |
|---|---|---|---|---|---|---|---|---|---|---|
| 00 | [00-splash.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/00-splash.png) | 7 | 7 | 6 | 5 | 8 | 6 | 3 | 8 | **6.3** |
| 01 | [01-oyunlar-bos-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/01-oyunlar-bos-koyu.png) | 7 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | **7.9** |
| 02 | [02-profiller-bos-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/02-profiller-bos-koyu.png) | 7 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | **7.9** |
| 03 | [03-yedekler-bos-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/03-yedekler-bos-koyu.png) | 6 | 8 | 8 | 8 | 8 | 7 | 8 | 8 | **7.6** |
| 04 | [04-marketplace-bos-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/04-marketplace-bos-koyu.png) | 7 | 6 | 7 | 8 | 7 | 6 | 6 | 8 | **6.9** |
| 05 | [05-aktarim-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/05-aktarim-koyu.png) | 6 | 7 | 7 | 8 | 8 | 7 | 8 | 8 | **7.4** |
| 06 | [06-ayarlar-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/06-ayarlar-koyu.png) | 7 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | **7.9** |
| 07 | [07-shizuku-kurulum-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/07-shizuku-kurulum-koyu.png) | 7 | 6 | 7 | 8 | 8 | 6 | 7 | 8 | **7.1** |
| 07b | [07b-shizuku-rehber-diyalog-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/07b-shizuku-rehber-diyalog-koyu.png) | 7 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | **7.9** |
| 08 | [08-rehber-liste-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/08-rehber-liste-koyu.png) | 7 | 8 | 7 | 8 | 8 | 7 | 7 | 8 | **7.5** |
| 09 | [09-rehber-bolum-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/09-rehber-bolum-koyu.png) | 7 | 7 | 7 | 8 | 8 | 7 | 6 | 8 | **7.3** |
| 10 | [10-uygulama-secici-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/10-uygulama-secici-koyu.png) | 7 | 7 | 8 | 8 | 8 | 7 | 8 | 8 | **7.6** |
| 11 | [11-uygulama-arama-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/11-uygulama-arama-koyu.png) | 7 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | **7.9** |
| 12 | [12-sihirbaz-shizuku-yok-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/12-sihirbaz-shizuku-yok-koyu.png) | 7 | 7 | 5 | 7 | 6 | 7 | 5 | 8 | **6.5** |
| 13 | [13-oyunlar-bos-acik.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/13-oyunlar-bos-acik.png) | 7 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | **7.9** |
| 14 | [14-profiller-bos-acik.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/14-profiller-bos-acik.png) | 7 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | **7.9** |
| 15 | [15-ayarlar-acik.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/15-ayarlar-acik.png) | 7 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | **7.9** |
| 16 | [16-rehber-acik.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/16-rehber-acik.png) | 7 | 8 | 7 | 8 | 8 | 7 | 7 | 8 | **7.5** |
| 17 | [17-uygulama-secici-acik.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/17-uygulama-secici-acik.png) | 7 | 7 | 8 | 8 | 8 | 7 | 8 | 8 | **7.6** |
| 18 | [18-oyunlar-buyukfont-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/18-oyunlar-buyukfont-koyu.png) | 7 | 7 | 5 | 6 | 8 | 4 | 3 | 8 | **6.0** |
| 19 | [19-ayarlar-buyukfont-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/19-ayarlar-buyukfont-koyu.png) | 7 | 7 | 6 | 7 | 8 | 5 | 5 | 8 | **6.6** |
| 20 | [20-rehber-buyukfont-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/20-rehber-buyukfont-koyu.png) | 6 | 6 | 4 | 4 | 8 | 4 | 3 | 8 | **5.4** |
| 21 | [21-cek-analiz-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/21-cek-analiz-koyu.png) | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | **8.0** |
| 22 | [22-duzenleyici-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/22-duzenleyici-koyu.png) | 7 | 6 | 6 | 7 | 8 | 6 | 5 | 8 | **6.6** |
| 23 | [23-uygula-basari-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/23-uygula-basari-koyu.png) | 8 | 7 | 7 | 7 | 8 | 7 | 6 | 8 | **7.3** |
| 24 | [24-oyunlar-hazir-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/24-oyunlar-hazir-koyu.png) | 6 | 8 | 8 | 8 | 7 | 8 | 7 | 8 | **7.5** |
| 24b | [24b-oyunlar-dolu-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/24b-oyunlar-dolu-koyu.png) | 6 | 8 | 8 | 8 | 7 | 8 | 7 | 8 | **7.5** |
| 25 | [25-profiller-dolu-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/25-profiller-dolu-koyu.png) | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | **8.0** |
| 26 | [26-yedekler-dolu-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/26-yedekler-dolu-koyu.png) | 7 | 7 | 5 | 7 | 8 | 6 | 5 | 8 | **6.6** |

---

### 2) Genel Ortalama

- **Kriter Ortalamaları:**
  - **Kimlik:** 6.9 / 10
  - **Hiyerarşi:** 7.4 / 10
  - **Ritim / Hizalama / Boşluk:** 7.1 / 10
  - **Tipografi:** 7.6 / 10
  - **Renk:** 7.8 / 10
  - **Durumlar:** 7.1 / 10
  - **Bitmişlik:** 6.8 / 10
  - **AI-slop:** 8.0 / 10
- **GENEL ORTALAMA: 7.33 / 10** *(1. Tur: 6.31 / 10 → 2. Tur: 7.33 / 10. Geçme eşiği: Ortalama ≥ 8.0, hiçbir ekran < 7.0 ve sıfır Bitmişlik sorunu).*

---

### 3) Öncelik Sırasıyla Somut Düzeltme Listesi

#### P0 — Doğrudan "Bitmişlik" Kusurları (Kesik Metin, Kırılan Rakam, Taşan/Çakışan Öğeler)

1. **[00-splash.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/00-splash.png) — Marka alt başlığı sağdan ve soldan kesilmiş (`"ALIBRATION SUIT"`):**
   - **Ne yanlış:** `OmniGFX` başlığının altındaki `CALIBRATION SUITE` yazısı, splash ikonunun dairesel/kare kırpma sınırına (safe zone) çarptığı için baştaki `C` harfinin yarısı ve sondaki `E` harfinin tamamı kesilmiş; ekranda **`ALIBRATION SUIT`** okunuyor.
   - **Nasıl düzelir:** Tüm marka kilidi (kadran + `OmniGFX` + `CALIBRATION SUITE`) splash ikon alanının kırpılmayan güvenli çemberi (`~160dp` çap) içine sığacak şekilde ölçeklenmeli: alt başlık genişliği `OmniGFX` kelimesinin genişliğini (`~112dp`) kesinlikle aşmamalı (`fontSize: 9.5sp–10sp`, `letterSpacing: 0.12em`), böylece `C` ve `E` harflerinin sağında ve solunda en az `16dp` güvenli pay kalmalı.

2. **[18-oyunlar-buyukfont-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/18-oyunlar-buyukfont-koyu.png) & [19-ayarlar-buyukfont-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/19-ayarlar-buyukfont-koyu.png) — Büyük fontta alt navigasyon çubuğunun buton ve kartları ortadan ikiye kesmesi:**
   - **Ne yanlış:**
     - `18-oyunlar-buyukfont-koyu.png` ekranında boş durum bloğu dikey kaydırılamadığı ve yüksekliği sabit/sıkışık kaldığı için en alttaki `"Oyun ekle"` butonunun alt yarısı alt bar tarafından kesilmiş (`Oyun ekle` harflerinin alt %40'ı yok).
     - `19-ayarlar-buyukfont-koyu.png` ekranında en alttaki `"Hakkında › OmniGFX"` kartının `(i)` ikonu ortadan kesilmiş, `"Sürüm 2.0.0-PRO"` alt satırı tamamen alt barın altında kalmış; üstelik kart alt bara sıfır boşlukla yapışmış.
   - **Nasıl düzelir:**
     - `Oyunlar` (boş durum dahil) ve `Ayarlar` ekranlarının içerik alanı dikey kaydırılabilir olmalı ve alt barın üst sınırından itibaren en az `24dp` (`bottom padding = navigationBarHeight + 24dp`) iç boşluk bırakılmalı.
     - `Oyunlar` boş durumunda font büyüdüğünde (`fontScale > 1.15`), oyun kolu ikonu ile `"Henüz oyun eklemedin"` arasındaki boşluk `24dp`'den `12dp`'ye, açıklama ile `"Oyun ekle"` butonu arasındaki boşluk `24dp`'den `16dp`'ye daraltılarak `"Oyun ekle"` butonunun (`height >= 48dp`) tek ekranda bile tam görünmesi sağlanmalı.

3. **[20-rehber-buyukfont-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/20-rehber-buyukfont-koyu.png) & [09-rehber-bolum-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/09-rehber-bolum-koyu.png) — Sıra numaralarının (`01`, `02`) dikeyde `0 / 1` diye iki satıra kırılması ve alt kenar kesilmesi:**
   - **Ne yanlış:**
     - `20-rehber-buyukfont-koyu.png` ekranında bölüm kartlarının solundaki `01` ve `02` (`monoMicro`) numaralarının kutusu sabit dar genişlikte bırakıldığı için, büyük fontta rakamlar karakter bazında alt alta kırılmış: üstte `0`, altında `1` (`0\n1` ve `0\n2`) yazıyor!
     - Aynı ekranda 2. kartın alt başlığı (`"OmniGFX'in çalışması için gereken temel"`) `"araç"` kelimesi olmadan kesilmiş. `09-rehber-bolum-koyu.png` ekranında ise açılan 02 numaralı kartın alt kenar çizgisi sistem hareket çubuğunun (`gesture bar`) arkasında kesiliyor.
   - **Nasıl düzelir:**
     - Sıra numarası (`01`..`11`) etiketi kesinlikle tek satır (`maxLines = 1`, `softWrap = false`) olmalı ve genişliği sabit `16dp` yerine içeriğe göre (`minWidth = 24dp`) esnemeli.
     - Rehber listesinin alt iç boşluğu `bottom = 32dp` (sistem çubuğu üstünden) yapılarak son/açık kartların alt çerçevesinin kesilmesi önlenmeli.

4. **[12-sihirbaz-shizuku-yok-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/12-sihirbaz-shizuku-yok-koyu.png) — Kesik yol başı (`› sdcard`), `data` etrafında bozuk boşluk ve kırmızı üzerinde petrol metin:**
   - **Ne yanlış:**
     - `"Dosya gezgini"` altındaki yol çubuğunda en baştaki kök dizin kesildiği için satır sahipsiz bir `›` ayracıyla (`› sdcard › Android ›   data   › com.android.settings`) başlıyor.
     - `data` kelimesinin solunda ve sağında diğer segmentlerde olmayan `~24dp` genişliğinde anlamsız birer boşluk var (`Android ›` ile `data` arası kopuk).
     - Alttaki koyu kırmızı hata kutusunun (`#5C1F19`) içindeki `"Shizuku'yu bağla"` eylemi **petrol (`#6ECDBD`)** renginde yazılmış; koyu kırmızı zemin üzerinde turkuaz/petrol metin `DESIGN.md` renk semantiğini (`onErrorContainer #FBD5D0` / `error #F2837A`) bozuyor ve görsel çakışma yaratıyor.
   - **Nasıl düzelir:**
     - Yol çubuğunun solunda kırpılan kök segment varsa başlangıç `… › sdcard › Android › data › com.android.settings` şeklinde üç nokta (`…`) ile başlamalı, asla çıplak `›` ile başlamamalı.
     - Her yol segmentinin (`sdcard`, `Android`, `data`, `com.android.settings`) yatay iç boşluğu eşit (`paddingHorizontal = 4dp`, ayraçlarla arası `6dp`) olmalı; `data` öğesindeki fazladan minimum genişlik/ortalama kaldırılmalı.
     - Hata kartı içindeki `"Shizuku'yu bağla"` butonu `onErrorContainer` (`#FBD5D0`) metin renginde ve `#F2837A` (%35 opaklık) ince çerçeveli veya `#3A0A06` dolgulu `36dp` hap buton olmalı.

5. **[26-yedekler-dolu-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/26-yedekler-dolu-koyu.png) — `"Yedek geçmişi"` başlığının iki satıra kırılması ve ortada yüzen `"1"` rakamı:**
   - **Ne yanlış:** `"Profil"` seçicisinin altındaki başlık satırında `"Yedek geçmişi"` metni dar bir alana sıkıştırıldığı için **`"Yedek\ngeçmişi"`** olarak iki satıra bölünmüş; hemen yanındaki sayaç olan **`1`** rakamı ise başlıkla bağlantısı kopuk şekilde `~50dp` sağda, ekranın ortasında tek başına yüzüyor. Ayrıca yedek kartının sol alt çeyreği (`#1794707000164 · .bak` altı) boş bir çukur gibi duruyor.
   - **Nasıl düzelir:**
     - Bölüm başlığı ve sayaç tek bir sol grupta (`Row`, `softWrap = false`) birleştirilmeli: **`Yedek geçmişi · 1`** (veya `Yedek geçmişi` + `8dp` boşluk + `1` rozeti) tek satırda sola, `"Eski yedekleri temizle"` ise sağa hizalanmalı.
     - Yedek kartında çöp kutusu (`Sil`) ikonu sol alt satıra (veya `"Geri yükle"` butonunun soluna `40dp × 40dp` ikincil ikon buton olarak) alınmalı ya da üst bilgi bloğu ile alt aksiyon satırı arasına `1dp` `#2E3B3A` ayraç konarak sol altta kalan boşluk dengelenmeli.

---

#### P1 — İmza Anları (`BRIEF.md`), Kimlik ve Hiyerarşi Düzeltmeleri

6. **[22-duzenleyici-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/22-duzenleyici-koyu.png) & [23-uygula-basari-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/23-uygula-basari-koyu.png) — İmza Anı #1 ("Değer Okuması / Value Readout") tutarsızlığı ve ölü tik rayları:**
   - **Ne yanlış:**
     - `22-duzenleyici-koyu.png` ekranında `FPSLimit` (`90`), `ResolutionScale` (`100`) ve `ShadowQuality` (`2`) kartlarının altındaki tik raylarının üçü de **birebir aynı sönük gri çizgilerden** oluşuyor: ne değerin konumunu gösteren pirinç (`#E3A94F`) bir gösterge iğnesi/dolgusu var, ne de `0–120` / `0–100` gibi min–max sınır etiketleri var. Oysa `23-uygula-basari-koyu.png` ekranındaki tik rayında `90` konumuna kadar pirinç tikler yanıyor, `90` noktasında kalın pirinç iğne duruyor ve altında `0` ile `100` yazıyor. Düzenleyicideki tik rayı işlevsiz bir süs gibi duruyor.
     - İki ekrandaki Değer Okuması tipografisi birbirini tutmuyor: `22-duzenleyici` ekranında eski değer çok küçük (`12sp` `60 ›`) ve dev `90` (`28sp`) ile yan yana sol alt köşeye sıkışmışken (kartın sağ %70'i bomboş), `23-uygula-basari` ekranında `60 90` ikisi de büyük (`28sp`) ve aralarında `›` ayracı yok.
     - `23-uygula-basari-koyu.png` (5/5 son adım, `"Dosya uygulandı"`) ekranındaki `"Değer okumaları"` özet kartının sağ üst köşesinde `14dp` boyutunda küçük pirinç bir **kalem (düzenle)** ikonu var; işlem bitip dosya cihaza yazıldıktan sonraki özet ekranında düzenleme kalemi olmasının işlevsel anlamı yok.
     - `22-duzenleyici-koyu.png` ekranının en altında, `"Değişkenleri ayarla"` alt barının hemen üst sınırında 5. kartın metni ve pin ikonu (`π` / `.`) yarım kesilmiş halde sırıtıyor.
   - **Nasıl düzelir:**
     - `22-duzenleyici` ve `23-uygula-basari` ekranlarında **aynı Değer Okuması görsel şablonu** kullanılmalı:
       - Eski değer `18sp–20sp` JetBrains Mono (`#A8B6B5`, üstü çizili) + `8dp` boşluk + `›` (`#647673`) + `8dp` boşluk + yeni değer `28sp Bold` JetBrains Mono (değiştiyse pirinç `#E3A94F`, değişmediyse `#E4EAE9`).
       - Tik rayında (`22-duzenleyici` dahil!) sayısal değerin (`90`, `100`, `2`) bulunduğu oransal konumda `12dp` yüksekliğinde `2.5dp` kalınlığında pirinç (`#E3A94F`) veya petrol (`#6ECDBD`) gösterge iğnesi yer almalı ve o noktaya kadar olan tikler aktif renkte yanmalı; rayın sol ve sağ altına `monoMicro 11sp` (`#647673`) min–max değerleri (`0` – `120`, `0` – `100`, `0` – `4`) yazılmalı.
     - `23-uygula-basari` ekranındaki `"Değer okumaları"` kartının sağ üstündeki küçük pirinç kalem ikonu kaldırılmalı; yerine `"Uygulandı"` veya `UserCustom.ini` (`monoMicro 11sp`, `#A8B6B5`) etiketi konmalı.
     - `22-duzenleyici` ekranında alt sabit buton barının (`"Değişkenleri ayarla"`) üst kenarına `1dp` `#2E3B3A` üst sınır çizgisi ve `#121A1B` tam opak zemin (`16dp` dikey iç boşluk) verilerek alttaki kartların kesik harflerinin görünmesi engellenmeli.

7. **[24-oyunlar-hazir-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/24-oyunlar-hazir-koyu.png) & [24b-oyunlar-dolu-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/24b-oyunlar-dolu-koyu.png) — Shizuku göstergesinin "Duraklat (Pause ⏸)" ikonuna benzemesi:**
   - **Ne yanlış:** `"Bağlantı hazır · Sınırlı erişim"` durumunda soldaki 3 dikey çubuktan ilk 2'si parlak petrol (`#6ECDBD`), 3. çubuk ise `#2E3B3A` renginde. Ancak kartın zemini `#1A2425` olduğu için 3. çubuğun zeminle kontrastı yalnızca **1.25:1** — yani 3. çubuk fiilen **görünmüyor**. Ekranda yan yana iki dikey çubuk (`||`) kalıyor ve bu da evrensel **"Duraklat (Pause ⏸)"** sembolü gibi algılanıyor (`01-oyunlar-bos-koyu.png` ekranında da tek kırmızı çubuk yanındaki iki koyu bordo çubuk çok sönük).
   - **Nasıl düzelir:**
     - 3 segmentli göstergenin "cihaz/kalibrasyon seviye göstergesi" olarak okunması için:
       1. Sönük (pasif) segmentlerin içi `#2C393A` dolgulu ve etrafı `1dp` `#647673` (`outline`, ≥ 3:1 kontrast) çerçeveli olmalı ki kullanıcı her zaman **"3 gözlü bir yuva"** olduğunu net görsün.
       2. Üç segmentin yüksekliği soldan sağa kademeli (`10dp`, `14dp`, `18dp`, alt tabanları hizalı, genişlik `4.5dp`, aralık `3dp`) yapılmalı; böylece 2 segment yandığında asla `||` (Pause) sembolüne benzemez, ilk bakışta **2/3 kademeli sinyal/erişim göstergesi** olarak okunur.

8. **[07-shizuku-kurulum-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/07-shizuku-kurulum-koyu.png) & [04-marketplace-bos-koyu.png](file:///home/ahmete/Masaüstü/rastgele/omniGFX/design/after/04-marketplace-bos-koyu.png) — Durum ile çelişen buton vurguları:**
   - **Ne yanlış:**
     - `07-shizuku-kurulum-koyu.png` ekranının 3. kartında açıkça **`"Shizuku servisi çalışmıyor. Shizuku uygulamasını başlat."`** yazıyor; fakat hemen altındaki en parlak birincil (`#6ECDBD` dolgulu) buton **`"İzin iste"`**! Servis çalışmıyorken izin istenemez; kullanıcının yapması gereken şey servisi başlatıp `"Tekrar denetle"`ye basmaktır.
     - `04-marketplace-bos-koyu.png` (Profil analizi) ekranında JSON metin kutusu boşken soldaki `"Çözümle"` butonu doğru şekilde pasif (`disabled`), ancak sağdaki **`"İçe aktar"`** butonunun metni tam parlak beyaz (`#E4EAE9` — aktif gibi) ve kenarlığı `#121A1B` zeminde tamamen görünmez! Boş bir metin kutusunu içe aktarmak mümkün olmadığı halde `"İçe aktar"` aktif metin renginde duruyor.
   - **Nasıl düzelir:**
     - `07-shizuku-kurulum-koyu.png` ekranında Shizuku servisi çalışmıyorken birincil dolgulu buton (`#6ECDBD` zemin, `#06231F` metin) **`"Tekrar denetle"`** (veya 2. karttaki `"Shizuku'yu aç"`) olmalı; **`"İzin iste"`** butonu ise servis çalışmadığı sürece pasif (`disabled`, `#253132` zemin, `#647673` metin) olmalı ve yalnızca servis çalışıp izin beklendiğinde birincil renge geçmeli.
     - `04-marketplace-bos-koyu.png` ekranında JSON alanı boşken `"İçe aktar"` butonu da tıpkı `"Çözümle"` gibi pasif (`disabled`, metin `#A8B6B5` %38 opaklık, kenarlık `#2E3B3A`) olmalı; metin girildiğinde ise `"Çözümle"` birincil dolgulu (`#6ECDBD`), `"İçe aktar"` ise görünür `#647673` (`1dp`) kenarlıklı ikincil buton haline gelmeli.

ELESTIRI_SONUC: KALDI
