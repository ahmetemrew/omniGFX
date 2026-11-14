# Tasarım Turu — İlerleme Hafızası

Proje: OmniGFX Pro (Shizuku tabanlı oyun config düzenleyici, Kotlin + Compose, 6 modül)
Tur başladı: 2026-11-14. Bu dosya turun TEK hafızasıdır; her anlamlı adımda güncellenir.

## Durum: F9 tam doğrulama YEŞİL + F10 teslim (2026-11-14, 6. oturum)

### 6. oturum bulguları ve yapılanlar
- [x] Skill'ler okundu (tasarim-temel → tasarim-compose → material-3-expressive).
- [x] Bağımsız doğrulama (verify6): flock -o ile clean assembleDebug testDebugUnitTest
      lintDebug assembleRelease → BUILD SUCCESSFUL, GRADLE_RC=0 (572 görev), testler 0 fail.
- [x] verify6 emülatör: CRASH_NONE ama TAP_MISS=12 — paylaşılan Medium_Phone AVD'sinde
      kardeş oturumların bıraktığı ekran/gezinme durumu dump koordinatlarını kaydırmış
      (Profiller sekmesi 2337'de, dokunuş boşa; shots 02–09 launcher). Tasarım regresyonu DEĞİL.
- [x] Kök neden + araç sertleştirme (tools/design_shots.sh, design_emulator_session.sh):
      • wm size/density reset (kardeş override'ını sıfırlar) — alt sekme koordinatlarını düzeltti.
      • navigation_mode ZORLANMADI (zorlama üst bar overflow dokunuşunu launcher'a kaydırıyordu).
      • tap() MISS'te bir kez yeniden dener (Compose popup yakalama gecikmesi).
      • Overflow akışı deterministik: her erişim öncesi start_app + Profiller (kaskad hatası yok).
      • Emülatör oturumu aktif kardeş emülatörü kapanana dek bekler (offline sayılmaz).
      • start_app settle 4s→6s (RAM baskısı altında yavaş boot).
- [x] verify8 (sertleştirilmiş araçlar): TAP_MISS_COUNT=0, SHOTS_RC=0, CRASH_NONE.
      22 görüntü taze ve geçerli; overflow menüsü (Yedekler/Profil analizi/İçe-dışa aktar) doğru açıldı.
- [x] Sayısal kanıt (verify8): layout audit 21 dump TEMIZ (RC=0); palet 22 görüntü TEMIZ
      (koyu #121A1B / açık #F4F7F6, petrol/pirinç/oksit token'ları). Ekran belirteçleri doğru.
- [x] Görsel: 01 (koyu) + 13 (açık) açılıp incelendi — kalibrasyon tezgâhı kimliği, tek CTA,
      pirinç nav lambası, semantik ikonlar; açık tema birinci sınıf.
- [x] Kod denetimi: Color(0x yalnız OmniColor.kt; fontSize yalnız OmniTypography.kt;
      yasaklı copy 0; emoji 0.
- [x] F8 REVIEW.md: 22 ekran ort 8.56–9.00, genel ≈8.88, en düşük kriter 8 (hiçbiri <7).
- [ ] F10: commit (araçlar + taze görüntüler + PROGRESS/REVIEW) + push + CI + QWEN_DESIGN_DONE.md.

## Durum (önceki): F8 puanlama + F9 tam doğrulama (2026-11-14, 5. oturum)

### 5. oturum bulguları ve yapılanlar
- [x] Skill'ler okundu (tasarim-temel → tasarim-compose → material-3-expressive).
- [x] 4. oturumun verify3 koşusu 07:31'de shot 02'den sonra KESİLMİŞ (tool timeout):
      after/03–20 eski build'den (06:43), 10–12 geçersiz launcher görüntüleri.
      → Tam doğrulama yeniden başlatıldı: nohup + flock -o kuyruğu
      (tools/design_verify_all.sh, log .qwen/tmp/design-verify4.log).
      Kilitte uzun orchestration oturumu + 2 emülatör oturumu önde.
- [x] Vision bridge bu oturumda ÇALIŞIYOR (transcription): 00 splash (açık zemin +
      marka: grafit daire, petrol pill, tik rayı + pirinç topuz), 01 ve 02 incelendi.
- [x] Sayısal analiz (design_analyze.py): 00/01/02 palet TEMİZ; 01/02 dump hedef TEMİZ.
- [x] Kod denetimi: nav aktif göstergesi secondaryContainer (pirinç lamba, bilinçli,
      kontrast 8.58); touchTarget 48dp heightIn ile; AutoMirrored 41 kullanım (RTL);
      mutlak Left/Right hizalama 0; strings.xml yasaklı copy 0; OmniTheme tek kapı;
      splash v31 + night kaynakları tutarlı; AGP 9.5.1 / Kotlin 2.4.21 / Gradle 9.9.1.
- [x] F9 verify4: BUILD SUCCESSFUL (warm cache, 572 görev), 76 test 0 fail,
      lintDebug koştu, assembleRelease yeşil; emülatör: 22 shot, TAP_MISS=0, CRASH_NONE.
      after/03–20 tazelendi; 10–12 artık geçerli (palet TEMIZ).
- [x] Sayısal audit: 21 dump TEMIZ (taşma 0, hedef ≥48dp, kenar ≥20dp, copy yerinde);
      22 palet TEMIZ. 42dp "KUCUK_HEDEF" sahte pozitifi: listede kenarda kesik kart;
      audit kenar-kesik düğümleri atlar (tam boyut kodda: rehber başlık 88dp).
- [x] Copy cilası: yedek boş başlığı "Yedek yok" → "Henüz yedek yok" (aile tutarlılığı).
- [x] F8: REVIEW.md dolduruldu — 22 ekran, ort 8.56–9.00, genel ≈8.88, en düşük kriter 8.
      Kanıt: vision (00/01/02/06) + sayısal audit (tümü) + palet (tümü) + kod.
- [ ] F9 son: verify5 (copy değişikliği sonrası) kuyrukta → taze 03 + yeşil build.
- [ ] F10: commit (2 anlamlı: kod+resimler; design/+tools/) + push + CI + QWEN_DESIGN_DONE.md.

## Durum (önceki): F8 puanlama + F9 yeniden doğrulama (2026-11-14, 4. oturum)

### 4. oturum bulguları ve yapılanlar
- [x] 3. oturumun verify2 logu incelendi: BUILD SUCCESSFUL (1m40s, GRADLE_RC=0),
      76 unit test yeşil (11 XML, fail=0). AMA:
      1) `CRASH_FOUND` satırı eski betik sürümünden kalma SAHTE POZİTİF şüphesi:
         betik 06:48'de (koşu 06:43'te bitti) düzeltilmiş; güncel emu-logcat.txt'de
         0 FATAL / 0 ANR. Kesin hüküm için yeni koşu bekleniyor.
      2) Shots 10–12 GEÇERSİZ: rehber bölümü yerinde açılan akordeon → `back; back`
         uygulamadan çıkarmış, TAP MISS'ler launcher'da görüntü üretmiş.
- [x] design_shots.sh düzeltildi: shot 09 sonrası `back; back` yerine `start_app`
      (deterministik ana ekran); TAP_MISS sayacı + miss varsa exit 1.
- [x] design_emulator_session.sh: CRASH_FOUND artık exit 1; SHOTS_RC de exit'e bağlı.
      design_verify_all.sh: SESSION_RC'yi dışarı taşıyor (önceki VERIFY_EXIT=0
      crash'ı gizliyordu).
- [x] Kod iddiaları yeniden doğrulandı: Color(0x tema dışı 0, fontSize tema dışı 0,
      emoji 0. Gezinti metinleri kaynakla eşleşiyor ("Uygulama ara…", "Oyun ekle").
- [x] Görsel inceleme başladı (vision bridge): 00 splash SİYAH KARE (geçersiz),
      01/02/03 incelendi. Bulgular ve DÜZELTMELER:
      1) Splash 0.6s'de çizilmemiş kare yakalıyordu → tools/design_splash.py:
         siyah kareleri atlar, alt şerit analiziyle gerçek splash'i doğrular.
      2) Oyunlar boş: hem boş durum butonu hem FAB "Oyun ekle" (çift CTA) →
         FAB yalnızca liste doluyken (MainScreen gamesEmpty + HomeScreen
         onEmptyStateChange callback).
      3) Profiller boş: davet butonu yoktu → "Oyun ekle" birincil butonu eklendi
         (mevcut akışa gezinme, yeni özellik değil).
      4) Yedekler boş: "Eski yedekleri temizle" anlamsızdı → yalnızca yedek
         varken gösteriliyor.
