# Eleştiri turu 1 (2026-11-15 00:20:10)

### Ön Değerlendirme: Önceki Sürümden (`before/`) Bugüne (`after/`) Ne Değişti?

Önceki sürümdeki (`design/before/`) varsayılan Material moru (`#7C4DFF`), sahte neon yeşiller, cümle sonu `→` okları ve rastgele emoji/rozet kirliliği temizlenmiş; **Barlow + JetBrains Mono** tipografisi ve **Grafit / Petrol / Pirinç / Kâğıt** renk sistemi (`DESIGN.md`) gerçekten uygulanmış. Ancak **Apple Design Award** standartlarında ve `BRIEF.md` + `DESIGN.md` sözleşmesine göre bakıldığında ürün hâlâ **"teması değiştirilmiş standart Material 3 ekranları"** düzeyinde kalıyor:
1. `BRIEF.md`'nin kalbi olan **"Değer Okuması (Value Readout)"** imza anı `22-duzenleyici` ekranında **hiç yok**, `23-uygula-basari` ekranında ise eski/yeni değer ve pirinç vurgusu olmadan yarım bırakılmış.
2. `BRIEF.md`'nin açıkça yasakladığı **SELinux / UID / octal izin** terimleri (`u:object_r:fuse:s0`, `10215:1023`, `660`) `21-cek-analiz` ekranında doğrudan kullanıcıya gösteriliyor.
3. `25-profiller-dolu` ekranında birincil eylem olan **"Profili uygula"** butonu yok; kartta yalnızca silme (çöp kutusu) ikonu var.
4. Kesik metinler (`12-sihirbaz`, `08-rehber-liste`, `20-rehber-buyukfont`), diyalog arkasından taşan harfler (`07b`), boş ekranda havada yüzen `"Yenile"` butonu (`03-yedekler-bos`) gibi doğrudan **Bitmişlik** kusurları mevcut.

---

### 1) Ekran Başına Puan Tablosu (1–10)

> **Not:** *Kimlik* = "Kalibrasyon tezgâhı" dünyası; *Ritim* = Ritim/hizalama/boşluk; *AI-slop* = 10 puan slop yok, düşük puan şablon/slop izi var demektir.

