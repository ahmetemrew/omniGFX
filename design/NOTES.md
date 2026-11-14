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
- Emülatörde Shizuku çalışmaz → sihirbazın dolu durumları gerçek cihaz kanıtıyla
  (before/ görüntüleri) doğrulanır; emülatör boş/hata/izin durumlarını çeker.
- Font glif doğrulaması fontTools cmap ile yapıldı (Barlow: › — – … • ğ İ ş VAR,
  → YOK; JetBrains Mono: → VAR).