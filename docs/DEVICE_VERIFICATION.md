# Fiziksel cihaz doğrulaması — 7 Kasım 2026

Cihaz: Samsung Galaxy A20 / SM-A205F, Android 13 (API 33). Telefon rootlu olsa da test boyunca `su`, `adb root` veya root ile başlatılan Shizuku kullanılmadı. ADB oturumu UID 2000 (`shell`), Shizuku sunucusu `shell` kullanıcısı ve SELinux `Enforcing` olarak doğrulandı. Shizuku arayüzü de ADB modunu gösterdi. Oyunun özel `/data/data/com.tencent.ig` dizinine erişim reddedildi; harici oyun dizinine erişim başarılıydı.

Mevcut `com.basitce.gfx` kurulumu farklı imzalı olduğu için kaldırılmadı. Güncel kod ayrı `com.basitce.gfx.e2e` paketine kuruldu. Mevcut uygulama verileri korundu.

## Gerçek PUBG dosyalarında sonuçlar

Paket: `com.tencent.ig`. Her iki dosya da şu dizindeydi:

```text
/sdcard/Android/data/com.tencent.ig/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/
```

| Akış | Sonuç |
| --- | --- |
| Shizuku bağlantısı ve kullanıcı izni | Başarılı, ADB/shell modu |
| Bilinen oyun yolu ile dosya çekme | Gerçek `UserCustom.ini` okundu, 85 satır analiz edildi |
| Config editörü ve diff | Kodlanmış bir `r.MobileContentScaleFactor` satırı için 1.0 → 0.9 deneme değişikliği hazırlandı; diff yalnızca bir satır değişikliği gösterdi |
| Ham profil kaydetme ve güvenli yazma | Cihazdan tekrar çekilen dosya, beklenen 4363 baytlık içerikle birebir eşleşti |
| Yedekleme ve geri yükleme | Uygulamanın Yedekler ekranından geri yüklendi; özgün SHA-256 ile birebir eşleşti |
| Metadata | `10246:1015`, `660`, `u:object_r:sdcardfs:s0` değerleri korundu |
| Dinamik profil | Gerçek `UserSettings.ini` içindeki `SoundQualityType=1` değeri 0 yapıldı; dosyanın kalan içeriği değişmedi |
| Dinamik profil kontrolü | Kaydedilen profil yeniden açıldı; metin kontrolünden 1 değeri uygulandı, başarı ekranı görüldü ve dosya özgün içeriğiyle eşleşti |
| JSON dışa aktarma | Sistem dosya seçicisiyle iki profil `Download/OmniGFX-PUBG-Cihaz-0711.json` dosyasına kaydedildi; ham içerik ve dinamik UI şeması bulunduğu doğrulandı |
| JSON içe aktarma | Aynı dosyadan iki yeni kopya oluşturuldu; oyun dosyalarına kendiliğinden yazılmadı |
| Kalıcılık | Uygulama kapatılıp tekrar açıldı ve güncellendi; dört profil listede kaldı |
| Dosya gezgini | Harici oyun dizini açıldı; config dizinindeki gerçek dosyalar listelendi, `UserSettings.ini` gezginden seçilip başarıyla çekildi |

## Cihaz testinde bulunan ve düzeltilen sorunlar

1. **Tekrarlanan Unreal INI anahtarları:** PUBG `UserCustom.ini` çok sayıda aynı `+CVars` anahtarı içeriyor. Bu dosyalar ham profil olarak saklanıyor; parser, belirsiz dinamik güncellemeyi dosyaya yazmadan reddediyor. Bir regresyon testi eklendi.
2. **Android dosya listesi tarih biçimi:** Gezgin, `ls -la` çıktısındaki ISO tarihini doğru okuyamadığı için klasörleri boş gösterebiliyordu. Android ISO biçimi, geleneksel tarih biçimi ve bilinmeyen biçimde sade listeye geçiş için üç regresyon testi eklendi. Düzeltme aynı telefonda tekrar doğrulandı.
3. **Rootsuz başlangıç konumu:** Gezgin artık `/sdcard/Android/data/<paket>` dizininde başlıyor. Özel uygulama dizini varsayılan başlangıç konumu değil.

## İçerik bütünlüğü

`UserCustom.ini` özgün ve geri yüklenen SHA-256:

```text
3f436067a67295beed2e364e5d084ab0c0fbd856abe8e962025decc72246b9f0
```

Geçici deneme içeriğinin SHA-256 değeri:

```text
524c16b561cd25992cd07680e1393f11032a09dec2212e22c1dda503596da616
```

`UserSettings.ini` özgün ve test sonunda geri dönen SHA-256:

```text
1e925b9fcfb248ea2581d0c203601ebe7f2ea04d3195f497d6cb49d6fb050952
```

Oyun dosyalarının özgün kopyaları bilgisayarda ayrıca tutuldu; uygulamanın oyun dizininde oluşturduğu `.omnigfx.*.bak` yedekleri korundu. Test profilleri ve dışa aktarılan JSON telefonda inceleme için bırakıldı.

## Otomatik kontroller

- 66 core engine birim testi: başarılı (Unreal tekrar anahtar ve dosya gezgini regresyonları dahil).
- 4 editör alan ağacı birim testi: başarılı.
- Bağlı SM-A205F üzerinde 3 gerçek Room/JSON entegrasyon testi: başarılı, sıfır hata.
- Toplam: **73 başarılı test**.
- Debug/release derlemeleri ve `lintDebug`: başarılı.
- Her iki debug APK için imza doğrulaması: başarılı.
- Test için geçici değiştirilen ekran zaman aşımı ve USB uyanık kalma ayarları özgün değerlerine döndürüldü.

```bash
./gradlew :core:core-engine:connectedDebugAndroidTest testDebugUnitTest lintDebug :app:assembleDebug :app:assembleRelease
```

## Görsel kanıtlar

- [Tek satırlık PUBG diff önizlemesi](device-test/pubg-diff.png)
- [Yedekten geri yükleme ve doğrulama sonucu](device-test/pubg-restored.png)
- [Dinamik profil uygulama başarısı](device-test/pubg-dynamic-success.png)
- [Yeniden açılış sonrası dört kalıcı profil](device-test/profiles-persisted.png)
- [Düzeltilmiş gezginde gerçek PUBG dosyaları](device-test/pubg-browser.png)

## Onayın kapsamı

Bu cihazda, **root yetkisine başvurmadan ADB/Shizuku ile gerçek oyun config dosyalarının düzenlenmesi, profil uygulanması, yedeklenmesi, geri yüklenmesi ve JSON aktarımı doğrulandı**. Test, oyunda FPS artışı veya grafik değişikliğinin oyun motoru tarafından kullanıldığını ölçmez. Telefonun rootlu olması nedeniyle bu sonuç tüm rootsuz OEM/Android sürümlerine genellenemez; üretim desteği için tamamen rootsuz fiziksel cihazlarda da doğrulama gerekir.

Test paketini yeniden derlemek için:

```bash
./gradlew :app:assembleDebug -Pomnigfx.applicationId=com.basitce.gfx.e2e
```

Normal debug APK SHA-256:

```text
c36413f86506cef9e5b03835ac6065530723acb5c8e9bab7402d659847e9cba5
```

Cihaza kurulan test APK SHA-256:

```text
aad8cfa9ceeae06b57ee523949b4f84da238fc4b2e863c2ef926f04199ebd3fa
```