| # | Ekran (`design/after/`) | Kimlik | Hiyerarşi | Ritim | Tipografi | Renk | Durumlar | Bitmişlik | AI-slop | **Ort.** |
|---|---|---|---|---|---|---|---|---|---|---|
| 00 | `00-splash` | 4 | 5 | 5 | 5 | 7 | 6 | 5 | 6 | **5.4** |
| 01 | `01-oyunlar-bos-koyu` | 6 | 6 | 7 | 8 | 8 | 7 | 7 | 7 | **7.0** |
| 02 | `02-profiller-bos-koyu` | 6 | 6 | 6 | 7 | 8 | 6 | 7 | 7 | **6.6** |
| 03 | `03-yedekler-bos-koyu` | 5 | 5 | 4 | 7 | 7 | 6 | 4 | 6 | **5.5** |
| 04 | `04-marketplace-bos-koyu` (Profil analizi) | 5 | 5 | 5 | 7 | 7 | 5 | 5 | 6 | **5.6** |
| 05 | `05-aktarim-koyu` | 6 | 7 | 6 | 8 | 8 | 6 | 7 | 7 | **6.9** |
| 06 | `06-ayarlar-koyu` | 6 | 7 | 7 | 8 | 8 | 7 | 7 | 7 | **7.1** |
| 07 | `07-shizuku-kurulum-koyu` | 6 | 6 | 6 | 7 | 7 | 7 | 6 | 6 | **6.4** |
| 07b | `07b-shizuku-rehber-diyalog-koyu` | 5 | 5 | 5 | 7 | 6 | 6 | 3 | 6 | **5.4** |
| 08 | `08-rehber-liste-koyu` | 5 | 5 | 5 | 7 | 6 | 6 | 4 | 5 | **5.4** |
| 09 | `09-rehber-bolum-koyu` | 5 | 5 | 6 | 7 | 6 | 6 | 6 | 5 | **5.8** |
| 10 | `10-uygulama-secici-koyu` | 6 | 7 | 7 | 8 | 8 | 7 | 7 | 7 | **7.1** |
| 11 | `11-uygulama-arama-koyu` | 6 | 7 | 7 | 8 | 8 | 7 | 7 | 7 | **7.1** |
| 12 | `12-sihirbaz-shizuku-yok-koyu` | 5 | 5 | 5 | 6 | 7 | 4 | 4 | 6 | **5.3** |
| 13 | `13-oyunlar-bos-acik` | 6 | 6 | 7 | 8 | 8 | 7 | 7 | 7 | **7.0** |
| 14 | `14-profiller-bos-acik` | 6 | 6 | 6 | 7 | 8 | 6 | 7 | 7 | **6.6** |
| 15 | `15-ayarlar-acik` | 6 | 7 | 7 | 8 | 8 | 7 | 7 | 7 | **7.1** |
| 16 | `16-rehber-acik` | 5 | 5 | 5 | 7 | 6 | 6 | 4 | 5 | **5.4** |
| 17 | `17-uygulama-secici-acik` | 6 | 7 | 7 | 8 | 8 | 7 | 7 | 7 | **7.1** |
| 18 | `18-oyunlar-buyukfont-koyu` | 6 | 6 | 6 | 7 | 8 | 6 | 6 | 7 | **6.5** |
| 19 | `19-ayarlar-buyukfont-koyu` | 6 | 7 | 6 | 7 | 8 | 7 | 7 | 7 | **6.9** |
| 20 | `20-rehber-buyukfont-koyu` | 5 | 5 | 5 | 6 | 6 | 5 | 4 | 5 | **5.1** |
| 21 | `21-cek-analiz-koyu` | 6 | 6 | 7 | 7 | 8 | 6 | 5 | 6 | **6.4** |
| 22 | `22-duzenleyici-koyu` | 4 | 5 | 5 | 7 | 7 | 5 | 5 | 5 | **5.4** |
| 23 | `23-uygula-basari-koyu` | 7 | 7 | 7 | 8 | 7 | 6 | 6 | 7 | **6.9** |
| 24 | `24-oyunlar-hazir-koyu` | 7 | 7 | 7 | 8 | 8 | 7 | 7 | 7 | **7.3** |
| 24b | `24b-oyunlar-dolu-koyu` | 6 | 6 | 6 | 8 | 8 | 6 | 6 | 7 | **6.6** |
| 25 | `25-profiller-dolu-koyu` | 5 | 4 | 5 | 7 | 7 | 5 | 5 | 6 | **5.5** |
| 26 | `26-yedekler-dolu-koyu` | 5 | 5 | 5 | 6 | 7 | 6 | 5 | 6 | **5.6** |

---

### 2) Genel Ortalama

- **Kriter Ortalamaları:**
  - **Kimlik:** 5.6 / 10
  - **Hiyerarşi:** 5.9 / 10
  - **Ritim / Hizalama / Boşluk:** 6.0 / 10
  - **Tipografi:** 7.3 / 10
  - **Renk:** 7.4 / 10
  - **Durumlar:** 6.2 / 10
  - **Bitmişlik:** 5.7 / 10
  - **AI-slop:** 6.4 / 10
- **GENEL ORTALAMA: 6.31 / 10** *(29 ekran ortalaması — Geçme eşiği: ≥ 8.0, en düşük ekran: 5.1)*

---

### 3) Öncelik Sırasıyla Somut Düzeltme Listesi

#### P0 — Kritik "Bitmişlik" Kusurları ve Doğrudan BRIEF / DESIGN İhlalleri