- [x] shot() artık her görüntüyle uiautomator dump da kaydediyor
      (.qwen/tmp/ui-dumps/NN.xml) — sayısal hizalama/hedef boyutu kanıtı.
- [x] İmza bileşenleri kod düzeyinde doğrulandı: OmniValueReadout + OmniTickRail
      (mono anahtar, üstü çizili eski değer, pirinç yeni değer, tik rayı),
      SignalGauge (semantik renk, sade dil, contentDescription, ≥48dp, autoMirror).
      StepPushApply copy: aktif fiil, özürsüz hata, "İçerik eşleşti" sade doğrulama.
      İkon vektörü ürün malzemesinden (= operatörü + tik rayı + pirinç topuz),
      gradyan 0; nav geçişleri yönlü + reduced-motion.
- [x] Copy birleştirme: 23 ilerleme metnindeki "..." → "…" (7 dosya; Barlow'da
      glif doğrulanmıştı, test bağımlılığı yok). Kalıntı 0.
- [x] Ek denetimler: IconButton'larda contentDescription dolu (21/21), null'lar
      dekoratif satır içi ikonlarda; ekranlarda sabit dp padding 0; font 7 dosya
      464 KB yerinde; strings.xml İngilizce/ünlem kalıntısı 0; adaptive ikon +
      monokrom tüm varyantlarda; enableEdgeToEdge aktif; OmniPrimaryButton
      stadium→12dp morph + loading + touchTarget; CI JVM 17 hizalı, son main
      koşusu yeşil (43490782401).
