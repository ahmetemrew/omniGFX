# NOTES — denenip vazgeçilen yönler ve tur hafızası

## Vazgeçilen yönler
1. **Turkuaz + tek vurgu (ilk taslak):** "koyu zemin + turkuaz" herhangi bir araç
   uygulamasına uygulanırdı → petrol + pirinç ikilisine geçildi (DESIGN.md §10).
2. **Sabit dark token'lar (eski kod):** `OmniBackground`/`OmniPrimary` gibi sabit
   renkler açık temayı fiilen bozuyordu → tümü `MaterialTheme.colorScheme` +
   `OmniTheme.semantic`'e taşındı, `app/ui/theme/` silindi.
3. **Mor gradyan ikon placeholder:** OmniPrimary→Glow gradyanı slop'tu → gerçek
   vektör marka ikonu çizildi (= operatörü + tik rayı + pirinç topuz).
4. **Nokta + meta dizisi Shizuku çubuğu:** "Shizuku hazır • ADB/shell (UID 2000)"
   → segmentli SignalGauge + sade dil ("Bağlantı hazır / Sınırlı erişim").
5. **Metinde "→" oku:** Barlow alt kümesinde glif yok, fallback font'a düşüyordu.
   Ayar yollarında "›" (U+203A, fontta doğrulandı), terim dizilerinde "—" kullanıldı.
   Kod blokları JetBrains Mono çiziyor; oradaki "→" teknik gösterim, kaldı.
6. **"Her Kelime Büyük" başlıklar:** rehberdeki tip başlıkları cümle düzenine çekildi
   ("Profil Import/Export" → "Profilleri dışa ve içe aktarma").
7. **Shimmer yüklenme:** sakin placeholder + OmniLoading tercih edildi (skill kuralı).
8. **Eşit segmentli gösterge (tur 1 kararı, tur 2'de terk edildi):** "artan çubuklar
   şebeke ikonu çağrıştırır" diye eşit boyutta (4×16dp) kalınmıştı; eleştirmen eşit
   çubukların Pause (⏸) sembolüne benzediğini gösterdi. Kademeli 10/14/18dp boy +
   çerçeveli pasif gözler iki çağrışımı da keser (alet paneli ölçek yuvası).
9. **Büyük punto splash etiketi (tur 2'de reddedildi):** eleştirmen 9.5–10sp önerdi
   ama kendi "genişlik ≤ wordmark" kısıtıyla çelişiyordu; 7.25sp + 0.12em track
   güvenli çembere sığan en büyük okunur kilit (üretici assert'li, pay 19.3dp).
10. **Breadcrumb'da TextButton (tur 2'de terk edildi):** 58dp asgari genişlik kısa
    segmentlerde ("data") anlamsız boşluk üretiyordu; eşit 4dp padding'li düz
    clickable hedefler + kaydırmada "…" öneki.
11. **Breadcrumb'da sabit genişlikli "…" örtüsü + önde ayraç (tur 3'te terk
    edildi):** örtü genişliği metin+2×12dp idi; kaydırma konumuna göre kısmi kök
    öğenin sağ ucu örtünün dışında kalıp kırpık glif sızdırıyordu ("… t › sdcard").
    Örtü genişliği artık DİNAMİK (layoutInfo: ilk görünür öğenin offset+size'ı),
    fillMaxHeight + clipToBounds; ayraç öğe SONUNA alındı — derin kaydırmada
    örtüden sonra gelen ilk şey daima segment adı, çıplak "›" olamaz.
    **Tur 3 devamı (17. oturum):** layoutInfo koordinatları LazyRow'ın kendi
    viewport'una göredir; örtü dış Box'ın solundan başladığı için LazyRow'ın
    yatay padding'i (spacing.md, 12dp) genişliğe EKLENMELİDİR — yoksa örtü
    12dp kısa kalır ve kısmi segmentin sağ ucu yine sızar.

## Bilinçli korunanlar
- Koyu zemin (saf siyah değil, grafit + 5 yüzey adımı) — DESIGN.md §9 gerekçeli sapma.
- Sihirbaz adım numaralandırması — gerçek sıra var, slop değil.
- Dosya gezgini metadata'sı (boyut/tarih/izin) — ileri düzey kullanıcı için işlevsel,
  monospace ve hizalı; "A · B · C" süs meta dizisi değil.
- Bileşen içi boyutlar (ikon 16/20/24/28, gauge segment yükseklikleri, canvas
  çizim sabitleri) token değil bileşen boyutudur; spacing/radius/fontSize token'landı.

## Uyarlanabilirlik kararı
- BRIEF hedef cihazı: orta segment telefon, tek el. Düzen tek kolon, akışkan
  (fillMaxWidth + weight + scroll); Medium/Expanded genişlikte dağılmaz, satır ve
  okuma pencereleri uzar. Tablet iki-panel/liste-detay bu turda kapsam dışı —
  ürünün ana işi telefonda 2-5 dk'lık patlamalar; iki-panel ana işi değiştirirdi.
- font_scale 2.0 doğrulandı (after/18–20 + dump denetimi): taşma yok, hedefler korunuyor.

## Ortam / süreç notları
- Kardeş projeler build.lock'u uzun tutuyor (emülatör oturumları). Tüm build/adb
  komutları `flock -o` ile sarıldı; emülatör tek çağrıda başlatılıp kapatılacak.
- Shizuku emülatörde ADB modunda ÇALIŞIYOR (v13.6, resmi native libshizuku.so
  başlatma yolu; tools/design_filled_session.sh). Dolu durumlar (21–26+24b)
  emülatörde gerçek yazma + yedek akışıyla çekilir; 10. oturumdan beri kanıt hattı bu.
- **Shizuku izin DB'si koşular arası kalır** (AVD userdata): dolu oturum Shizuku'yu
  da pm clear etmeli, yoksa önceki koşunun izni CONNECTED döndürür ve kurulum
  ekranı canContinue ile kendini anında kapatır → grant akışı kırılır (16. oturum).
- **uiautomator dump'ın `enabled` alanı Compose'da güvenilir değil**: disabled
  butonlar true görünür. Pasiflik kanıtı piksel renginden verilir (zemin + metin
  tonu); dump yalnız metin/konum kanıtıdır.
- Font glif doğrulaması fontTools cmap ile yapıldı (Barlow: › — – … • ğ İ ş VAR,
  → YOK; JetBrains Mono: → VAR).