1. **`22-duzenleyici-koyu.png` & `23-uygula-basari-koyu.png` — İmza Anı #1 ("Değer Okuması / Value Readout") eksik ve kusurlu:**
   - **Ne yanlış:** `BRIEF.md` *"Değer Okuması: config değeri bir alet okuması olarak çizilir — monospace, eski değer sönük ve üstü çizili, yeni değer pirinç renginde, altında tik rayı. Düzenleyici / fark önizleme / uygulama özetinde görünür."* diyor. Ancak `22-duzenleyici-koyu.png` ekranında hiçbir Değer Okuması veya tik rayı yok; sıradan `OutlinedTextField` kutuları ve her satırın altında çıplak `"Yukarı  Aşağı  Altına ekle  Sil"` metin butonları var. `23-uygula-basari-koyu.png` ekranındaki `"Değer okumaları"` kartında ise yalnızca beyaz (`#E4EAE9`) `60` yazıyor; eski değer (üstü çizili) ve pirinç (`#E3A94F`) yeni değer gösterimi hiç yok.
   - **Nasıl düzelir:**
     - `22-duzenleyici` ekranında sayısal config satırlarında (`FPSLimit`, `ResolutionScale`, `ShadowQuality`) standart metin kutusu yerine kalibrasyon tezgâhı okuma bileşeni (`ValueReadout`) kullanılmalı: sol üstte `monoKey` (`13sp`, `#A8B6B5`), sağ üstte eski değer (`14sp`, `#A8B6B5`, `TextDecoration.LineThrough`) + `›` + canlı/yeni değer (`monoReadout` `22sp–28sp Bold`, değiştiyse pirinç `#E3A94F`, değişmediyse `#E4EAE9`), altında `4dp` yüksekliğinde tik rayı (min/max veya adım tikleri `#2E3B3A`, aktif tik pirinç `#E3A94F`).
     - Her kartta sürekli tekrarlanan `"Yukarı  Aşağı  Altına ekle  Sil"` 4'lü metin dizisi kaldırılmalı; satır sıralama/silme işlemleri tek bir `MoreVert` (`20dp`) menüsüne veya yalnızca odaklanılan/düzenlenen satıra taşınarak kart yüksekliği `~148dp`'den `~96dp`'ye indirilmeli.
     - `23-uygula-basari` ekranındaki `"Değer okumaları"` kartında eski değer → yeni değer geçişi (`30` üstü çizili `#A8B6B5` → `60` pirinç `#E3A94F`, `monoReadout 28sp`) gösterilmeli ve tik rayının sol/sağ uçlarına min–max (`0` – `120`) mikro etiketleri (`monoMicro 11sp`) eklenmeli.

2. **`25-profiller-dolu-koyu.png` — Birincil eylem ("Profili uygula") ekranda hiç yok:**
   - **Ne yanlış:** `BRIEF.md` tablosuna göre `Profiller` ekranının tek birincil eylemi **"Profili uygula"**dır. Oysa `25-profiller-dolu-koyu.png`'da `Dengeli 60 FPS` kartının sağında yalnızca silme (`Delete` çöp kutusu) ikonu var. Kullanıcı profili nasıl uygulayacağını göremiyor.
   - **Nasıl düzelir:** Profil kartının sağ tarafına birincil aksiyon olarak `36dp` yüksekliğinde stadium `"Uygula"` butonu (zemin `#6ECDBD`, metin `#06231F`, `labelLarge 14sp Medium`) yerleştirilmeli; `"Sil"` ve `"Yedekler"` gibi ikincil/yıkıcı eylemler `MoreVert` (`20dp`, `#A8B6B5`) taşma menüsüne veya kart detayına alınmalı. Ayrıca kartta profilin hangi dosyayı/kaç değişkeni etkilediği (`UserCustom.ini · 1 değişken`) `monoMicro 11sp` ile gösterilmeli.

