# Doğrulama kaydı — 7 Kasım 2026

## Otomatik kontroller

- Core engine: 66 birim testi, hata yok.
- Config editörünün JSON/XML/INI alan ağacı: 4 birim testi, hata yok.
- Gerçek Room veritabanı ve profil JSON aktarımı: Android emülatöründe ve son olarak bağlı fiziksel cihazda 3 entegrasyon testi, hata yok.
- `lintDebug`: hata yok. Eksik eski çeviriler ve mevcut kaynak uyarıları sürüyor.
- `assembleDebug` ve `assembleRelease`: başarılı. Release keystore tanımlı olmadığında unsigned APK üretilir.

Çalıştırılan komut:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease :core:core-engine:connectedDebugAndroidTest
```

## Gerçek Shizuku ile elle doğrulanan akış

Android emülatörü (API 37), resmî Shizuku 13.6.0 ve ADB/shell yetkisi kullanıldı. Test yalnızca `/sdcard/Download/omnigfx-e2e.ini` örnek dosyası üzerinde yapıldı.

1. Shizuku sağlayıcısı bağlandı; erişim isteği onaylandı ve ana ekranda hazır durumu görüldü.
2. Uygulama seçici → mutlak dosya yolu → çekme/analiz → INI editörü açıldı.
3. `Graphics/fps` değeri 30'dan 60'a değiştirildi; geri al / ileri al ve slider pinleme kullanıldı.
4. Dinamik profil kaydedildi ve uygulandı. Dosya içeriği ADB üzerinden okunarak 60 değeri doğrulandı; aynı dizinde işlem öncesi `.bak` dosyası görüldü.
5. Profiller listesinden seçilen profil doğrudan uygulama penceresini açtı. Slider ile 52 değeri uygulandı; gerçek dosyada 52 görüldü.
6. Yedekler ekranından geri yükleme yapıldı; gerçek dosyada önceki 60 değeri görüldü. Daha eski yedekten 30 değerine dönüş de doğrulandı.
7. Sistem dosya seçicisiyle bir profil JSON'a dışa aktarıldı. JSON içeriği incelendi; aynı dosya içe aktarıldı ve yeni profil ana listede göründü.
8. Logcat AndroidRuntime hata kaydında çökme görülmedi.

## Kapsam sınırları

Fiziksel cihaz/OEM çeşitliliği, root modunda başka uygulamaların özel dizinleri ve mağaza yayını doğrulanmadı. ADB modunda özel uygulama dizinlerine erişim Android izinleriyle sınırlıdır. Bulut servisi yapılandırılmamıştır; JSON aktarımı cihazda çalışır. Üretim imzası için geliştiricinin keystore'u gereklidir.

Gerçek oyun dosyalarıyla sonraki doğrulama: [Galaxy A20 / Android 13 cihaz raporu](DEVICE_VERIFICATION.md).
