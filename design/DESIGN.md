# DESIGN — OmniGFX Pro token sistemi

Kimlik: **Kalibrasyon tezgâhı.** İşlenmiş grafit gövde + pirinç sinyal + petrol "doğrulandı".
Tek kaynak: `core/core-ui/.../theme/` (`OmniColor.kt`, `OmniTypography.kt`, `OmniShape.kt`,
`OmniSpacing.kt`, `OmniMotion.kt`). `app/ui/theme/` yalnızca bunları eşler, kendi rengi yok.

## 1. Renk

### İsimli ana renkler (6)
| İsim | Açık | Koyu | Rolü |
|---|---|---|---|
| **Petrol** | `#0E5F56` | `#6ECDBD` | primary — güven, doğrulama, birincil eylem |
| **Pirinç** | `#8A5C10` | `#E3A94F` | secondary — sinyal lambası, değişen/canlı değer, dikkat |
| **Oksit** | `#A8342A` | `#F2837A` | error — hata, tehlikeli yazma |
| **Grafit** | — | `#121A1B` | koyu gövde (petrol dökümlü, saf siyah değil) |
| **Kâğıt** | `#F4F7F6` | — | açık zemin (soğuk çizim kâğıdı, krem değil) |
| **Duman** | `#4C5A58` | `#A8B6B5` | ikincil metin |

### Anlamsal roller — KOYU
```
background #121A1B   onBackground #E4EAE9
surface    #161F20   onSurface    #E4EAE9
surfaceContainerLow #1A2425   surfaceContainer #1F2A2B
surfaceContainerHigh #253132  surfaceContainerHighest #2C393A
surfaceVariant #253132        onSurfaceVariant #A8B6B5
primary #6ECDBD  onPrimary #06231F  primaryContainer #14453E  onPrimaryContainer #A8E6DA
secondary #E3A94F onSecondary #2A1B02 secondaryContainer #4A3413 onSecondaryContainer #F6D9A4
error #F2837A onError #3A0A06 errorContainer #5C1F19 onErrorContainer #FBD5D0
outline #647673  outlineVariant #2E3B3A  scrim #000000
```

### Anlamsal roller — AÇIK
```
background #F4F7F6   onBackground #16211F
surface    #FFFFFF   onSurface    #16211F
surfaceContainerLow #F0F4F3   surfaceContainer #EAEFEE
surfaceContainerHigh #E3E9E8  surfaceContainerHighest #DBE3E2
surfaceVariant #E3E9E8        onSurfaceVariant #4C5A58
primary #0E5F56 onPrimary #FFFFFF primaryContainer #CDEAE4 onPrimaryContainer #06302A
secondary #8A5C10 onSecondary #FFFFFF secondaryContainer #F7E3BE onSecondaryContainer #3E2A05
error #A8342A onError #FFFFFF errorContainer #FADCD8 onErrorContainer #4A0F09
outline #7C8B89  outlineVariant #C6D0CF  scrim #000000
```

### Kontrast (hesaplandı, WCAG 2.1 bağıl luminans)
Koyu: onSurface/bg **14.50**, onSurface/surface **13.78**, onSurface/scHighest **9.83**,
onSurfaceVariant/bg **8.43**, onSurfaceVariant/scHighest **5.72**, onPrimary/primary **8.78**,
primary/bg **9.36**, onPrimaryContainer/primaryContainer **7.69**, onSecondary/secondary **8.00**,
secondary/bg **8.45**, onSecondaryContainer/secondaryContainer **8.58**, onError/error **6.77**,
error/bg **6.98**, onErrorContainer/errorContainer **9.33**, outline/bg **3.68**, outline/surface **3.50**.

Açık: onSurface/bg **15.32**, onSurface/surface **16.52**, onSurfaceVariant/bg **6.69**,
onSurfaceVariant/scHighest **5.53**, onPrimary/primary **7.53**, primary/bg **6.98**,
onPrimaryContainer/primaryContainer **11.26**, onSecondary/secondary **5.80**,
onSecondaryContainer/secondaryContainer **10.85**, onError/error **6.58**,
onErrorContainer/errorContainer **11.95**, outline/bg **3.30**, outline/surface **3.55**.