3. **`08-rehber-liste-koyu.png`, `09-rehber-bolum-koyu.png`, `16-rehber-acik.png`, `20-rehber-buyukfont-koyu.png` — Alt kenarda giyotin gibi kesilen metinler ve birebir mükerrer iki liste:**
   - **Ne yanlış:**
     - `08-rehber-liste-koyu.png` ve `16-rehber-acik.png`'da `"İçindekiler"` kartının en altındaki 9. satır (`"İleri düzey ipuçları"`) kart çerçevesi ve ekran alt kenarı tarafından ortadan kesilmiş (yalnızca ikonun üst ucu ve `"İ..."` harflerinin üst 3 pikseli görünüyor). `20-rehber-buyukfont-koyu.png`'da da `"Shizuku kurulumu"` satırının açıklaması (`"için gereken temel..."`) yatayda ikiye biçilmiş.
     - Dahası, üstte `"İçindekiler"` kartında 9 madde (ikon + başlık + altbaşlık) listelenirken, hemen altında (`09-rehber-bolum-koyu.png`) **birebir aynı 9 madde, aynı ikonlar ve aynı altbaşlıklarla** akordeon kartlar olarak ikinci kez listeleniyor!
   - **Nasıl düzelir:**
     - `"İçindekiler"` kutusu tamamen kaldırılmalı! Doğrudan üstteki giriş panelinin (`OmniGFX rehberi`) altında `12dp` aralıklı genişleyebilir (accordion) bölüm satırları başlamalı; böylece hem %100 mükerrer içerik kalkar hem de sabit iç yükseklik/iç çerçeve yüzünden oluşan metin kesilmesi (clipping) kökten çözülür.
     - `LazyColumn` içerik boşluğuna `contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp + navigationBarsPadding)` verilerek son satırın sistem çubuğuyla çakışması engellenmeli.

4. **`07b-shizuku-rehber-diyalog-koyu.png` — Zayıf scrim yüzünden diyalog kenarından taşan arka plan metinleri (`"S"`, `"t."`) ve yarım daire:**
   - **Ne yanlış:** Diyalog (`surfaceContainerHigh #253132`) açıldığında arkadaki ekranın karartması (scrim) çok şeffaf kaldığı ve diyalog genişliği arkadaki kartlardan dar olduğu için, diyaloğun sol üst köşesinden arkadaki `2` numaralı yeşil dairenin sol kenarı, sol ortadan `"S"` harfi, sağ ortadan `"t."` harfleri dışarı taşıyor. Görsel olarak bozuk bir katman çakışması yaratıyor. Ayrıca diyalog içindeki `"Shizuku başlatma rehberi"` bağlantısı `Adım 2` ile `Adım 3` arasında tanımsız bir boşlukta yüzüyor.
   - **Nasıl düzelir:**
     - Modal scrim opaklığı `#000000` üzerinde en az **%65 (`alpha = 0.65f`)** yapılmalı.
     - Bu içerik 3 adımlı bir rehber olduğu için dar `AlertDialog` yerine tam genişlikli `ModalBottomSheet` (`shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)`, `surfaceContainerHigh #253132`) olarak açılmalı; böylece arkadaki kartların kenarlardan pörtlemesi tamamen önlenir.
     - Diyalog/alt sayfa içindeki adımlar `Adım 1`, `Adım 2`, `Adım 3` düz paragraf metni yerine `24dp` numaralı rozet + metin satırlarıyla hizalanmalı; `"Shizuku başlatma rehberi"` ise `OutlinedButton` (`height = 36.dp`, `8dp` radius) olarak `Adım 2` bloğunun içine bağlanmalı.