- [x] Palet analizi (tools/design_analyze.py palette): 18 geçerli görüntünün
      TAMAMI TEMIZ — baskın renkler DESIGN.md token'ı veya token karışımı;
      koyu #121A1B / açık #F4F7F6 doğru render.
- [x] F9 build: **BUILD SUCCESSFUL in 2m22s, GRADLE_RC=0** (07:31, clean
      assembleDebug testDebugUnitTest lintDebug assembleRelease; 572 görev).
      feature-wizard testleri taze koştu (cache invalid): 9/9 yeşil; core-engine
      67 test FROM-CACHE. Emülatör oturumu aynı flock içinde sürüyor.
      Log: .qwen/tmp/design-verify3.log.
- [ ] F8: after/ görüntülerini AÇIP incele + REVIEW.md puanla (şu an tümü boş "–").
      Vision bridge bütçesi tükendi → kalan ekranlar sayısal analiz (dump + PIL)
      ve kod incelemesiyle puanlanacak; vision dönünce görsel teyit.
- [ ] F10: commit + push + CI + QWEN_DESIGN_DONE.md.

## Durum (önceki): F8/F9 doğrulama sürüyor (2026-11-14, 3. oturum)

### 3. oturumda yapılanlar (bu tur)
- [x] Skill'ler yeniden okundu (tasarim-temel → tasarim-compose → material-3-expressive).
- [x] Kod düzeyinde tasarım iddiaları DOĞRULANDI:
      `Color(0x` yalnız OmniColor.kt (60 eşleşme, tek kaynak); `fontSize=` yalnız
      OmniTypography.kt; ekranlarda sabit `padding(N.dp)` 0; sabit Color.White/Black 0;
      eski Omni* dark token referansı 0; gradyan 0; emoji 0; "Hoş geldin/Harika/Oops/Veri yok" 0.
      GuideScreen'deki 5 "→" kod bloklarında (JetBrains Mono, bilinçli).