→ **Tüm metin çiftleri ≥ 4.5:1, tüm ikon/kenar çiftleri ≥ 3:1.** İstisna: `outlineVariant`
bilinçli olarak dekoratif ayraç içindir (kontrol tanımlamanın tek yolu değil), ≥3:1 aranmaz.
Dolgulu metin alanları `surfaceContainerLow`/`surface` zemin kullanır ki `outline` 3:1 geçsin.

### Bilinçli anlamsal kararlar
- **success = primary (petrol).** "Doğrulandı" ürünün marka vaadi; hash eşleşmesi marka
  renginde parlar. Ayrı bir yeşil icat edilmedi — petrol ile yeşil yan yana gelip birbirini
  kirletmesin diye. `OmniSuccess` token'ı primary'ye eşlenir.
- **warning = secondary (pirinç).** Dikkak = sinyal lambası. Pirinç fosforlu değil, doygunluğu
  düşük; koyu temada `#E3A94F` bir lamba rengi, neon değil.
- **info = onSurfaceVariant.** Nötr teknik not ayrı bir maviyle değil, sönük metinle verilir.
  Üçüncü bir vurgu rengi eklemek paleti dağıtır.

### Dinamik renk (Material You): **KAPALI**
Gerekçe: kimlik güçlü ve ürüne özgü (petrol+pirinç = ölçüm aleti). Duvar kâğıdından gelen
rastgele bir seed bu kimliği yok eder ve "doğrulandı = petrol" semantiğini bozar.
`dynamicColor = false` varsayılan; kullanıcıya sunulmuyor.

## 2. Tipografi — 2 aile, belirgin farklı

### UI: **Barlow** (OFL, Google Fonts)
Gerekçe: Barlow, Kaliforniya kamusal yönlendirme tabelalarından esinlenen bir grotesk;
DIN soyundan gelir = **teknik panel, ölçüm, wayfinding**. Tam da "birincil eylemi 2 saniyede
bul" gereksinimi için tasarlanmış bir tür. Roboto/Inter'den belirgin biçimde farklı:
daha dar apertür, hafif yuvarlatılmış terminaller (sıcaklık verir, sevimlileşmez).
Türkçe/Latin-ext tam (ğ ü ş ı ö ç İ doğrulandı).

### Malzeme: **JetBrains Mono** (OFL, Google Fonts)
Gerekçe: uygulamanın gerçek içeriği config metni — anahtarlar, değerler, dosya yolları,
SHA-256 dizeleri, satır numaraları. Kodlama monospace'i **işlevsel zorunluluk**: sıfır
çizikli (0/O ayrımı), l/1/I ayrımı, sabit genişlik → yollar ve hash'ler hizalı okunur.
Bu, "her uygulamaya" uygulanmayacak, ürüne özgü bir karar.

Fontlar gömülü (`core-ui/res/font/`), downloadable değil → çevrimdışı tutarlılık.
Latin + Latin-1 + Latin Extended-A + noktalama alt kümesine indirildi: **7 dosya, 464 KB**.
Lisanslar: `design/licenses/Barlow-OFL.txt`, `design/licenses/JetBrainsMono-OFL.txt`.
Barlow'da ok glifi (→←↑↓) **yok** → metinde ok kullanılmaz, vektör ikon çizilir (copy kuralıyla da uyumlu).

### Ölçek (tüm stillerde lineHeight VAR — eski sürümde hiç yoktu)
| Rol | Boyut | Ağırlık | Satır yüksekliği | Harf aralığı | Aile |
|---|---|---|---|---|---|
| displaySmall | 34 | SemiBold | 40 | −0.5 | Barlow |
| headlineLarge | 30 | SemiBold | 36 | −0.4 | Barlow |
| headlineMedium | 26 | SemiBold | 32 | −0.3 | Barlow |
| headlineSmall | 22 | SemiBold | 28 | −0.2 | Barlow |
| titleLarge | 19 | SemiBold | 26 | −0.1 | Barlow |
| titleMedium | 16 | Medium | 22 | 0 | Barlow |
| titleSmall | 14 | Medium | 20 | 0.1 | Barlow |
| bodyLarge | 16 | Normal | 24 | 0 | Barlow |
| bodyMedium | 14 | Normal | 21 | 0.1 | Barlow |
| bodySmall | 12 | Normal | 17 | 0.2 | Barlow |
| labelLarge | 14 | Medium | 18 | 0.1 | Barlow |
| labelMedium | 12 | Medium | 16 | 0.4 | Barlow |
| labelSmall | 11 | Medium | 14 | 0.5 | Barlow |

