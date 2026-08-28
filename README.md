# 🚀 OmniGFX Pro — Kurulum ve Kullanım Kılavuzu

OmniGFX Pro, modern Android oyunları için geliştirilmiş, Shizuku tabanlı, gelişmiş ve güvenli bir GFX / Config yönetim aracıdır.

---

## 📦 Paket İçeriği

* **`OmniGFX_v2.0_debug.apk` / `app-debug.apk`**: Projenin derlenmiş, kuruluma hazır debug APK dosyası.
* **`GEREKLI_KODLAR.md`**: Tüm 8 fazın kaynak kodlarını içeren tam kod yedeği.
* **`shizuku.apk`**: Gerekli Shizuku servisi kurulum paketi (varsa).
* **`README.md`**: Bu kılavuz.

---

## 📲 APK Kurulumu & Çalıştırma

1. **APK'yı Telefona Yükleyin:**
   * `OmniGFX_v2.0_debug.apk` dosyasını Android cihazınıza kopyalayın ve yükleyin (veya `adb install OmniGFX_v2.0_debug.apk` komutunu çalıştırın).
2. **Shizuku'yu Başlatın:**
   * Cihazınızda **Shizuku** uygulamasını açın.
   * **Kablosuz Hata Ayıklama (Wireless Debugging)**, **ADB** veya **Root** yöntemiyle Shizuku servisini başlatın.
3. **İzin Verin:**
   * OmniGFX uygulamasını açtığınızda Shizuku izin istemi görünecektir. "Her zaman izin ver" seçeneğini onaylayın.
4. **Kullanmaya Başlayın:**
   * **Oyunlar** sekmesinden "+" butonuna basarak yüklü bir oyunu seçin.
   * 5 adımlı sihirbaz (Hedef Dosya Seçimi → Çekme & Analiz → Akıllı Editör → Pinleme & Profil Kaydetme → Güvenli Yazma) üzerinden istediğiniz optimizasyonları uygulayın.

---

## 🛠️ Projeyi Sıfırdan Tekrar Derlemek İsterseniz (Geliştirici Notları)

Tüm kaynak kodlar `GEREKLI_KODLAR.md` içerisinde modül ve dosya yollarına göre eksiksiz saklanmaktadır. Projeyi yeniden oluşturmak için:

1. **Gereksinimler:**
   * JDK 17 (Örn: `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot`)
   * Android SDK (API 34/35)
   * Gradle 8.x
2. **Derleme Komutu:**
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot"
   .\gradlew.bat assembleDebug
   ```
3. **Oluşan APK Konumu:**
   `app/build/outputs/apk/debug/app-debug.apk`

---

## 🌟 Başlıca Özellikler

* **5 Adımlı Lineer Sihirbaz:** Dağınıklık olmadan adım adım config çekme, düzenleme ve geri yazma.
* **Çoklu Format Desteği:** INI, JSON, XML ve Düz Metin formatlarını otomatik algılama ve akıllı parse etme.
* **Akıllı UI Pinleme:** Değişkenleri Slider, Dropdown, Toggle veya Metin Girişi olarak pinleyip profil kaydetme.
* **Dinamik & Ham Profil Desteği:** İster slider'lı dinamik profiller, ister tek tıkla tam dosya yedeği (Ham Dosya) uygulama.
* **Gelişmiş Güvenlik:** Shizuku Atomic Push, UID/GID & SELinux Context koruma, Hash doğrulama ve otomatik Rollback.
* **Modern Material 3 UI:** Karanlık tema uyumlu, canlı Diff önizlemeli ve akıcı animasyonlu arayüz.
