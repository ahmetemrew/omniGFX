STATUS: IN_PROGRESS

7 Kasım 2026 çoklu oyun testi. Kullanıcı gerçek oyun dosyalarına müdahaleyi ve yerel APK'lardan başka oyunlar kurulmasını açıkça yetkilendirdi. Ana amaç tek telefonda Shizuku ile root yetkisi kullanmadan çalışmayı dürüstçe sınamak.

- Bağlı cihaz: R58M51WS98M, SM-A205F / Android 13 / rootlu test telefonu. Root yetkisi KULLANILMAYACAK: su, adb root, run-as ile başka oyun dosyası okuma, chmod/chown ile izin atlatma veya APK koruma atlatma yok.
- OmniGFX test paketi com.basitce.gfx.e2e. İmzalar farklı olduğundan eski com.basitce.gfx kurulumu korunuyor.
- Önceki PUBG gerçek dosya düzenleme, ham/dinamik profil, yedekten dönüş, SHA-256 ve JSON testleri başarılı. Rapor: docs/DEVICE_VERIFICATION.md. PUBG dosyaları özgün hash değerlerine geri döndü.
- Yeni oyunların beşi de kuruldu: com.dvloper.granny; com.keplerians.evilnun; com.eyesthegame.eyes; air.com.RustyLake.CubeEscapeCollection; com.kiloo.subwaysurf. APK kaynağı /home/ahmete/Belgeler/ChatGPT/algoritma-ogren/apks. Kurulum kanıtı /tmp/omni-multigame/install-results.json.
- Henüz yeni oyunların ilk açılışı/config analizi yapılmadı.
- Wi-Fi başlangıçta kapalı, uçak modu açık. Kullanıcı Wi-Fi açılmasını onayladı; kayıtlı ağa bağlanıldı. Uçak modu değiştirilmedi. Ağ kimlik bilgilerini rapora koyma.
- Shizuku tek telefonda kablosuz eşleştirme BAŞARILI. Settings eşleme kodu Shizuku bildirimine girildi; Eşleştirme başarılı görüldü. Shizuku UI > Diğer seçenekler > Shizuku’yu durdur ile eski servis durduruldu (ps listesinde yokluğu doğrulandı). UI > Kablosuz hata ayıklama ile başlat düğmesi ile yeni servis açıldı; Starting with wireless adb logu, starter exit 0 ve yeni PID 8403 görüldü. Root/su/adb root veya bilgisayardan servis başlatma komutu kullanılmadı. Kablosuz hata ayıklama test için açıldı (başlangıçta kapalıydı).
- /tmp/omni_device_ui.py ADB+UIAutomator yardımcı aracı; son sürüm null root çıktısını tekrar deneyip eski XML'e tıklamayı engeller, tap sonrası 0.6s bekler. Fiziksel cihazda klavye XML'de görünmeyebilir: ekran görüntüsünü de kontrol et. Metin değiştirirken input keycombination 113 29 Ctrl+A çalışıyor.
- /tmp/omni-multigame/original-screen-timeout.txt ekran zaman aşımının başlangıç değerini tutar; test için 600000 yapıldı. Sonunda eski değeri geri getir. Kullanıcı Wi-Fi'yi pil için kapalı tutuyordu; test sonunda uygun şekilde eski kapalı haline getirip Shizuku'nun durumunu raporla.
- Kullanıcı 14:52 Europe/Istanbul otomatik devam istedi. Yerel systemd user timer kuruldu: omnigfx-resume-20261107-1452.timer. /home/ahmete/.local/share/omnigfx/resume-20261107-1452.py mevcut bu oturuma codex queue ile devam mesajı yollar; kota aşma yok. Bu dosya STATUS: COMPLETE olursa tekrar tetiklemez.

Yapılacaklar:
1. Shizuku kablosuz eşleştirme/telefon içinden başlatma testi.
2. Beş oyunu ilk kez açıp meşru grafik/ses seçeneklerini ve gerçek dosya yerlerini sına. Sadece erişilebilen gerçek oyun config'ini başarı say; telemetri/reklam JSON'unu veya elle uydurulmuş dosyayı GFX başarısı sayma.
3. Özel /data/data dosyalarına shell erişimi reddediliyorsa bunu destek sınırı olarak raporla, aşmaya çalışma.
4. Uygun dosyalarda OmniGFX UI ile değişiklik -> uygulama -> dosyadan doğrulama -> geri yükleme ve özgün hash kontrolü yap. Oyun menüsüyle gözlenebilir ayar etkisini mümkünse doğrula; ölçülmeyen FPS artışı iddia etme.
5. Bulguları docs/MULTIGAME_VERIFICATION.md içinde oyun bazlı sonuç tablosu, kanıt ve tek telefon başlangıç ayrımıyla yaz. Gereken uygulama hatalarını düzelt/test et, APK'ları tekrar üret ve test paketini güncelle.
6. Son durum raporu, kullanıcı ayarları ve değişen oyun dosyaları geri doğrulaması. STATUS: COMPLETE yap.

Kablosuz başlangıç güncellemesi: İlk PID 8403 kalıcı servis olamadı. İkinci kez aynı KABLOSUZ Başlat düğmesiyle başlatmada PID 8570, shell kullanıcısıyla kalıcı hale geldi. OmniGFX ana ekranda Shizuku hazır olarak doğrulandı. İlk deneme başarısızlığını raporda sakla; tek denemede kusursuz çalıştı deme. Yeni oyun testleri bu kablosuz başlatılmış oturumla yürütülecek.