5. **`12-sihirbaz-shizuku-yok-koyu.png` — Kesik breadcrumb (`com.and`), teknik `"process"` jargonu ve eylemsiz hata durumu:**
   - **Ne yanlış:**
     - `"Dosya gezgini"` altındaki yol çubuğunda `Root > sdcard > Android > data > com.and` metni sağ kenarda sert biçimde kesilmiş (`com.and`).
     - Hata kutusunda `"Dizin okunamadı: Shizuku process oluşturulamadı."` yazıyor — hem İngilizce/iç yapı terimi (`process`) var, hem de `DESIGN.md` §8 (*"ne oldu + ne yapmalı + tek eylem"*) kuralına aykırı olarak kullanıcıya hiçbir çıkış/aksiyon sunmuyor.
   - **Nasıl düzelir:**
     - Breadcrumb satırı yatayda kaydırılabilir (`horizontalScroll`) yapılmalı ve açılışta en sağa (son segmente) odaklanmalı; baştaki segmentler sığmıyorsa `… › Android › data › com.android.settings` şeklinde sol-orta kısaltma (middle/start ellipsis) uygulanmalı. Ayrıca `>` yerine `DESIGN.md` §8'de belirtilen `›` (U+203A) ayracı kullanılmalı.
     - Shizuku çalışmıyorsa hata kutusu metni: `"Dosya gezgini için Shizuku bağlantısı gerekli. Shizuku'yu başlatıp tekrar dene."` olmalı ve kutunun içinde sağda/altta `"Shizuku'yu bağla"` (`TextButton` veya `FilledTonalButton`, `#FBD5D0` metin) aksiyonu yer almalı.

6. **`21-cek-analiz-koyu.png` — `BRIEF.md` ve `DESIGN.md` copy kuralı ihlali (SELinux, UID, octal izin):**
   - **Ne yanlış:** `BRIEF.md` *"UID, SELinux, hash, atomik rename bilmez. Bu ayrım copy'nin temel kuralıdır."* ve `DESIGN.md` §8 *"Teknik iç yapı terimi son kullanıcıya gösterilmez (detay isteyene açılır)"* diyor. Ancak `"Dosya kimliği"` kartında doğrudan `Sahip: 10215:1023`, `İzinler: 660`, `Güvenlik etiketi: u:object_r:fuse:s0` basılmış.
   - **Nasıl düzelir:** `"Dosya kimliği"` kartında varsayılan görünüm `23-uygula-basari` ekranındaki gibi insani ve güven verici olmalı: `"Orijinal dosya izinleri ve güvenlik kimliği yedeklendi — yazma sırasında korunur"` + yanında petrol onay rozeti (`#14453E` zemin, `#A8E6DA` metin: `"Korunuyor"`). Ham `10215:1023 · 660 · u:object_r:fuse:s0` değerleri yalnızca `"Teknik detayları göster ▾"` (`labelMedium 12sp`, `#A8B6B5`) satırına tıklandığında açılan ikincil bir `monoMicro` satırında gösterilmeli.

7. **`03-yedekler-bos-koyu.png` & `26-yedekler-dolu-koyu.png` — Boş ekranda havada yüzen `"Yenile"` butonu ve kaba `.bak` dosya başlığı:**
   - **Ne yanlış:**
     - `03-yedekler-bos-koyu.png` ekranında ortada `"Henüz yedek yok"` boş durumu varken, sol üstte (`x=20dp, y=108dp`) tek başına bir `"Yenile"` (`OutlinedButton`) butonu havada asılı duruyor.
     - `26-yedekler-dolu-koyu.png` ekranında yedek kartının ana başlığı `.UserCustom.ini.omnigfx.1794689761405.bak` gibi okunaksız bir sistem dosya adı; altında çöp kutusu ikonu kartın ortasında sahipsiz duruyor.
   - **Nasıl düzelir:**
     - `"Yenile"` butonu içerik alanından kaldırılıp `TopAppBar`'ın sağ aksiyon ikonuna (`Refresh` `24dp`, `#A8B6B5`) dönüştürülmeli (veya boş durumda tamamen gizlenmeli). `"Eski yedekleri temizle"` ise liste başlığı satırının sağına hizalanmalı.
     - Yedek kartında birincil başlık tarih/saat (`"14 Kasım 2026 · 23:56"`, `titleMedium 16sp SemiBold`, `#E4EAE9`), ikinci satır hedef dosya yolu (`/sdcard/Download/UserCustom.ini`, `monoKey 13sp`, `#A8B6B5`), üçüncü satır ise kısaltılmış yedek kimliği (`#1794689761405 · .bak`, `monoMicro 11sp`, `#647673`) olmalı. Silme ikonu (`Delete`) sol alt kenara (`x=16dp` kart içi) veya sağ üst köşeye hizalanmalı, ortada yüzmemeli.