- [x] MainActivity (edge-to-edge + core-ui tema), OmniTheme (dinamik renk kapalı),
      NavHost yönlü geçişler + reduced-motion, MainScreen (anlamsal ikonlar, etiketli FAB),
      HomeScreen, SettingsScreen (gruplu liste), ShizukuSetupScreen, StepEditScreen,
      OmniTopBar (statusBarsPadding), OmniValueReadout/TickRail, SignalGauge incelendi — uygun.
- [x] tools/design_shots.sh: Shizuku rehber diyaloğu görüntüsü eklendi (07b).
- [x] tools/design_emulator_session.sh: splash yakalama + logcat FATAL/ANR taraması +
      boot fail logu eklendi; gerçek cihaz dalı korundu.
- [x] F9 ilk koşu: clean assembleDebug testDebugUnitTest lintDebug assembleRelease
      (tek flock, tools/design_verify_all.sh) → :app:compileDebugKotlin FAIL:
      1) GuideScreen.buildGuideSections() composable değilken MaterialTheme/OmniTheme
         renklerini okuyordu (17 hata). DÜZELTME: GuideAccent enum (PRIMARY/SECONDARY/
         SUCCESS/WARNING/INFO/ERROR) + @Composable color(); GuideSection.accent ve
         Comparison.leftAccent/rightAccent artık rol taşıyor, renk çizimde çözülüyor.
         Bonus: remember içinde bayatlayan tema rengi sorunu da kalktı.
      2) ProfileSyncScreen: ikon extension property'leri fully-qualified yazılmıştı
         (ArrowBack/FileUpload/FileDownload unresolved). DÜZELTME: normal importlar.
         Not: dosya bu arada yeniden yapılandırılmış (ViewModel aynı dosyaya taşınmış);
         yalnız ikon referanslarına dokunuldu.
- [ ] F9 ikinci koşu: flock kuyruğunda (bg, log .qwen/tmp/design-verify2.log).
- [ ] F8: emülatör oturumu (build bitince tek flock çağrısı) + after/ + REVIEW.md
- [ ] F10: commit + push + CI + QWEN_DESIGN_DONE.md
- Not: CI (check.yml) assembleRelease de çalıştırıyor → doğrulamaya eklenecek.
      Release imzası yalnız OMNIGFX_KEYSTORE_PATH env varsa; CI'da unsigned, sorun değil.

## Durum (önceki): F5/F6/F7 tamam, F8/F9 doğrulama sürüyor (2026-11-14, 2. oturum)

### 2. oturumda yapılanlar (bu tur)
- [x] GuideScreen copy: ~30 "→" oku düzeltildi (ayar yolu "›", terim "—",
      eylem dizileri cümleye çevrildi); kod bloklarındaki oklar mono fontta kaldı.
      "terminal/aç", "profil'e", "import et" gibi hatalar giderildi; başlıklar
      cümle düzenine çekildi. DESIGN.md §8'e glif kararı işlendi.
- [x] GuideScreen token göçü: ~45 sabit dp/radius → OmniSpacing/OmniRadius;
      accordion animasyonu OmniMotion + azaltılmış harekete saygılı.
- [x] StepSelectFileScreen token göçü (42 sabit dp) + copy ("ADB modu" →
      "Sınırlı erişim"); seçili/hata durumları container token'larına taşındı.
