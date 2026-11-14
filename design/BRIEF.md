# BRIEF — OmniGFX Pro

## Ürün tek cümle
OmniGFX Pro, Shizuku yetkisiyle oyunların yapılandırma dosyalarını (INI/JSON/XML/metin)
çekip değerlerini düzenleyen, profil olarak kaydeden ve **yedek + SHA-256 doğrulama +
otomatik geri alma** ile güvenle geri yazan bir Android aracıdır.

## Kim kullanıyor
- Mobil oyuncu / "tweaker", ~16–35, Türkçe konuşuyor (birincil dil TR).
- Cihaz: orta segment Android telefon (doğrulanan: Galaxy A20 / Android 13), tek elle kullanım.
- Ortam: oyunu açmadan hemen önce, kısa patlamalar halinde **2–5 dakika**; çoğunlukla
  akşam/gece, yatakta veya masada. Bazen ilk kurulumda 10–15 dk (Shizuku kurulumu).
- Teknik okuryazarlık: orta. "Config", "FPS" bilir; **UID, SELinux, hash, atomik rename bilmez.**
  Bu ayrım copy'nin temel kuralıdır.

## Kullanıcının ana işi (primary job)
> "Oyunumun ayarını değiştireyim, ama oyunumu bozmayayım — bozulursa geri alabileyim."

Bu işin duygusal çekirdeği **güven**. Kullanıcı başka bir uygulamanın dosyasına yükseltilmiş
yetkiyle yazıyor. Ürünün tüm mimarisi (yedek, hash doğrulama, çakışma tespiti, rollback)
bu güveni üretmek için var. Tasarımın işi bu güveni **göstermek** — "güçlü gamer aracı"
gibi görünmek değil.

## Ekran başına tek birincil eylem
| Ekran | Birincil eylem |
|---|---|
| Oyunlar (ana) | Oyunu seç → config'ine git |
| Profiller | Profili uygula |
| Ayarlar | Shizuku bağlantısını kur / düzelt |
| Uygulama seçici | Oyunu seç |
| Dosya seç | Dosyayı seç |
| Çek & analiz | Dosyayı çek |
| Düzenleyici | Değeri değiştir |
| Pin & profil | Profili kaydet |
| Uygula | Cihaza yaz |
| Oyun detay | Profili uygula |
| Yedekler | Yedeği geri yükle |
| Fark önizleme | Değişikliği onayla |
| Rehber | Shizuku'yu kur |
| İçe/dışa aktar | Dosyayı seç / kaydet |

## Ürünün dünyası (görsel kararların kaynağı)
Bu bir "RGB gamer" uygulaması değil. Gerçek malzemesi:
- **Config satırının kendisi:** `fps=60`, `[Graphics]`, `+CVars=...`, satır numaraları,
  dosya yolları, `a3f9…` hash dizeleri. Ekranda görünen şey kaynak-kod benzeri metin.
- **Ölçüm/kalibrasyon aleti:** cetvel tikleri, okuma penceresi (readout), kadran,
  tezgâh, tornacı mengenesi, osiloskop, mikser kanal şeridi.
- **Doğrulama:** mühür, damga, "eşleşti" işareti, sağlama toplamı.

→ Görsel dil: **"Kalibrasyon tezgâhı"**. İşlenmiş grafit gövde, pirinç sinyal lambası,
petrol yeşili "doğrulandı" rengi, monospace okumalar, tik rayları. Sıcak ama ölçülü;
teknik ama soğuk değil.

## Kişilik — 3 sıfat
**Ölçülü · Teknik · Güvenilir**

## Olmayacağı 3 şey
1. **Neon gamer RGB** — mor/camgöbeği gradyan, fosforlu yeşil, "cyber" karanlık.
2. **Jenerik SaaS kart panosu** — her şey aynı radius/gölgeyle özdeş kartlara bölünmüş.
3. **Sevimli tüketici uygulaması** — emoji, ünlem yağmuru, "Harika!", illüstrasyon şakası.

## İmza anı (en fazla 2 — geri kalan her şey sakin)
1. **Değer Okuması (Value Readout):** config değeri bir alet okuması olarak çizilir —
   monospace, eski değer sönük ve üstü çizili, yeni değer pirinç renginde, altında
   tik rayı. Düzenleyici / fark önizleme / uygulama özetinde görünür. Ürünün kalbi:
   "bir sayıyı güvenle değiştirmek".
2. **Bağlantı Göstergesi (Shizuku gauge):** nokta+metin çubuğu yerine segmentli sinyal
   göstergesi + sade dilde durum + tek eylem. Ana ekranda, uygulamanın ilk 2 saniyesi.

## Başarı ölçütü
Logo kapatıldığında ekranın "config düzenleyen bir ölçüm aleti" olduğu anlaşılmalı;
herhangi bir başka uygulamaya benzememeli. Birincil eylem 2 saniyede bulunmalı.
Açık tema koyu tema kadar birinci sınıf olmalı.