---

#### P1 — Kimlik, Hiyerarşi ve Navigasyon Tutarlılığı Düzeltmeleri

8. **Ana Sekmeler (`01-oyunlar`, `02-profiller`, `06-ayarlar`) — Üst Bar (TopAppBar) tutarsızlığı:**
   - **Ne yanlış:** `Oyunlar` ekranında başlık sola hizalı büyük `headlineLarge` (`30sp SemiBold`) + altbaşlık iken, `Profiller` ve `Ayarlar` sekmelerinde başlık ortalanmış küçük `titleLarge` (`19sp`) ve altbaşlıksız! Kullanıcı alt bardaki 3 ana sekme arasında geçiş yaptığında üst bar zıplıyor.
   - **Nasıl düzelir:** Üç ana sekmenin (`Oyunlar`, `Profiller`, `Ayarlar`) tamamında aynı üst başlık şablonu kullanılmalı: sola hizalı (`paddingStart = 20.dp, paddingTop = 16.dp`) `headlineMedium` (`26sp SemiBold`, `#E4EAE9` / `#16211F`) + altında tek satır `bodyMedium` (`14sp`, `#A8B6B5` / `#4C5A58`) açıklama. `Profiller` sekmesindeki sağ üst aksiyonlar (`Profil analizi`, `Profil aktarımı`) başlık satırının sağ karşısına (`x = end - 20dp`) ikon/menü olarak yerleştirilmeli.

9. **`01-oyunlar-bos-koyu.png`, `13-oyunlar-bos-acik.png`, `24-oyunlar-hazir-koyu.png` — İmza Anı #2 ("Shizuku Bağlantı Göstergesi / Gauge") ve buton önceliği:**
   - **Ne yanlış:**
     - `BRIEF.md` *"nokta+metin çubuğu yerine segmentli sinyal göstergesi + sade dilde durum + tek eylem"* diyor. Mevcut ekranda sol tarafta standart telefon şebeke ikonu (`SignalCellularAlt`) var ve hata durumunda 4 çubuğun 4'ü de aynı soluk kırmızı renkte boyanmış — bu bir "kalibrasyon/sinyal göstergesi" değil, hazır ikon.
     - Ayrıca `01-oyunlar-bos` ekranında Shizuku çalışmıyorken bile ortadaki `"Oyun ekle"` butonu en parlak birincil (`primary`) buton olarak parlıyor; kullanıcı önce Shizuku'yu başlatmak yerine `"Oyun ekle"`ye basıp `12-sihirbaz`'da hataya düşüyor.
   - **Nasıl düzelir:**
     - Shizuku göstergesi özel çizilmiş 3 veya 4 dikey/yatay kalibrasyon segmentinden (`4dp × 16dp`, `2dp` radius, `3dp` aralık) oluşmalı:
       - Bağlantı yok: 3 segment sönük (`#5C1F19` / açıkta `#FADCD8`), sol 1 segment oksit (`#F2837A` / `#A8342A`), sağda `"Bağla"` hap (pill) aksiyonu.
       - Sınırlı erişim (ADB): 2 segment petrol (`#6ECDBD`), 1 segment sönük (`#2E3B3A`).
       - Tam erişim (Root): 3 segment tam petrol (`#6ECDBD`).
     - Shizuku çalışmıyorken ortadaki `"Oyun ekle"` butonu ikincil (`OutlinedButton` veya `FilledTonalButton`) seviyeye çekilmeli, birincil vurgu Shizuku bağlantı kartındaki `"Shizuku'yu başlat"` eyleminde olmalı.

