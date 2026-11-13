# OmniGFX Pro

Android 8.0+ için Shizuku üzerinden metin tabanlı oyun yapılandırmalarını düzenleyen Kotlin / Jetpack Compose uygulaması.

## Kullanım

1. [Resmî Shizuku uygulamasını](https://shizuku.rikka.app/download/) kurun ve [başlatma rehberini](https://shizuku.rikka.app/guide/setup/) izleyin.
2. OmniGFX'i açın; Shizuku erişim isteğini onaylayın.
3. **Oyunlar → +** ile uygulamayı seçin. Dosya gezgininden veya mutlak yol girerek config dosyasını belirleyin.
4. Dosyayı çekip analiz edin, değerleri düzenleyin. Geri al / ileri al ve fark önizlemesini kullanabilirsiniz.
5. İstediğiniz alanları slider, açılır liste, anahtar veya metin alanı olarak pinleyin. Profil adı verip kaydedin.
6. **Uygula** ile dosyayı yedekleyip yazın. SHA-256 doğrulaması başarısızsa işlem öncesi içerik geri yüklenir ve tekrar doğrulanır.

**Profiller** sekmesinde kayıtlı profili seçerek uygulama penceresini açabilirsiniz. Dinamik profilin pinlenmiş değerleri bu pencerede değiştirilebilir. Menüden **Yedekler**, **JSON Profil Analizi** ve **İçe / Dışa Aktar** ekranlarına ulaşılır.

INI ve düz metinde satır ekleme, silme ve sıralama desteklenir. Aynı bölümde tekrarlanan INI anahtarları (ör. Unreal `+CVars`) satırların korunması için ham profil olarak saklanır. JSON/XML'de mevcut alanların değerleri düzenlenir; tekrar eden XML elemanları ve JSON kök dizileri desteklenir. Satır yapısını değiştiren profiller tam içerik olarak saklanır. Ham profil de editördeki son içeriği kaydeder.

## Dosya erişimi ve veri saklama

- ADB ile başlayan Shizuku, Android shell kullanıcısının izinlerine sahiptir. Başka uygulamaların `/data/data` dizinlerine erişim için genellikle root ile çalışan Shizuku gerekir. Shizuku tüm dosya erişim kısıtlamalarını kaldırmaz.
- Desteklenen içerik UTF-8 metindir; ikili/sıkıştırılmış/şifreli oyun dosyaları düzenlenmez. Sihirbaz en fazla 10 MB kabul eder.
- Yazmadan önce dosyanın aynı dizininde `.dosya.omnigfx.<zaman>.bak` yedeği alınır. Yedek oluşturulamazsa varsayılan olarak yazma iptal edilir. Yerel kopya uygulamanın özel alanında saklanır.
- Sihirbaz, çekilen dosya ile uygulama anındaki dosya farklıysa yazmayı durdurur. Aktif oyunun dosyayı yeniden yazmaması için oyunu kapatın.
- UID/GID, dosya izinleri ve mevcut SELinux bağlamı korunmaya çalışılır; atomik değişimden önce geçici dosyanın metadatası kontrol edilir. Bazı dosya sistemlerinde atomik yeniden adlandırma mümkün olmayabilir.
- Profiller Room veritabanında saklanır. Eski JSON profilleri de aktarım ve yedek ekranlarında okunabilir.
- JSON dışa aktarma ham içerik ve UI şemasını da içerir. İçe aktarma yeni profil kimlikleri üretir ve kendiliğinden oyun dosyalarına yazmaz.
- Bu sürüm cihazda çalışır. Bulut sunucusu/kimlik doğrulama yapılandırılmamıştır; bellek içi simülatör üretim servisi olarak kullanılmaz.

## Derleme

Gereksinimler: JDK 17+, Android SDK Platform 37 ve Android SDK Build Tools. Gradle 9.9.1 wrapper depoda bulunur. SDK yolunu `ANDROID_HOME` veya git'e eklenmeyen `local.properties` içindeki `sdk.dir` ile belirtin.

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest lintDebug
./gradlew assembleRelease
```

Windows'ta `gradlew.bat` kullanın. Çıktılar:

- Kurulabilir debug: `app/build/outputs/apk/debug/app-debug.apk`
- İmza verilmemiş release: `app/build/outputs/apk/release/app-release-unsigned.apk`

Release, debug anahtarıyla imzalanmaz. Üretim imzası için şu ortam değişkenlerini tanımlayın ve `assembleRelease` çalıştırın:

```text
OMNIGFX_KEYSTORE_PATH
OMNIGFX_KEYSTORE_PASSWORD
OMNIGFX_KEY_ALIAS
OMNIGFX_KEY_PASSWORD
```

Bu değerleri veya keystore dosyasını depoya eklemeyin. Üretim anahtarı tanımlıysa release çıktısı imzalı üretilir.

## Doğrulama

```bash
# Parser, dosya yolu, güvenlik taraması, diff, pipeline ve editör alan yolları
./gradlew testDebugUnitTest

# Bağlı emülatör/cihazda gerçek Room veritabanı ve profil JSON aktarımı
./gradlew :core:core-engine:connectedDebugAndroidTest

# Statik Android kontrolleri ve iki APK varyantı
./gradlew lintDebug assembleDebug assembleRelease
```

Birim testleri yazma öncesi yedekleme, doğrulama istisnasında geri dönüş, dosya çakışması ve kısmi regex işleminin engellenmesini kapsar. Entegrasyon testleri sihirbaz profillerinin ortak depodan okunmasını, içe aktarılanların ana listede görünmesini, silinmesini ve JSON veri tiplerinin korunmasını doğrular.

GitHub Actions kontrolleri push/PR üzerinde derleme, birim testi ve lint çalıştırır. `v*` etiketinde debug APK yayınlayan mevcut release iş akışı da bu kontrolleri çalıştırır. Eski dil kaynaklarındaki eksik çeviriler varsayılan metne düşer ve lint uyarısı olarak raporlanır; Compose akışları ağırlıklı Türkçedir.

`GEREKLI_KODLAR.md` tarihsel kaynak yedeğidir; güncel uygulama `app/`, `core/` ve `feature/` altındadır.

Gerçek Galaxy A20 / Android 13 üzerinde ADB modu ve PUBG dosyalarıyla yapılan testler: [Fiziksel cihaz doğrulaması](docs/DEVICE_VERIFICATION.md).