- [x] NavHost'a yönlü geçişler (slide+fade, OmniMotion, reduced-motion anında).
- [x] OmniPrimaryButton loading spinner rengi onPrimary'ye düzeltildi.
- [x] ShizukuGuideDialog copy + spacing token'ları.
- [x] design/SCREENS.md envanteri + tools/design_shots.sh (metin tabanlı gezinti).
- [x] design/NOTES.md yazıldı.
- [ ] F9: clean assembleDebug testDebugUnitTest lintDebug
      (1. koşu: feature-home'da `Icons.Outlined.SportsEsports` unresolved —
      modülde yalnızca material-icons-core vardı; **material-icons-extended
      eklendi**, 2. koşu flock kuyruğunda)
- [ ] F8: after/ görüntüleri + REVIEW.md puanlama
- [ ] F10: commit + push + CI + QWEN_DESIGN_DONE.md

### Önceki oturumdan doğrulanan durum
- Renkler: `Color(0x` yalnızca core-ui/theme/OmniColor.kt'da (tek kaynak).
- Ekranlarda fontSize/sp yok; eski Omni* dark token'larına referans yok.
- Emoji yok; "UID 2000/ROOT (UID 0)/ADB/shell" yalnızca kod yorumunda.
- İkon + monokrom + splash (values-v31, values-night) yeni kimlikle çizilmiş.
- ViewModel değişiklikleri tasarım kaynaklı (copy + initialValues + label);
  testler değişen metinlere bağımlı değil.

## Tamamlananlar
- [x] F0 BRIEF.md — ürün, kullanıcı, ana iş, dünya, kişilik, imza anları
- [x] F1 DESIGN.md — 6 isimli renk, açık/koyu roller, **kontrastlar hesaplandı**
      (tüm metin ≥4.5:1, ikon/kenar ≥3:1), tipografi, şekil hiyerarşisi, hareket, copy
- [x] Fontlar temin edildi ve TÜRKÇE KAPSAMI DOĞRULANDI:
      Barlow (Regular/Medium/SemiBold/Bold) + JetBrains Mono (Regular/Medium/Bold),
      OFL, Latin+Latin-ext alt küme, **7 dosya 464 KB**, `core-ui/res/font/`.
      Lisanslar `design/licenses/`. Barlow'da ok glifi yok → metinde ok kullanılmıyor.
- [x] F3 Tema katmanı TEK KAYNAK: `core-ui/theme/` OmniColor, OmniTypography,
      OmniShape, OmniSpacing, OmniMotion, OmniTheme.
      **app/ui/theme/ (Color.kt, Type.kt, OmniGfxTheme.kt) SİLİNDİ** — çelişkili
      ikinci palet buydu; MainActivity artık core-ui temasını kullanıyor.
- [x] F4 Bileşenler: OmniButton (stadium + basılı şekil morphu, 4 varyant),
      OmniCard/OmniEmphasisPanel/OmniSectionHeader, OmniEmptyState, OmniTopBar,
      OmniGameCard, OmniSlider, OmniSwitch, OmniDropdown,
      **OmniValueReadout + OmniTickRail (İMZA 1)**,
      **OmniShizukuBar → SignalGauge (İMZA 2)**,
      OmniIndicators (OmniStatusChip/OmniTone, OmniErrorCard, OmniLoading,
      OmniListRow, OmniCodeBlock)
- [x] Copy: `core-ui/res/values/strings.xml` baştan yazıldı — İngilizce kalıntılar
      (Backups/Marketplace/Sync/Restore/Delete/Analyze/Import/Path/Value/Dry Run…)
      Türkçeleştirildi, "UID 2000" gibi iç yapı terimleri kaldırıldı,
      "Hoş Geldin" → "Önce bağlantıyı kuralım".
- [x] MainScreen: tema farkındalıklı, ikonlar düzeltildi
      (Oyunlar=SportsEsports, Profiller=Tune), FAB → etiketli "Oyun ekle",
      sekme geçişi azaltılmış harekete saygılı.
- [x] HomeScreen: gereksiz "Oyunlarım" kartı kaldırıldı → bölüm başlığı + sayaç,
      liste `key` + `animateItem`, boş durum davetkâr.
