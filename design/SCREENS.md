# SCREENS — OmniGFX Pro ekran envanteri

Tur: 2026-11-14. `before/` önceki kimliğin görüntüleri (14 adet, koyu tema).
`after/` yeni kimlikle aynı senaryolar + açık tema + font ölçeği 2.0.
Betik: `tools/design_shots.sh` (tek flock çağrısı içinde emülatör başlat→kur→gez→kapat).

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
- Shizuku emülatörde çalışmaz → 12-16 ekranlarının "Shizuku yok" durumu emülatörde,
  dolu durumları gerçek cihazda (before/ görüntüleri) doğrulanır.
- Açık tema: 01/02/03/05/06/08 yeniden çekilir (after/ *-acik).
- Font ölçeği 2.0: 01/03 yeniden çekilir (after/ *-buyukfont), sonra 1.0'a döndürülür.
- Azaltılmış hareket: ANIMATOR_DURATION_SCALE=0 ile geçişler anında (kod düzeyinde
  OmniMotion.scale; görüntü farkı üretmez, davranışsal doğrulama).