10. **`24b-oyunlar-dolu-koyu.png` — Oyun kartı içeriği zayıf ve FAB şekil/hiyerarşi hatası:**
    - **Ne yanlış:**
      - `DESIGN.md` §4 *"full | stadium | birincil buton, FAB"* diyor ama sağ alttaki `+ Oyun ekle` FAB'ı stadium değil, `16dp` köşeli dikdörtgen ve parlak `#6ECDBD` dolgusuyla ekrandaki oyun kartından çok daha baskın (`BRIEF.md`: *"Oyunlar birincil eylem: Oyunu seç → config'ine git"*).
      - Oyun kartında sadece uygulama adı (`Settings`) ve paket adı (`com.android.settings`) var; bu oyuna ait kaç profil olduğu veya son uygulanan profil bilgisi hiç görünmüyor.
    - **Nasıl düzelir:**
      - Liste dolu olduğunda `+ Oyun ekle` ya üst başlık satırının sağında kompakt bir `FilledTonalButton` (`height = 36.dp`, stadium, `#14453E` zemin, `#A8E6DA` metin) olmalı ya da FAB kalacaksa `CircleShape` / `Stadium` (`50%` radius) ve `surfaceContainerHighest` / `primaryContainer` tonunda olup oyun kartının önüne geçmemeli.
      - Oyun kartına (`surfaceContainer #1F2A2B`, `12dp` radius) 3. bir mikro bilgi satırı veya sağ rozet eklenmeli: örn. `"1 profil · Dengeli 60 FPS"` (`labelSmall 11sp`, `#6ECDBD`).

11. **`21-cek-analiz-koyu.png` & `23-uygula-basari-koyu.png` — Ara adım ile final adımın hero alanı birebir aynı:**
    - **Ne yanlış:** 2. adım olan `Dosya çekildi` (`21`) ile 5. ve son adım olan `Dosya uygulandı` (`23`) ekranlarının üst kısmında birebir aynı `80dp` koyu petrol kare kutu ve aynı `CheckCircle` ikonu kullanılmış. Ara adımla final "mühür/doğrulama" adımı görsel olarak ayırt edilmiyor.
    - **Nasıl düzelir:**
      - `21-cek-analiz` (ara adım) ekranında dev hero kutusu kaldırılıp `"Dosya bilgisi"` kartının üst şeridine kompakt bir durum satırı (`CheckCircle 20dp` + `"Dosya okundu · 128 B · 11 satır"`) konmalı; böylece ekran yukarı toplanır ve gereksiz dikey kaydırma önlenir.
      - Hero mühür alanı yalnızca `23-uygula-basari` (final ekranı) için saklanmalı.

---

#### P2 — AI-Slop Temizliği, Boşluk ve Detay Cilası

12. **`08-rehber-liste-koyu.png`, `09-rehber-bolum-koyu.png`, `16-rehber-acik.png` — Rastgele renkli ikon kutuları (AI-slop):**
    - **Ne yanlış:** Rehber maddelerinin solundaki ikon kutuları sırayla petrol, pirinç (kehribar) ve griye boyanmış. `DESIGN.md` §1'de pirinç (`secondary`) açıkça *"sinyal lambası, değişen/canlı değer, dikkat"* için ayrılmışken, `"Root ve ADB: farklar"`, `"Regex nedir?"`, `"Sık karşılaşılan sorunlar"` ikonlarının pirinç renginde olmasının hiçbir anlamsal karşılığı yok; tipik "renkli SaaS liste şablonu" (AI-slop) izi taşıyor.
    - **Nasıl düzelir:** Tüm rehber bölüm ikonları tek bir teknik stilde birleştirilmeli: `36dp × 36dp`, `8dp` radius (`small`), zemin `surfaceContainerHigh` (`#253132` koyu / `#E3E9E8` açık), ikon rengi `onSurfaceVariant` (`#A8B6B5` koyu / `#4C5A58` açık) veya `primary` (`#6ECDBD` / `#0E5F56`). Yanlarına `01`..`09` `monoMicro` (`11sp`) sıra numarası eklenerek "kullanım kılavuzu / teknik el kitabı" kimliği güçlendirilmeli.