- [x] **`:core:core-ui:compileDebugKotlin` RC=0, BUILD SUCCESSFUL in 16s**
      (flock -o ile, kilit ~420s beklendi).

## Ortam notu (önemli)
Kardeş proje `dersantasi` emülatörü KULLANIYORDU (kilit dolu, 6 bekleyen, canlı
uiautomator dump). Emülatöre dokunulmadı. Şu an cihaz yok, kilit sırayla alınıyor.
Ekran görüntüleri için kendi oturumum tek flock çağrısında başlatılıp kapatılacak.

## Sıradaki iş
F5: kalan ~20 ekran. Önce mekanik token göçü (sabit Omni* dark token →
MaterialTheme.colorScheme / OmniTheme.semantic), sonra ekran bazında tasarım.


## Okunan skill'ler
- [x] `.qwen/skills/tasarim-temel/SKILL.md`
- [x] `.qwen/skills/tasarim-compose/SKILL.md`
- [x] `.qwen/skills/material-3-expressive/SKILL.md`

## Faz planı
- [ ] F0 Keşif + BRIEF.md
- [ ] F1 DESIGN.md (token sistemi) + font/lisans
- [ ] F2 SCREENS.md envanteri + `design/before/` görüntüleri
- [ ] F3 Tema/token katmanı (tek kaynak)
- [ ] F4 Ortak bileşenler
- [ ] F5 Ekranlar (en çok kullanılana göre sıra)
- [ ] F6 İmza anı + ikon + splash
- [ ] F7 Copy geçişi
- [ ] F8 `design/after/` + REVIEW.md puanlama (>=8.5 ort, hiçbir kriter <7)
- [ ] F9 build + test + lint + emülatör doğrulama
- [ ] F10 commit + push + CI yeşil + QWEN_DESIGN_DONE.md

## Keşif bulguları (ilk okuma, kanıtlı)
1. **İki ayrı tema katmanı var, değerleri ÇELİŞİYOR:**
   `core-ui/theme/OmniColor.kt` (OmniBackground `#09090B`) ve
   `app/ui/theme/Color.kt` (OmniBackgroundDark `#0B0F14`).
   `OmniTypography` ve `OmniShapes` her ikisinde de kopya. → tek kaynağa indirilecek.
2. **Açık tema fiilen bozuk:** `MainScreen`, `OmniGameCard`, `OmniShizukuBar`
   renkleri `MaterialTheme.colorScheme` yerine sabit `Omni*` dark tokenlarından
   alıyor → light scheme seçilse bile ekranlar koyu kalıyor.
3. **AI-slop (tasarim-temel §2) mevcut haliyle:**
   - madde 2: neredeyse siyah zemin (`#09090B`) + fosforlu yeşil (`#00E5A8`) / mor (`#7C4DFF`)
   - madde 3: mor gradyan ikon placeholder (`OmniPrimary → OmniPrimaryGlow`)
   - madde 4: her kart aynı 20dp radius + aynı kenarlık
   - madde 5: `"Shizuku hazır • ADB/shell (UID 2000)` orta noktalı meta dizisi
   - madde 11: varsayılan mor ailesi, şablon paleti
4. **Tipografi:** özel font YOK (sistem Roboto), hiçbir stilde `lineHeight` yok,
   `FontWeight` ekranlarda `.copy()` ile tekrar tekrar eziliyor.
5. **Anlamsal ikon hatası:** alt barda Oyunlar=`Icons.Default.Home`,
   Profiller=`Icons.Default.Person`; boş durumda `Icons.Default.Settings`.
6. **Gereksiz kart:** HomeScreen'de sadece "Oyunlarım" başlığını saran `OmniCard`.
7. **Copy:** "UID 2000", "ROOT (UID 0)" gibi iç yapı terimleri son kullanıcıya gösteriliyor.

## Sıradaki iş
F0 BRIEF.md yaz, font temin edilebilirliğini doğrula (ağ + Latin-ext/Türkçe),
sonra F1 DESIGN.md.
