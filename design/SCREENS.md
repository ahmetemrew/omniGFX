# SCREENS — OmniGFX Pro ekran envanteri

Tur: 2026-11-14. `before/` önceki kimliğin görüntüleri (14 adet, koyu tema).
`after/` yeni kimlikle aynı senaryolar + açık tema + font ölçeği 2.0.
Betik: `tools/design_shots.sh` (tek flock çağrısı içinde emülatör başlat→kur→gez→kapat).
Dolu durumlar: `tools/design_shots_filled.sh` + `design_filled_session.sh`
(Shizuku ADB modu + demo config ile sihirbazın dolu/başarı durumları emülatörde üretilir).
Dosya adı = ekran-durum; before/ ve after/ numaraları aynı slug haritasını kullanır.

## Ana gezinme (alt bar: Oyunlar / Profiller / Ayarlar)
| # | Ekran | Dosya | Durumlar |
|---|---|---|---|
| 01 | Oyunlar (ana) | feature-home/HomeScreen.kt | boş (davet + "Oyun ekle"), dolu (liste), Shizuku bar: hazır/izin/yok/denetleniyor |
| 02 | Profiller | app/ui/profile/ProfileListScreen.kt | boş, dolu, oyun filtresi |
| 03 | Ayarlar | app/ui/main/SettingsScreen.kt | bağlantı kartı, araç satırları, tema bilgisi |
| 04 | Shizuku kurulum | app/ui/shizuku/ShizukuSetupScreen.kt | durum kartı, adım listesi, izin iste |
| 05 | Rehber | app/ui/guide/GuideScreen.kt | bölüm listesi, bölüm detayı (callout/adım/kod bloğu/sözlük) |
| 06 | Marketplace | app/ui/marketplace/ProfileMarketplaceScreen.kt | boş, dolu, risk skoru |
| 07 | Aktarım (sync) | app/ui/sync/ProfileSyncScreen.kt | içe/dışa aktarma, boş |
| 08 | Yedekler | app/ui/backup/BackupManagerScreen.kt | boş, dolu, geri yükleme onayı |

## Sihirbaz ve detay
| # | Ekran | Dosya | Durumlar |
|---|---|---|---|
| 09 | Uygulama seçici | feature-wizard/AppPickerScreen.kt | liste, arama, boş sonuç |
| 10 | Oyun detay | feature-wizard/GameDetailScreen.kt | profiller, boş, yeni profil |
| 11 | Sihirbaz çerçevesi | feature-wizard/SetupWizardScreen.kt | adım göstergesi (gerçek sıra), ileri/geri |
| 12 | Dosya seç | feature-wizard/StepSelectFileScreen.kt | bilinen yollar, gezgin, Shizuku yok uyarısı |
| 13 | Çek & analiz | feature-wizard/StepPullAnalyzeScreen.kt | çekiliyor, bulundu, bulunamadı, hata |
| 14 | Düzenleyici | feature-wizard/StepEditScreen.kt | slider/dropdown/switch satırları, mono okuma, format seçimi |
| 15 | Pin & değişkenler | feature-wizard/StepPinVariablesScreen.kt | sabitlenenler, adlandırma, kaydet |
| 16 | Uygula | feature-wizard/StepPushApplyScreen.kt | özet + Değer Okuması, yazılıyor, başarı (hash), hata + geri alma |
| 17 | Gelişmiş pin sayfası | feature-wizard/AdvancedPinSheet.kt | alt sayfa: min/max/adım |
| 18 | Fark önizleme | feature-wizard/DiffPreviewSheet.kt | alt sayfa: diff satırları, onay |
| 19 | Profil uygulama sayfası | feature-wizard/ProfileApplySheet.kt | alt sayfa: özet + Değer Okuması, dry run, uygula |
| 20 | Shizuku rehber diyaloğu | core-ui/ShizukuGuideDialog.kt | diyalog: neden + tek eylem |

## Durum kapsama notları
- Dolu durumlar (21–26) Shizuku ADB modu ile emülatörde üretilir: Shizuku v13.6
  servisi resmi yoldan (native `libshizuku.so`) başlatılır, demo INI (`tools/design_demo_config.ini`)
  /sdcard/Download/UserCustom.ini olarak seed edilir, sihirbaz uçtan uca koşulur
  (çek → düzenle → pin → profil → uygula → yedek). Before/ karşılıkları gerçek
  cihazdan alınmış eski kimlik görüntüleridir; ekran ve durum eşleşmesi birebirdir.
- Açık tema: 01/02/03/05/06/08 yeniden çekilir (after/ *-acik).
- Font ölçeği 2.0: 01/03/05 yeniden çekilir (after/ *-buyukfont), sonra 1.0'a döndürülür.
- Azaltılmış hareket: ANIMATOR_DURATION_SCALE=0 ile geçişler anında (kod düzeyinde
  OmniMotion.scale; görüntü farkı üretmez, davranışsal doğrulama).

## Dolu durum seti (before/ ↔ after/ eşleşmesi)
| # | Ekran / durum | before/ (eski kimlik) | after/ (yeni kimlik) |
|---|---|---|---|
| 21 | Çek & analiz — dosya çekildi | 21-cek-analiz-koyu | 21-cek-analiz-koyu |
| 22 | Düzenleyici — satırlar + pin | 22-duzenleyici-koyu | 22-duzenleyici-koyu |
| 23 | Uygula — başarı + doğrulama | 23-uygula-basari-koyu | 23-uygula-basari-koyu |
| 24 | Oyunlar — Shizuku hazır (boş liste) | 24-oyunlar-hazir-koyu | 24-oyunlar-hazir-koyu |
| 24b | Oyunlar — dolu liste (sihirbaz sonrası) | — (before karşılığı yok) | 24b-oyunlar-dolu-koyu |
| 25 | Profiller — dolu liste | 25-profiller-dolu-koyu | 25-profiller-dolu-koyu |
| 26 | Yedekler — dolu liste | 26-yedekler-dolu-koyu | 26-yedekler-dolu-koyu |

Not (9. oturum): before/24'ün gerçek içeriği OCR ile doğrulandı — "Shizuku hazır ·
ADB/shell (UID 2000)" bandı + BOŞ liste (before/01 ise "Shizuku izni gerekli" bandı).
Slug'daki "hazir" bağlantı durumudur, dolu liste değil. after/24 aynı durumda çekilir
(sihirbazdan önce, pm clear ile temiz veri); dolu liste kanıtı 24b'de (07b gibi
before karşılığı olmayan ek durum).