Ek: `OmniType.mono*` — config anahtarı/değeri/yolu/hash için JetBrains Mono
(monoValue 16/22 Medium, monoKey 13/18 Regular, monoReadout 28/32 Bold, monoMicro 11/14).
Sayılar her yerde `FontFeature "tnum"` (tabular) → slider değeri titremez.

## 3. Boşluk & ızgara — 4/8 tabanı
`OmniSpacing`: xxs 2, xs 4, sm 8, md 12, lg 16, xl 20, xxl 24, xxxl 32, section 40.
- Ekran kenar boşluğu: **20dp** (eski 24dp → 8'in katı değil, 20 = 4×5 ve ızgaraya oturur).
- Kart içi: 16dp. Bölümler arası: 24dp. Liste öğesi arası: 12dp.
- Dokunma hedefi: ≥48dp (`minimumInteractiveComponentSize`).

## 4. Şekil — hiyerarşiye göre değişir
| Rol | Radius | Nerede |
|---|---|---|
| extraSmall | **4dp** | çip, etiket, tik rayı, kod bloğu |
| small | **8dp** | metin alanı, liste satırı, küçük kutu |
| medium | **12dp** | kart, panel |
| large | **16dp** | alt sayfa, diyalog |
| extraLarge | **20dp** | hero okuma penceresi |
| full | stadium | **birincil buton** (fiziksel alet tuşu), FAB |

→ Birincil eylem stadium, veri öğeleri 4dp: **gerçek şekil kontrastı** (M3 Expressive kaldıracı).
Eski sürümde her şey 12–24dp arası aynıydı = slop.

## 5. Yükselti — 3 anlamlı seviye
`level0` zemin (gölge yok) · `level1` kart/panel (1dp) · `level2` modal/alt sayfa/FAB (3dp).
Koyu temada gölge yerine **yüzey adımı** (surfaceContainer* tonları) hiyerarşi kurar;
gölge koyuda görünmez. Her karta aynı gri gölge yok.

## 6. Hareket
`OmniMotion`: short 120ms · medium 220ms · long 320ms.
Easing: `Emphasized` = CubicBezier(0.2, 0, 0, 1) (M3 emphasized); standard = (0.2,0,0,1).
Spring: `snappy` (dampingRatio 0.72, stiffness 420) — basılı hal / şekil morphu.
- Yalnızca kullanıcı eylemine cevap veya durum açıklaması. Sürekli dekoratif animasyon YOK.
- **`Settings.Global.ANIMATOR_DURATION_SCALE == 0` ise tüm süreler 0** → anında geçiş.
  `OmniMotion.scale()` CompositionLocal ile okunur.
- Basılı hal: birincil butonda şekil morphu (stadium → 12dp) + ripple.

## 7. İkonografi
Tek aile: `material-icons-extended` **Outlined**, tek çizgi kalınlığı, 20dp (satır içi) /
24dp (standart) / 28dp (hero). Dolgu (filled) yalnızca seçili alt bar göstergesinde.
Anlamsal düzeltme: Oyunlar = `SportsEsports`, Profiller = `Tune`, Ayarlar = `Settings`.
Eski `Home`/`Person` yanlış anlam taşıyordu.
**Emoji ikon yerine geçmez** — mevcut 💡📂📌📝✅❌⚠✓✗●━ temizlenir, vektörle değiştirilir.

## 8. Copy (Türkçe)
- Sade, aktif fiil, cümle düzeni ("Değişkenleri Yapılandır & Kaydet →" → "Değişkenleri ayarla").
- Teknik iç yapı terimi son kullanıcıya gösterilmez: "UID 2000", "ROOT (UID 0)",
  "ADB/shell" → "Sınırlı erişim" / "Tam erişim" (detay isteyene açılır).
- Hata özür dilemez: ne oldu + ne yapmalı + tek eylem.
- Boş ekran davettir: başlık + neden + tek buton. "Veri yok" yok.
- "→" ok yok (Barlow alt kümesinde glifi yok). Ayar yolu ayraçlarında **"›"**
  (U+203A, Barlow'da VAR, doğrulandı) kullanılır: "Ayarlar › Telefon Hakkında".
  Terim/açıklama dizilerinde "—" (em dash, Barlow'da VAR). Kod blokları JetBrains
  Mono ile çizilir; onda "→" glifi vardır ve teknik gösterimin parçasıdır, kalır.
- "&" yerine "ve", emoji yok, ünlem yok.
- Aynı eylem her yerde aynı ad: "Uygula" → "Uygulandı"; "Kaydet" → "Kaydedildi".

## 9. AI-slop kontrolü — her madde için durum
| §2 maddesi | Durum |
|---|---|
| Krem + serif + terrakota | YOK (kâğıt soğuk gri, Barlow grotesk, vurgu petrol/pirinç) |
| Neredeyse siyah + tek fosforlu vurgu | **Bilinçli sapma, aşağıda gerekçe** |
| Mor/mavi gradyan, glassmorphism | YOK — tüm gradyanlar kaldırıldı |
| Özdeş kart kiti | YOK — şekil hiyerarşisi + 3 yükselti + kart yerine panel/satır |
| BÜYÜK HARFLİ etiket, "A · B · C" meta | YOK — "Shizuku hazır • ADB/shell (UID 2000)" kaldırıldı |
| Başlıkta tek kelimeyi renkli/italik | YOK |
| Sırasız 01/02/03 numaralandırma | YOK — numaralandırma yalnızca sihirbaz adımlarında (gerçek sıra) |
| Büyük sayı + gradyan istatistik kartı | YOK |
| Her bölüme fade+slide, dekoratif dönen | YOK — yalnızca sekme geçişi ve durum değişimi |
| Buton sonunda "→", emoji ikon | YOK — temizleniyor |
| Varsayılan Material mor | YOK — `#7C4DFF`/`#6750A4` tamamen kaldırıldı |
| "Hoş geldiniz!", "Harika!", "Oops!" | YOK |
| Boş ekranda sadece "Veri yok" | YOK — yönlendirici boş durum |

### Bilinçli sapma gerekçesi: koyu zemin
Ürün bir **gece kullanılan oyun aracı** ve gerçek malzemesi koyu zeminde daha iyi okunan
config metni. Koyu tema korunuyor AMA slop'un iki koşulu da kırılıyor:
1. Zemin **saf siyah değil**, petrol dökümlü grafit (`#121A1B`) ve **5 belirgin yüzey adımı** var
   (`#161F20 → #1A2425 → #1F2A2B → #253132 → #2C393A`) → hiyerarşi ışıltıyla değil yüzeyle kuruluyor.
2. Vurgu **tek ve fosforlu değil**: petrol + pirinç + oksit, üçü de doygunluğu düşük,
   gerçek bir semantik sistem. `#00E5A8` / `#00F0FF` / `#FF007F` neonları tamamen gitti.
3. **Açık tema birinci sınıf** — ayrı tasarlandı, tüm ekranlar açık temada da doğrulanacak.
   Eski sürümde açık tema fiilen bozuktu (sabit dark token'lar).

## 10. "Benzer uygulamada da aynısını yapar mıydım?" — öz-eleştiri
İlk taslakta "koyu zemin + turkuaz vurgu + yuvarlak kartlar" düşünülürdü → **evet, herhangi
bir araç uygulamasına uygulanırdı.** Değiştirilenler:
- Turkuaz yerine **petrol + pirinç ikilisi**: pirinç sinyal lambası, ölçüm aleti dünyasından
  gelir; jenerik "modern uygulama" paletinde pirinç yoktur.
- Yuvarlak kart yerine **şekil hiyerarşisi**: veri öğesi 4dp (teknik), birincil eylem stadium
  (fiziksel tuş). Kartlar panele/satıra dönüştü.
- Genel bir "değer gösterimi" yerine **tik raylı Değer Okuması**: uygulamanın gerçek işi
  olan "sayıyı değiştir"i görselleştirir. Başka uygulamada anlamı olmaz.
- Roboto/Inter yerine **Barlow + JetBrains Mono**: ikincisi uygulamanın malzemesi config
  metni olduğu için *işlevsel* zorunluluk, dekoratif tercih değil.