13. **`04-marketplace-bos-koyu.png` (Profil analizi) & `05-aktarim-koyu.png` (Profil aktarımı) — Kopuk yardımcı ekranlar ve yanıltıcı buton durumu:**
    - **Ne yanlış:**
      - `04-marketplace-bos-koyu.png` ekranında JSON metin alanı boşken `"Çözümle"` butonu tam aktif (`#6ECDBD`) görünüyor; yanındaki `"İçe aktar"` butonu ise `#121A1B` zeminde neredeyse görünmez (`#2E3B3A` kenarlık). Metin kutusunun içi `mono` ipucu (placeholder) içermiyor.
      - `04` ve `05` ekranlarının ikisi de JSON içe/dışa aktarma işi yaptığı halde iki ayrı yarım ekran olarak bölünmüş, ikisinin de alt %55'i boş.
    - **Nasıl düzelir:**
      - `04-marketplace-bos-koyu.png` ekranında JSON alanı boşken `"Çözümle"` butonu `enabled = false` (`surfaceContainerHighest` zemin, `onSurfaceVariant` %38 metin) olmalı; metin kutusunun sağ üst köşesine `"Panodan yapıştır"` (`ContentPaste` `18dp` + `labelMedium`) hızlı aksiyon çipi ve içine `JetBrains Mono` örnek placeholder (`{ "name": "...", "rules": [...] }`) eklenmeli.

14. **`00-splash.png` — Kimliksiz, kaba geometrik splash ikonu:**
    - **Ne yanlış:** Üstte soyut bir petrol hap, altında ise sağ kenarından turuncu daire taşan kaba bir çubuk var. İki şekil arasındaki mesafe, oran ve hizalama ne bir ölçüm aletini ne de `OmniGFX` markasını anlatıyor.
    - **Nasıl düzelir:** Splash ekranında tek ve net bir "kalibrasyon kadranı / tik rayı" amblemi kullanılmalı (`72dp × 72dp` grafit/petrol çerçeve içinde ince tik rayı ve pirinç `#E3A94F` gösterge iğnesi/kaydırıcısı) ve altında `16dp` boşlukla `Barlow SemiBold 22sp` `"OmniGFX"` + `JetBrains Mono 11sp` `"CALIBRATION SUITE"` / `"v2.0"` yer almalı.

15. **`12`, `21`, `22`, `23` (Sihirbaz ekranları) — Dilsiz 5 segmentli ilerleme çubuğu:**
    - **Ne yanlış:** Üst barın altındaki 5 çizgi yalnızca renk değiştiriyor (`petrol` = bitti, `pirinç` = aktif, `gri` = kalan); kullanıcının kaçıncı adımda olduğu ve toplam adım sayısı yazmıyor.
    - **Nasıl düzelir:** `TopAppBar`'ın sağ tarafına (`x = end - 20dp`) `JetBrains Mono` (`monoMicro 12sp`, `#A8B6B5`) ile adım sayacı (`ADIM 1/5`, `ADIM 2/5`, `ADIM 3/5`, `ADIM 5/5` veya `1 / 5`) eklenmeli; aktif segmentin yüksekliği `4dp`, pasif segmentlerin yüksekliği `2dp` yapılarak görsel odak keskinleştirilmeli.

ELESTIRI_SONUC: KALDI
