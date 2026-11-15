// Guide content quotes real Android config paths as documentation text, not file access.
@file:SuppressLint("SdCardPath")

package com.basitce.gfx.ui.guide

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Dangerous
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Games
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.basitce.gfx.core.core_ui.components.OmniTopBar
import com.basitce.gfx.core.core_ui.theme.OmniRadius
import com.basitce.gfx.core.core_ui.theme.OmniTheme

// ═══════════════════════════════════════════════════════════════
//  REHBER VERİ MODELLERİ
// ═══════════════════════════════════════════════════════════════

private enum class CalloutType { INFO, TIP, WARNING, DANGER, CODE }

/**
 * Bölüm vurgusu bir renk değil roldür: veri modeli composable renk taşımaz,
 * renk çizim anında temadan çözülür (tema değişince bayatlamaz).
 */
private enum class GuideAccent {
    PRIMARY, SECONDARY, SUCCESS, WARNING, INFO, ERROR;

    @Composable
    fun color(): Color = when (this) {
        PRIMARY -> MaterialTheme.colorScheme.primary
        SECONDARY -> MaterialTheme.colorScheme.secondary
        SUCCESS -> OmniTheme.semantic.success
        WARNING -> OmniTheme.semantic.warning
        INFO -> OmniTheme.semantic.info
        ERROR -> MaterialTheme.colorScheme.error
    }
}

private data class GuideParagraph(
    val text: String,
    val isBold: Boolean = false,
    val isCode: Boolean = false
)

private data class GuideCallout(
    val type: CalloutType,
    val title: String,
    val body: String
)

private data class GuideStep(
    val number: Int,
    val title: String,
    val description: String,
    val code: String? = null
)

private sealed class GuideBlock {
    data class Text(val paragraphs: List<GuideParagraph>) : GuideBlock()
    data class Callout(val callout: GuideCallout) : GuideBlock()
    data class Steps(val steps: List<GuideStep>) : GuideBlock()
    data class BulletList(val items: List<String>) : GuideBlock()
    data class CodeBlock(val code: String, val caption: String? = null) : GuideBlock()
    data class Comparison(
        val leftTitle: String,
        val leftItems: List<String>,
        val leftAccent: GuideAccent,
        val rightTitle: String,
        val rightItems: List<String>,
        val rightAccent: GuideAccent
    ) : GuideBlock()
}

private data class GuideSection(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val blocks: List<GuideBlock>
)

// ═══════════════════════════════════════════════════════════════
//  ANA EKRAN
// ═══════════════════════════════════════════════════════════════

@Composable
fun GuideScreen(onBack: () -> Unit) {
    val sections = remember { buildGuideSections() }
    val expandedSections = remember { mutableStateOf(emptySet<String>()) }
    val listState = rememberLazyListState()
    val spacing = OmniTheme.spacing
    val motion = OmniTheme.motion
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            
            .navigationBarsPadding()
    ) {
        OmniTopBar(
            title = "Kullanım rehberi",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )

        // İçindekiler kutusu YOK: bölümlerin kendisi zaten başlık + alt başlık taşıyan
        // bir liste. Aynı içeriği iki kez listelemek hem yer israfı hem kesilme riskiydi.
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = spacing.screen, end = spacing.screen,
                top = spacing.md, bottom = spacing.xxxl
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.listItem)
        ) {
            // ─── HERO ──────────────────────────────────────
            item { HeroSection() }

            // ─── BÖLÜMLER (genişleyebilir satırlar) ─────────
            itemsIndexed(sections, key = { _, s -> s.id }) { index, section ->
                SectionCard(
                    section = section,
                    number = index + 1,
                    isExpanded = expandedSections.value.contains(section.id),
                    onToggle = {
                        val current = expandedSections.value.toMutableSet()
                        if (current.contains(section.id)) {
                            current.remove(section.id)
                        } else {
                            current.add(section.id)
                            // Açılan kart görünür alana girer: içerik alt kenarda
                            // kesik başlamaz (tur 2 P0-3). Hero item 0 → index+1.
                            scope.launch {
                                if (motion.reduced) {
                                    listState.scrollToItem(index + 1)
                                } else {
                                    listState.animateScrollToItem(index + 1)
                                }
                            }
                        }
                        expandedSections.value = current
                    }
                )
            }

            // ─── FOOTER ───────────────────────────────────
            item { FooterNote() }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  HERO BÖLÜMÜ
// ═══════════════════════════════════════════════════════════════

@Composable
private fun HeroSection() {
    val spacing = OmniTheme.spacing
    // Büyük fontta hero kompaktlaşır: bölüm kartları ilk ekranda tam görünür
    // (tur 2 P0-3). İçerik aynı kalır, yalnız boşluk sıkılaşır.
    val largeFont = LocalDensity.current.fontScale > 1.15f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(OmniRadius.lg))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(if (largeFont) spacing.lg else spacing.xl)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(spacing.touchTarget)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Book,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(spacing.md))
                Column {
                    Text(
                        text = "OmniGFX rehberi",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Sıfırdan ustalaşmaya",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(if (largeFont) spacing.sm else spacing.lg))
            Text(
                text = "Bu rehber, oyunu hiç bilmeyen bir kullanıcının bile adım adım ilerleyebilmesi için hazırlandı. Her bölüm kendi içinde bağımsızdır; istediğin yerden başlayabilirsin. İleri düzey konular da ayrı başlıklar altında detaylıca anlatıldı.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  SECTION CARD (ACCORDION)
// ═══════════════════════════════════════════════════════════════

/**
 * Bölüm satırı: teknik el kitabı kimliği. İkon kutuları tek stil (yüzey adımı +
 * nötr ikon rengi) — rastgele renkli kutular yok. Sıra numarası gerçek bir sıra
 * olduğu için meşru (kılavuz bölümleri 01..09).
 */
@Composable
private fun SectionCard(
    section: GuideSection,
    number: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val spacing = OmniTheme.spacing
    val motion = OmniTheme.motion
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(OmniRadius.lg))
            .background(colors.surface)
            .border(
                width = if (isExpanded) 1.5.dp else 1.dp,
                color = if (isExpanded) colors.primary.copy(alpha = 0.5f)
                else colors.surfaceContainerHighest,
                shape = RoundedCornerShape(OmniRadius.lg)
            )
    ) {
        // ─── HEADER (tıklanabilir) ─────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "%02d".format(number),
                style = OmniTheme.mono.micro,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.widthIn(min = 24.dp)
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(OmniRadius.sm))
                    .background(colors.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface
                )
                Text(
                    text = section.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = if (isExpanded) "Daralt" else "Genişlet",
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }

        // ─── İÇERİK (animasyonlu, azaltılmış harekette anında) ─────────────────────
        AnimatedVisibility(
            visible = isExpanded,
            enter = if (motion.reduced) EnterTransition.None
            else expandVertically(motion.mediumTween()) + fadeIn(motion.mediumTween()),
            exit = if (motion.reduced) ExitTransition.None
            else shrinkVertically(motion.mediumTween()) + fadeOut(motion.mediumTween())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.xl, vertical = spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.lg)
            ) {
                section.blocks.forEach { block ->
                    when (block) {
                        is GuideBlock.Text -> TextBlock(block)
                        is GuideBlock.Callout -> CalloutBlock(block.callout)
                        is GuideBlock.Steps -> StepsBlock(block.steps)
                        is GuideBlock.BulletList -> BulletListBlock(block.items)
                        is GuideBlock.CodeBlock -> CodeBlock(block)
                        is GuideBlock.Comparison -> ComparisonBlock(block)
                    }
                }
                Spacer(modifier = Modifier.height(spacing.sm))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  BLOK RENDERER'LARI
// ═══════════════════════════════════════════════════════════════

@Composable
private fun TextBlock(block: GuideBlock.Text) {
    val spacing = OmniTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        block.paragraphs.forEach { p ->
            if (p.isCode) {
                SelectionContainer {
                    Text(
                        text = p.text,
                        style = OmniTheme.mono.key,
                        color = OmniTheme.semantic.onCodeSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(OmniRadius.xs))
                            .background(OmniTheme.semantic.codeSurface)
                            .padding(spacing.md)
                    )
                }
            } else {
                Text(
                    text = p.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (p.isBold) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

private data class CalloutStyle(
    val bgColor: Color,
    val borderColor: Color,
    val iconColor: Color,
    val bodyColor: Color,
    val icon: ImageVector
)

@Composable
private fun CalloutBlock(callout: GuideCallout) {
    val semantic = OmniTheme.semantic
    val colors = MaterialTheme.colorScheme
    val style = when (callout.type) {
        CalloutType.INFO -> CalloutStyle(
            colors.surfaceContainer, colors.outlineVariant,
            colors.onSurfaceVariant, colors.onSurface, Icons.Outlined.Info
        )
        CalloutType.TIP -> CalloutStyle(
            semantic.successContainer, semantic.success.copy(alpha = 0.5f),
            semantic.onSuccessContainer, semantic.onSuccessContainer, Icons.Outlined.Lightbulb
        )
        CalloutType.WARNING -> CalloutStyle(
            semantic.warningContainer, semantic.warning.copy(alpha = 0.5f),
            semantic.onWarningContainer, semantic.onWarningContainer, Icons.Outlined.Warning
        )
        CalloutType.DANGER -> CalloutStyle(
            colors.errorContainer, colors.error.copy(alpha = 0.5f),
            colors.onErrorContainer, colors.onErrorContainer, Icons.Outlined.Dangerous
        )
        CalloutType.CODE -> CalloutStyle(
            semantic.codeSurface, colors.outlineVariant,
            colors.primary, semantic.onCodeSurface, Icons.Outlined.Terminal
        )
    }
    val spacing = OmniTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(OmniRadius.md))
            .background(style.bgColor)
            .border(1.dp, style.borderColor, RoundedCornerShape(OmniRadius.md))
            .padding(spacing.lg)
    ) {
        Icon(
            imageVector = style.icon,
            contentDescription = null,
            tint = style.iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = callout.title,
                style = MaterialTheme.typography.titleSmall,
                color = style.iconColor
            )
            Spacer(modifier = Modifier.height(spacing.xs))
            SelectionContainer {
                Text(
                    text = callout.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = style.bodyColor
                )
            }
        }
    }
}

@Composable
private fun StepsBlock(steps: List<GuideStep>) {
    val spacing = OmniTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.listItem)) {
        steps.forEach { step ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(spacing.xxxl)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = step.number.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    step.code?.let { code ->
                        Spacer(modifier = Modifier.height(spacing.sm))
                        SelectionContainer {
                            Text(
                                text = code,
                                style = OmniTheme.mono.micro,
                                color = OmniTheme.semantic.onCodeSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(OmniRadius.xs))
                                    .background(OmniTheme.semantic.codeSurface)
                                    .padding(spacing.md)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BulletListBlock(items: List<String>) {
    val spacing = OmniTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        items.forEach { item ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(spacing.lg)
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun CodeBlock(block: GuideBlock.CodeBlock) {
    val spacing = OmniTheme.spacing
    Column {
        SelectionContainer {
            Text(
                text = block.code,
                style = OmniTheme.mono.key,
                color = OmniTheme.semantic.onCodeSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(OmniRadius.xs))
                    .background(OmniTheme.semantic.codeSurface)
                    .padding(spacing.lg)
            )
        }
        block.caption?.let {
            Spacer(modifier = Modifier.height(spacing.xs))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ComparisonBlock(block: GuideBlock.Comparison) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(OmniTheme.spacing.md)
    ) {
        ComparisonColumn(
            title = block.leftTitle,
            items = block.leftItems,
            color = block.leftAccent.color(),
            modifier = Modifier.weight(1f)
        )
        ComparisonColumn(
            title = block.rightTitle,
            items = block.rightItems,
            color = block.rightAccent.color(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ComparisonColumn(
    title: String,
    items: List<String>,
    color: Color,
    modifier: Modifier = Modifier
) {
    val spacing = OmniTheme.spacing
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(OmniRadius.md))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(OmniRadius.md))
            .padding(spacing.md)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = color
        )
        Spacer(modifier = Modifier.height(spacing.sm))
        items.forEach { item ->
            Row(modifier = Modifier.padding(vertical = spacing.xxs)) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier
                        .width(spacing.lg)
                        .size(16.dp)
                )
                Spacer(modifier = Modifier.width(spacing.sm))
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun FooterNote() {
    val spacing = OmniTheme.spacing
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(OmniRadius.lg))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
            .padding(spacing.xl)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Star,
                contentDescription = null,
                tint = OmniTheme.semantic.warning,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(spacing.md))
            Text(
                text = "Bir sorun mu yaşıyorsun? OmniGFX açık kaynaklıdır. GitHub üzerinden sorun kaydı açabilir veya topluluğa katılabilirsin.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  BÖLÜM İÇERİKLERİ
// ═══════════════════════════════════════════════════════════════

private fun buildGuideSections(): List<GuideSection> = listOf(

    // ─────────────────────────────────────────────────────────
    // 1. OMNIGFX NEDİR?
    // ─────────────────────────────────────────────────────────
    GuideSection(
        id = "what-is",
        icon = Icons.Outlined.Games,
        title = "OmniGFX nedir?",
        subtitle = "Uygulamanın ne yaptığını 2 dakikada anla",
        blocks = listOf(
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "OmniGFX, mobil oyunların grafik ve performans ayarlarını cihazın içinden " +
                    "değiştirmeni sağlayan bir araçtır. Oyunun kendi menüsünde görünmeyen, " +
                    "gizli kalmış ayarları açığa çıkarır.",
                ),
                GuideParagraph(
                    "Basit bir benzetme:", isBold = true
                ),
                GuideParagraph(
                    "Oyunun ayarlar menüsünü bir TV kumandası gibi düşün. Kumandayla sadece " +
                    "ses ve kanal değiştirirsin. Ama TV'nin arkasında bir servis menüsü vardır; " +
                    "orada renk kalibrasyonu, yenileme hızı, gizli modlar bulunur. OmniGFX, " +
                    "o servis menüsüne ulaşmanı sağlar."
                )
            )),
            GuideBlock.Callout(GuideCallout(
                type = CalloutType.INFO,
                title = "OmniGFX ne değildir?",
                body = "OmniGFX bir hile aracı değildir. Oyun hafızasına müdahale etmez, " +
                    "çok oyunculu maçlarda avantaj sağlamaz. Sadece oyunun kendi config " +
                    "dosyasındaki ayarları düzenler — tıpkı oyunun kendi ayarlar menüsünü " +
                    "kullanmak gibi, ama daha fazlasına erişir."
            )),
            GuideBlock.Text(listOf(
                GuideParagraph("OmniGFX'in yapabildikleri:", isBold = true)
            )),
            GuideBlock.BulletList(listOf(
                "Oyunun gizli FPS sınırlarını açma (örn: 90 FPS, 120 FPS)",
                "Çözünürlüğü cihazın desteklediği üst sınıra çıkarma",
                "Gölge, anti-aliasing, doku kalitesi gibi ince ayarlar",
                "Render mesafesini ve efekt detaylarını değiştirme",
                "Oyuna özel performans profilleri kaydetme"
            )),
            GuideBlock.Callout(GuideCallout(
                type = CalloutType.WARNING,
                title = "Root gerekli mi?",
                body = "Hayır. OmniGFX, Shizuku adlı bir araç aracılığıyla ADB Shell yetkisiyle " +
                    "çalışır. Bu, root yapmadan bilgisayardan tek bir komut girerek elde edilen " +
                    "bir yetki seviyesidir. Detaylar bir sonraki bölümde."
            ))
        )
    ),

    // ─────────────────────────────────────────────────────────
    // 2. SHIZUKU KURULUMU
    // ─────────────────────────────────────────────────────────
    GuideSection(
        id = "shizuku",
        icon = Icons.Outlined.Key,
        title = "Shizuku kurulumu",
        subtitle = "OmniGFX'in çalışması için gereken temel araç",
        blocks = listOf(
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "Shizuku, Android uygulamalarına özel yetkiler veren açık kaynaklı bir araçtır. " +
                    "OmniGFX, oyun dosyalarına erişmek için Shizuku'yu kullanır."
                ),
                GuideParagraph(
                    "Basit benzetme: Shizuku bir \"güvenlik görevlisi\" gibidir. " +
                    "Bir binaya (oyunun dosyalarına) girmek istiyorsun ama kapı kilitli. " +
                    "Shizuku, kimliğini doğruladıktan sonra sana anahtarı verir. " +
                    "Bu anahtar her telefon yeniden başlatıldığında tekrar alınmalıdır."
                )
            )),
            GuideBlock.Callout(GuideCallout(
                type = CalloutType.TIP,
                title = "İki kurulum yöntemi var",
                body = "Yöntem A: Bilgisayar gerekli (her Android sürümü için çalışır)\n" +
                    "Yöntem B: Bilgisayarsız (sadece Android 11 ve üzeri, Wireless ADB ile)\n\n" +
                    "Hangi Android sürümünü kullandığını bilmiyorsan Ayarlar › Telefon Hakkında " +
                    "bölümünden kontrol edebilirsin."
            )),

            // ── Yöntem A: Bilgisayar ile ──
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "Yöntem A: Bilgisayar ile kurulum",
                    isBold = true
                ),
                GuideParagraph(
                    "Bu yöntem tüm Android sürümlerinde çalışır. Bir kez kurulum yapman " +
                    "yeterlidir; Shizuku servisi her telefon yeniden başlatıldığında " +
                    "aynı komutla tekrar başlatılmalıdır."
                )
            )),
            GuideBlock.Steps(listOf(
                GuideStep(
                    number = 1,
                    title = "Shizuku uygulamasını indir",
                    description = "Google Play Store'dan \"Shizuku\" uygulamasını yükle. " +
                        "Geliştirici adı: Rikka."
                ),
                GuideStep(
                    number = 2,
                    title = "Telefonunda USB Hata Ayıklama'yı aç",
                    description = "Ayarlar › Telefon Hakkında › Derleme Numarası'na 7 kez " +
                        "dokunarak Geliştirici Seçenekleri'ni aktif et. Sonra Ayarlar › " +
                        "Geliştirici Seçenekleri › USB Hata Ayıklama'yı aç."
                ),
                GuideStep(
                    number = 3,
                    title = "Bilgisayarına ADB sürücülerini kur",
                    description = "Windows için \"Universal ADB Driver\" veya telefonunun " +
                        "markasına özel sürücüyü kur. Mac/Linux'ta genellikle gerekmez."
                ),
                GuideStep(
                    number = 4,
                    title = "Telefonu USB ile bilgisayara bağla",
                    description = "Ekrana \"USB Hata Ayıklamasına izin verilsin mi?\" sorusu " +
                        "gelirse \"Her zaman izin ver\" seçeneğini işaretle ve Onayla."
                ),
                GuideStep(
                    number = 5,
                    title = "Bilgisayarında terminali aç ve şu komutu gir",
                    description = "Windows'ta CMD veya PowerShell, Mac/Linux'ta Terminal:",
                    code = "adb shell sh /storage/emulated/0/Android/data/moe.shizuku.privileged.api/start.sh"
                ),
                GuideStep(
                    number = 6,
                    title = "Shizuku'yu aç ve \"Çalışıyor\" yazısını gör",
                    description = "Uygulamada \"Shizuku is running\" yazısını görüyorsan " +
                        "kurulum başarılıdır."
                ),
                GuideStep(
                    number = 7,
                    title = "OmniGFX'e izin ver",
                    description = "OmniGFX'i aç, Shizuku izni iste ve onayla."
                )
            )),

            // ── Yöntem B: Bilgisayarsız ──
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "Yöntem B: Bilgisayarsız kurulum (Android 11 ve üzeri)",
                    isBold = true
                ),
                GuideParagraph(
                    "Android 11 ve üzeri sürümlerde Wireless ADB özelliği sayesinde " +
                    "bilgisayara ihtiyaç duymadan doğrudan telefondan kurulum yapabilirsin."
                )
            )),
            GuideBlock.Steps(listOf(
                GuideStep(
                    number = 1,
                    title = "Shizuku'yu indir",
                    description = "Google Play Store'dan yükle."
                ),
                GuideStep(
                    number = 2,
                    title = "Geliştirici Seçenekleri'ni aktif et",
                    description = "Ayarlar › Telefon Hakkında › Derleme Numarası'na 7 kez dokun."
                ),
                GuideStep(
                    number = 3,
                    title = "Wireless Hata Ayıklama'yı aç",
                    description = "Ayarlar › Geliştirici Seçenekleri › Kablosuz Hata Ayıklama › " +
                        "Aç. (Wi-Fi'ya bağlı olman gerekir.)"
                ),
                GuideStep(
                    number = 4,
                    title = "Shizuku'da \"Wireless Debugging\" ile başlat",
                    description = "Shizuku uygulamasını aç, \"Start via Wireless Debugging\" " +
                        "seçeneğine dokun ve eşleştirme kodunu gir."
                ),
                GuideStep(
                    number = 5,
                    title = "OmniGFX'e izin ver",
                    description = "OmniGFX'i aç, Shizuku izni iste, onayla."
                )
            )),
            GuideBlock.Callout(GuideCallout(
                type = CalloutType.WARNING,
                title = "Telefonu yeniden başlatırsan…",
                body = "Shizuku servisi telefon her yeniden başlatıldığında durur. " +
                    "Yukarıdaki komutu (Yöntem A) veya Wireless ADB eşleştirmesini (Yöntem B) " +
                    "tekrar yapman gerekir. Bu, Android'in güvenlik politikasıdır; " +
                    "OmniGFX veya Shizuku ile ilgili bir kısıtlama değildir."
            ))
        )
    ),

    // ─────────────────────────────────────────────────────────
    // 3. ROOT vs ADB SHELL
    // ─────────────────────────────────────────────────────────
    GuideSection(
        id = "root-vs-adb",
        icon = Icons.Outlined.Shield,
        title = "Root ve ADB: farklar",
        subtitle = "Yetki seviyeleri ve erişim sınırları",
        blocks = listOf(
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "Android'de farklı yetki seviyeleri vardır. Bunları bir binadaki " +
                    "erişim kartları gibi düşün:"
                )
            )),
            GuideBlock.Comparison(
                leftTitle = "Sınırlı erişim (ADB)",
                leftItems = listOf(
                    "OmniGFX'in varsayılan modu",
                    "Root gerektirmez",
                    "/sdcard/Android/data/ erişilebilir",
                    "PUBG, CoD Mobile gibi oyunlar çalışır",
                    "/data/data/ çoğu cihazda KISITLI",
                    "Garanti bozulmaz"
                ),
                leftAccent = GuideAccent.SUCCESS,
                rightTitle = "Tam erişim (root)",
                rightItems = listOf(
                    "Magisk/KernelSU gerekir",
                    "Tam sistem erişimi",
                    "/data/data/ TAM erişilebilir",
                    "Tüm oyunlar çalışır",
                    "Sistem dosyaları değiştirilebilir",
                    "Garanti geçersiz olabilir"
                ),
                rightAccent = GuideAccent.ERROR
            ),
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "Hangi oyunlar hangi yetkiyle çalışır?",
                    isBold = true
                )
            )),
            GuideBlock.BulletList(listOf(
                "PUBG Mobile: config dosyası /sdcard/ altında, sınırlı erişim yeterli.",
                "Call of Duty Mobile: config dosyası /sdcard/ altında, sınırlı erişim yeterli.",
                "Genshin Impact: config /data/data/ altında, tam erişim (root) gerekir.",
                "Fortnite: config /data/data/ altında, tam erişim (root) gerekir.",
                "Çoğu Unity oyunu: /data/data/ altında, tam erişim (root) gerekir."
            )),
            GuideBlock.Callout(GuideCallout(
                type = CalloutType.INFO,
                title = "OmniGFX otomatik algılar",
                body = "Bir dosyaya erişmeye çalıştığında OmniGFX, Shizuku'nun yetki " +
                    "seviyesine göre o dosyaya erişip erişemeyeceğini kontrol eder. " +
                    "Erişilemiyorsa sana açıkça \"Bu dosya için root gerekli\" der. " +
                    "Yanlış dosyayı yazma riski yoktur."
            ))
        )
    ),

    // ─────────────────────────────────────────────────────────
    // 4. OYUN EKLEME VE SİHİRBAZ
    // ─────────────────────────────────────────────────────────
    GuideSection(
        id = "wizard",
        icon = Icons.Outlined.Build,
        title = "Oyun ekleme ve sihirbaz",
        subtitle = "5 adımda ilk profilini oluştur",
        blocks = listOf(
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "OmniGFX'te bir oyun için ayar değiştirmek 5 adımlı bir sihirbazla yapılır. " +
                    "Bu sihirbaz seni dosya seçiminden profil kaydetmeye kadar götürür."
                )
            )),
            GuideBlock.Steps(listOf(
                GuideStep(
                    number = 1,
                    title = "Oyun seç",
                    description = "Ana ekrandaki + butonuna bas. Cihazında yüklü olan oyunlar " +
                        "listelenir. Değiştirmek istediğin oyunu seç."
                ),
                GuideStep(
                    number = 2,
                    title = "Hedef dosyayı belirle",
                    description = "Oyunun config (ayar) dosyasını seç. Bilinen oyunlar için " +
                        "hazır şablonlar sunulur. Bilmiyorsan Dosya Gezgini ile bulabilirsin."
                ),
                GuideStep(
                    number = 3,
                    title = "Dosyayı çek ve çözümle",
                    description = "OmniGFX dosyayı Shizuku aracılığıyla çeker, formatını " +
                        "algılar (INI, JSON, XML) ve düzenlenebilir hale getirir."
                ),
                GuideStep(
                    number = 4,
                    title = "Düzenleyicide değiştir",
                    description = "Satır satır config dosyasını gör. Değerleri değiştir, " +
                        "satır ekle/sil, sabitle."
                ),
                GuideStep(
                    number = 5,
                    title = "Değişkenleri sabitle ve kaydet",
                    description = "Sık değiştirdiğin ayarları kaydırıcı, liste veya anahtar " +
                        "olarak sabitle. Profil adı ver ve kaydet."
                )
            )),
            GuideBlock.Callout(GuideCallout(
                type = CalloutType.TIP,
                title = "Bilinen oyun şablonları",
                body = "PUBG Mobile, CoD Mobile, Genshin Impact, Honkai Star Rail gibi " +
                    "popüler oyunlar için OmniGFX hazır dosya yolları sunar. Oyunu seçtiğinde " +
                    "\"Bilinen Config Dosyaları\" bölümünde bu şablonları göreceksin. " +
                    "Dosya yolunu elle aramana gerek kalmaz."
            )),
            GuideBlock.Callout(GuideCallout(
                type = CalloutType.WARNING,
                title = "Oyunu ilk kez aç",
                body = "Eğer oyunu hiç açmadıysan config dosyası henüz oluşmamış olabilir. " +
                    "Dosya bulunamadı hatası alırsan: oyunu bir kez aç, birkaç saniye bekle, " +
                    "kapat ve OmniGFX'ten tekrar dene."
            ))
        )
    ),

    // ─────────────────────────────────────────────────────────
    // 5. CONFIG DOSYALARI
    // ─────────────────────────────────────────────────────────
    GuideSection(
        id = "config-files",
        icon = Icons.Outlined.Folder,
        title = "Config dosyaları hakkında",
        subtitle = "INI, JSON, XML — oyunların ayarlarını nasıl sakladığı",
        blocks = listOf(
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "Her oyun, ayarlarını bir config (yapılandırma) dosyasında saklar. " +
                    "Bu dosyalar düz metindir ve üç yaygın format kullanılır:"
                )
            )),

            // INI
            GuideBlock.Text(listOf(
                GuideParagraph("1. INI formatı", isBold = true),
                GuideParagraph(
                    "En yaygın oyun config formatıdır. Bölümler [köşeli parantez] içinde, " +
                    "ayarlar Anahtar=Değer şeklinde yazılır."
                )
            )),
            GuideBlock.CodeBlock(
                code = """[UserCustom DeviceProfile]
+CVars=r.PUBGDeviceFPSLow=60
+CVars=r.PUBGDeviceFPSHigh=60
+CVars=r.UserQualityLimit=0

[/Script/Engine.Engine]
bSmoothFrameRate=True
MinSmoothedFrameRate=30
MaxSmoothedFrameRate=60""",
                caption = "PUBG Mobile UserCustom.ini örneği"
            ),
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "OmniGFX INI dosyalarını satır satır parse eder. Yorum satırlarını (; veya #), " +
                    "boş satırları ve bölüm başlıklarını korur. Sadece değiştirdiğin değeri " +
                    "yazar, geri kalanı bozulmaz."
                )
            )),

            // JSON
            GuideBlock.Text(listOf(
                GuideParagraph("2. JSON formatı", isBold = true),
                GuideParagraph(
                    "Modern oyunlar ve Unity tabanlı oyunlar genellikle JSON kullanır. " +
                    "İçiçe nesneler ve diziler içerir."
                )
            )),
            GuideBlock.CodeBlock(
                code = """{
  "graphics": {
    "fps": 60,
    "resolution": "1920x1080",
    "shadows": "high"
  },
  "audio": {
    "music_volume": 0.8,
    "sfx_volume": 1.0
  }
}""",
                caption = "Örnek JSON config dosyası"
            ),
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "OmniGFX JSON dosyalarında JSONPath kullanır. Örneğin \"graphics.fps\" " +
                    "yoluyla fps değerine erişir. Eksik yolları otomatik oluşturur."
                )
            )),

            // XML
            GuideBlock.Text(listOf(
                GuideParagraph("3. XML formatı", isBold = true),
                GuideParagraph(
                    "Bazı eski oyunlar ve Unreal Engine oyunları XML kullanır. " +
                    "Etiketler ve attribute'lar içerir."
                )
            )),
            GuideBlock.CodeBlock(
                code = """<?xml version="1.0" encoding="UTF-8"?>
<Config>
  <Graphics Quality="3" Shadows="2" />
  <Audio MusicVolume="0.8" />
</Config>""",
                caption = "Örnek XML config dosyası"
            )
        )
    ),

    // ─────────────────────────────────────────────────────────
    // 6. REGEX NEDİR?
    // ─────────────────────────────────────────────────────────
    GuideSection(
        id = "regex",
        icon = Icons.Outlined.Code,
        title = "Regex nedir? (basit anlatım)",
        subtitle = "Metin içinde kalıp bulma ve değiştirme sanatı",
        blocks = listOf(
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "Regex (Regular Expression), metin içinde belirli kalıpları bulmaya " +
                    "yarayan bir dil gibidir. \"Bul ve Değiştir\" özelliğinin çok daha " +
                    "güçlü versiyonudur."
                ),
                GuideParagraph(
                    "Basit benzetme:", isBold = true
                ),
                GuideParagraph(
                    "Bir kitapta tüm \"elma\" kelimelerini bulup \"armut\" yapmak basit " +
                    "bul-değiştir'dir. Ama \"her satırın başındaki sayıyı bul, 2 ile çarp, " +
                    "yerine yaz\" demek regex gerektirir."
                )
            )),
            GuideBlock.Text(listOf(
                GuideParagraph("OmniGFX'te Regex ne işe yarar?", isBold = true),
                GuideParagraph(
                    "Bazı oyunların config dosyaları standart INI/JSON formatında değildir. " +
                    "Özel bir format kullanırlar. Bu durumda OmniGFX, regex kurallarıyla " +
                    "doğru satırı bulur ve değiştirir."
                )
            )),
            GuideBlock.CodeBlock(
                code = """Pattern:    r\.PUBGDeviceFPSLow=(\d+)
Replacement: r.PUBGDeviceFPSLow={{value}}""",
                caption = "Örnek: PUBG'de FPS düşük ayarını bul ve kullanıcının seçtiği değerle değiştir"
            ),
            GuideBlock.Text(listOf(
                GuideParagraph("Regex'i parçalayalım:", isBold = true)
            )),
            GuideBlock.BulletList(listOf(
                "r\\.PUBGDeviceFPSLow — Tam olarak bu metni ara (\\. nokta anlamına gelir)",
                "= — Eşittir işareti",
                "(\\d+) — Bir veya daha fazla rakam yakala (bu \"grup 1\" olur)",
                "{{value}} — OmniGFX buraya kullanıcının slider'dan seçtiği değeri koyar"
            )),
            GuideBlock.Callout(GuideCallout(
                type = CalloutType.TIP,
                title = "Regex bilmene gerek yok",
                body = "OmniGFX'in sihirbazı, bilinen oyunlar için regex kurallarını otomatik " +
                    "oluşturur. Sen sadece slider'ı kaydırır veya dropdown'dan seçim yaparsın. " +
                    "Regex sadece ileri düzey kullanıcıların özel oyunlar için yazdığı " +
                    "kurallardır."
            )),
            GuideBlock.Text(listOf(
                GuideParagraph("Yaygın regex kalıpları (ileri düzey)", isBold = true)
            )),
            GuideBlock.BulletList(listOf(
                "\\d+ — Bir veya daha fazla rakam",
                "[a-zA-Z]+ — Bir veya daha fazla harf",
                "^ — Satır başı",
                "$ — Satır sonu",
                ".* — Herhangi bir karakter, sıfır veya daha fazla kez",
                "(grup) — Yakalama grubu, sonradan {{value}} ile değiştirilebilir"
            ))
        )
    ),

    // ─────────────────────────────────────────────────────────
    // 7. PROFİL TİPLERİ
    // ─────────────────────────────────────────────────────────
    GuideSection(
        id = "profile-types",
        icon = Icons.Outlined.Settings,
        title = "Profil tipleri: dinamik ve ham dosya",
        subtitle = "Hangi profil tipi ne zaman kullanılır?",
        blocks = listOf(
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "OmniGFX iki farklı profil tipi destekler. Her birinin kullanım " +
                    "alanı farklıdır:"
                )
            )),
            GuideBlock.Comparison(
                leftTitle = "Dinamik profil",
                leftItems = listOf(
                    "Slider, dropdown, toggle içerir",
                    "Tek tek ayar değiştirilebilir",
                    "Küçük dosya boyutu",
                    "Her seferinde patch uygulanır",
                    "Önerilen: Günlük kullanım"
                ),
                leftAccent = GuideAccent.PRIMARY,
                rightTitle = "Ham dosya profili",
                rightItems = listOf(
                    "Dosyanın tam yedeğini saklar",
                    "Tek tıkla komple geri yazar",
                    "Büyük dosya boyutu",
                    "Tam geri yükleme garantisi",
                    "Önerilen: Yedekleme, deneme"
                ),
                rightAccent = GuideAccent.WARNING
            ),
            GuideBlock.Text(listOf(
                GuideParagraph("Ne zaman dinamik profil kullanmalısın?", isBold = true)
            )),
            GuideBlock.BulletList(listOf(
                "Sık sık FPS, grafik kalitesi gibi ayarları değiştiriyorsan",
                "Farklı oyun modları için farklı profiller istiyorsan (Rekabet, Yayın, Pil)",
                "Bir ayarı slider ile hızlıca ayarlamak istiyorsan"
            )),
            GuideBlock.Text(listOf(
                GuideParagraph("Ne zaman ham dosya profili kullanmalısın?", isBold = true)
            )),
            GuideBlock.BulletList(listOf(
                "Config dosyasını tamamen değiştirmek istiyorsan",
                "İnternetten indirdiğin bir config'i uygulamak istiyorsan",
                "Orijinal dosyanın tam yedeğini saklamak istiyorsan",
                "Dinamik profilin parse edemediği karmaşık bir formatla çalışıyorsan"
            )),
            GuideBlock.Callout(GuideCallout(
                type = CalloutType.TIP,
                title = "İlk kez mi kullanıyorsun?",
                body = "Dinamik Profil ile başla. Sihirbaz, dosyanı analiz eder ve " +
                    "değiştirilebilir ayarları otomatik bulur. Sadece istediğin ayarları " +
                    "pin'le ve kaydet."
            ))
        )
    ),

    // ─────────────────────────────────────────────────────────
    // 8. GÜVENLİK VE DOĞRULAMA
    // ─────────────────────────────────────────────────────────
    GuideSection(
        id = "security",
        icon = Icons.Outlined.Security,
        title = "Güvenlik ve doğrulama",
        subtitle = "OmniGFX dosyanı nasıl korur?",
        blocks = listOf(
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "Bir config dosyasını değiştirmek riskli görünebilir. \"Ya dosya " +
                    "bozulursa?\" sorusunun cevabı: OmniGFX 5 katmanlı koruma sistemi kullanır."
                )
            )),
            GuideBlock.Steps(listOf(
                GuideStep(
                    number = 1,
                    title = "Uzak yedek",
                    description = "Değişiklik yapmadan ÖNCE orijinal dosyanın bir yedeği " +
                        "aynı cihazda oluşturulur. Dosya adı: .config.omnigfx.<tarih>.bak"
                ),
                GuideStep(
                    number = 2,
                    title = "Yerel yedek",
                    description = "Çekilen dosyanın bir kopyası OmniGFX'in kendi " +
                        "cache dizinine kaydedilir."
                ),
                GuideStep(
                    number = 3,
                    title = "Atomik yazma",
                    description = "Yeni dosya önce geçici bir isimle yazılır, sonra " +
                        "orijinal dosya ile tek işlemde değiştirilir. Yazma sırasında " +
                        "telefon kapanırsa bile orijinal dosya bozulmaz."
                ),
                GuideStep(
                    number = 4,
                    title = "Dosya kimliği koruma",
                    description = "Dosyanın sahibi (UID/GID), izinleri (chmod) ve " +
                        "SELinux context'i (chcon) korunur. Oyun, dosyayı kendi " +
                        "yazmış gibi görür."
                ),
                GuideStep(
                    number = 5,
                    title = "Hash doğrulama",
                    description = "Yazma işleminden sonra dosyanın SHA-256 hash'i " +
                        "hesaplanır ve beklenen değerle karşılaştırılır. Eşleşmezse " +
                        "otomatik rollback yapılır."
                )
            )),
            GuideBlock.Callout(GuideCallout(
                type = CalloutType.INFO,
                title = "Otomatik geri alma",
                body = "Eğer doğrulama başarısız olursa (dosya bozuk yazılmışsa, " +
                    "hash eşleşmezse), OmniGFX otomatik olarak son yedekten geri " +
                    "yükleme yapar. Sen hiçbir şey yapmana gerek kalmaz."
            )),
            GuideBlock.Callout(GuideCallout(
                type = CalloutType.DANGER,
                title = "Anti-cheat uyarısı",
                body = "Bazı çok oyunculu oyunlar config dosyası değişikliklerini " +
                    "tespit edebilir. OmniGFX metadata'yı korusa da, oyunun kendi " +
                    "anti-cheat sistemi dosya içeriğini kontrol edebilir. Rekabetçi " +
                    "modlarda dikkatli kullan. OmniGFX, hile değil; sadece oyunun " +
                    "kendi ayarlarını değiştirir."
            ))
        )
    ),

    // ─────────────────────────────────────────────────────────
    // 9. SIK KARŞILAŞILAN SORUNLAR
    // ─────────────────────────────────────────────────────────
    GuideSection(
        id = "troubleshooting",
        icon = Icons.AutoMirrored.Outlined.HelpOutline,
        title = "Sık karşılaşılan sorunlar",
        subtitle = "Çözümler ve ipuçları",
        blocks = listOf(
            // Sorun 1
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "\"Shizuku çalışmıyor\" hatası",
                    isBold = true
                )
            )),
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "Sebep: Telefon yeniden başlatılmış ve Shizuku servisi durmuş."
                ),
                GuideParagraph(
                    "Çözüm: Bilgisayardan ADB komutunu tekrar çalıştır veya Wireless ADB " +
                    "ile yeniden başlat. (Bkz: Shizuku Kurulumu bölümü)"
                )
            )),

            // Sorun 2
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "\"Dosya bulunamadı\" hatası",
                    isBold = true
                )
            )),
            GuideBlock.BulletList(listOf(
                "Oyunu en az bir kez aç ve kapat. Config dosyası ilk açılışta oluşur.",
                "Dosya yolunu kontrol et. Sihirbazın sunduğu hazır şablonları kullan.",
                "Shizuku ADB Shell modundaysa /data/data/ altındaki dosyalara erişemeyebilir. " +
                    "Bu durumda root gereklidir."
            )),

            // Sorun 3
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "\"Dosya okunabiliyor ama yazılamıyor\" hatası",
                    isBold = true
                )
            )),
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "Sebep: Dosyanın sahibi (UID) oyunun kendisi ve ADB Shell'in yazma " +
                    "izni yok."
                ),
                GuideParagraph(
                    "Çözüm: Bu genellikle root gerektiren bir durumdur. Ancak bazı " +
                    "cihazlarda oyun kapalıyken yazma izni olabilir. Oyunu force-stop " +
                    "et ve tekrar dene."
                )
            )),

            // Sorun 4
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "\"Push doğrulanamadı\" hatası",
                    isBold = true
                )
            )),
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "Sebep: Dosya yazıldı ama hash doğrulaması başarısız oldu."
                ),
                GuideParagraph(
                    "Çözüm: OmniGFX otomatik olarak rollback yapar. Birkaç saniye bekle " +
                    "ve tekrar dene. Sorun devam ederse Shizuku bağlantısını kontrol et."
                )
            )),

            // Sorun 5
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "\"Shizuku izni reddedildi\" hatası",
                    isBold = true
                )
            )),
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "Çözüm: Shizuku uygulamasını aç, \"Authorized apps\" bölümünden " +
                    "OmniGFX'i bul ve izni manuel olarak ver. Veya OmniGFX'ten " +
                    "\"Tekrar izin iste\" butonuna bas."
                )
            )),

            // Sorun 6
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "\"Config parse edilemedi\" hatası",
                    isBold = true
                )
            )),
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "Sebep: Dosya formatı INI/JSON/XML değil, özel bir formatta."
                ),
                GuideParagraph(
                    "Çözüm: Ham Dosya Profili kullan. Dosyayı tamamen değiştir veya " +
                    "Regex kuralları yaz (ileri düzey)."
                )
            )),

            // Sorun 7
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "Xiaomi/Redmi cihazlarda Shizuku duruyor",
                    isBold = true
                )
            )),
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "MIUI, arka plan uygulamalarını agresif şekilde kapatır."
                )
            )),
            GuideBlock.BulletList(listOf(
                "Ayarlar › Uygulamalar › Shizuku › Pil tasarrufu › Kısıtlama yok",
                "Ayarlar › Uygulamalar › Shizuku › Otomatik başlatma › Açık",
                "Shizuku'yu kilitli uygulamalar listesine ekle"
            ))
        )
    ),

    // ─────────────────────────────────────────────────────────
    // 10. İLERİ DÜZEY İPUÇLARI
    // ─────────────────────────────────────────────────────────
    GuideSection(
        id = "advanced",
        icon = Icons.Outlined.Psychology,
        title = "İleri düzey ipuçları",
        subtitle = "Uzmanlar için ince detaylar",
        blocks = listOf(
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "Bu bölüm, Android sistem bilgisi olan ve OmniGFX'in tüm " +
                    "gücünü kullanmak isteyen kullanıcılar içindir."
                )
            )),

            // İpucu 1
            GuideBlock.Text(listOf(
                GuideParagraph("1. Özel regex kuralları yazma", isBold = true),
                GuideParagraph(
                    "Sihirbazın \"Enjeksiyon Kuralı\" bölümünde kendi regex pattern'larını " +
                    "yazabilirsin. Bu, standart parser'ların desteklemediği formatlar için " +
                    "gereklidir."
                )
            )),
            GuideBlock.CodeBlock(
                code = """Pattern:    MaxFPS=(\d+)
Replacement: MaxFPS={{value}}
→ "MaxFPS=30" satırını bulur, kullanıcının seçtiği değerle değiştirir.

Pattern:    "quality"\s*:\s*"(\w+)"
Replacement: "quality": "{{value}}"
→ JSON içindeki quality alanını bulur ve değiştirir.""",
                caption = "Regex kuralı örnekleri"
            ),

            // İpucu 2
            GuideBlock.Text(listOf(
                GuideParagraph("2. Dosya gezgini ile bilinmeyen dosyaları bulma", isBold = true),
                GuideParagraph(
                    "Sihirbazın dosya gezgini, Shizuku üzerinden cihazın tüm dosya " +
                    "sistemini gösterir. Bilinmeyen bir oyun için config dosyasını " +
                    "şu yollarda arayabilirsin:"
                )
            )),
            GuideBlock.BulletList(listOf(
                "/sdcard/Android/data/<paket_adı>/files/ — En yaygın konum",
                "/data/data/<paket_adı>/files/ — Root gerekli",
                "/data/data/<paket_adı>/shared_prefs/ — XML tercih dosyaları",
                "/sdcard/Android/data/<paket_adı>/Saved/Config/ — Unreal Engine oyunları"
            )),

            // İpucu 3
            GuideBlock.Text(listOf(
                GuideParagraph("3. Metadata'nın önemi", isBold = true),
                GuideParagraph(
                    "Android'de her dosyanın bir sahibi (UID:GID), izin modu (chmod) ve " +
                    "SELinux context'i vardır. Bunlar yanlışsa oyun dosyayı okuyamaz."
                )
            )),
            GuideBlock.CodeBlock(
                code = """# Bir dosyanın metadata'sını görüntüleme:
stat -c '%u %g %a %C' /sdcard/Android/data/com.tencent.ig/files/UserCustom.ini

# Örnek çıktı:
# 10142 10142 660 u:object_r:app_data_file:s0
#
# 10142:10142 → UID:GID (oyunun kendi kullanıcısı)
# 660 → İzin modu (sahip oku-yaz, grup oku-yaz, diğer: yok)
# u:object_r:app_data_file:s0 → SELinux context""",
                caption = "Metadata komutları"
            ),
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "OmniGFX, push işleminden sonra bu metadata'yı otomatik olarak " +
                    "geri yükler. Eğer manuel olarak dosya yazıyorsan chown, chmod " +
                    "ve chcon komutlarını kendin çalıştırman gerekir."
                )
            )),

            // İpucu 4
            GuideBlock.Text(listOf(
                GuideParagraph("4. Profilleri dışa ve içe aktarma", isBold = true),
                GuideParagraph(
                    "OmniGFX profillerini JSON olarak dışa aktarabilir ve başka " +
                    "cihazlarda içe aktarabilirsin. Marketplace bölümünde topluluk " +
                    "tarafından paylaşılan profilleri güvenlik taramasından geçirerek " +
                    "içe aktarabilirsin."
                )
            )),
            GuideBlock.Callout(GuideCallout(
                type = CalloutType.DANGER,
                title = "Güvenlik uyarısı: içe aktarılan profiller",
                body = "İnternetten indirilen profiller, /system/ veya /data/system/ " +
                    "gibi kritik dizinlere yazmaya çalışabilir. OmniGFX'in güvenlik " +
                    "tarayıcısı bunları engeller ama yine de dikkatli ol. Sadece " +
                    "güvenilir kaynaklardan profil içe aktar. Marketplace'teki " +
                    "\"Risk skoru\" değerini kontrol et."
            )),

            // İpucu 5
            GuideBlock.Text(listOf(
                GuideParagraph("5. Dry Run (kuru çalıştırma)", isBold = true),
                GuideParagraph(
                    "Bir profili gerçekten uygulamadan önce \"Dry Run\" modunda " +
                    "test edebilirsin. Dry Run, tüm adımları yapar ama son yazma " +
                    "işlemini gerçekleştirmez. Diff Preview ile orijinal ve " +
                    "değiştirilmiş dosya arasındaki farkları görebilirsin."
                )
            )),

            // İpucu 6
            GuideBlock.Text(listOf(
                GuideParagraph("6. Config dosyası formatını elle belirleme", isBold = true),
                GuideParagraph(
                    "OmniGFX dosya uzantısına ve içeriğe bakarak formatı otomatik " +
                    "algılar. Ama bazen yanlış algılayabilir. Editör ekranında " +
                    "format seçimini manuel olarak değiştirebilirsin: AUTO, INI, " +
                    "JSON veya XML."
                )
            )),

            // İpucu 7
            GuideBlock.Text(listOf(
                GuideParagraph("7. Yedek saklama (backup retention)", isBold = true),
                GuideParagraph(
                    "Varsayılan olarak OmniGFX her dosya için son 3 yedeği saklar. " +
                    "Eski yedekler otomatik temizlenir. Bu sayıyı Profil Seçenekleri'nden " +
                    "değiştirebilir veya tamamen kapatabilirsin."
                )
            )),

            // İpucu 8
            GuideBlock.Text(listOf(
                GuideParagraph("8. Oyunu zorla durdurma (force-stop)", isBold = true),
                GuideParagraph(
                    "Config değişikliğinin etkili olması için oyunun kapalı olması gerekir. " +
                    "OmniGFX, profil uygularken otomatik olarak oyunu force-stop edebilir. " +
                    "Bu özelliği Profil Seçenekleri'nden açıp kapatabilirsin."
                )
            )),
            GuideBlock.CodeBlock(
                code = """# Shizuku üzerinden oyunu force-stop etme:
am force-stop com.tencent.ig

# Oyunu başlatma:
monkey -p com.tencent.ig -c android.intent.category.LAUNCHER 1""",
                caption = "Oyun süreç yönetimi komutları"
            )
        )
    ),

    // ─────────────────────────────────────────────────────────
    // 11. SÖZLÜK
    // ─────────────────────────────────────────────────────────
    GuideSection(
        id = "glossary",
        icon = Icons.Outlined.Speed,
        title = "Terim sözlüğü",
        subtitle = "Karşına çıkabilecek teknik terimler",
        blocks = listOf(
            GuideBlock.BulletList(listOf(
                "ADB (Android Debug Bridge) — Bilgisayar ile telefon arasında köprü kuran araç",
                "Atomic Push — Dosya yazma işleminin tek seferde, kesintisiz yapılması",
                "Config — Oyunun ayarlarını saklayan yapılandırma dosyası",
                "Dry Run — İşlemi gerçekten yapmadan sadece simüle etme",
                "Force-Stop — Bir uygulamanın tüm süreçlerini zorla durdurma",
                "Hash (SHA-256) — Dosya içeriğinin benzersiz dijital parmak izi",
                "INI — Bölümler ve Anahtar=Değer çiftlerinden oluşan config formatı",
                "JSON — İçiçe nesneler ve dizilerden oluşan veri formatı",
                "JSONPath — JSON içinde belirli bir alana erişim yolu (örn: graphics.fps)",
                "Metadata — Dosyanın sahibi, izinleri ve SELinux context'i",
                "Patch — Config dosyasına yapılan tek bir değişiklik",
                "Pin — Bir ayarı slider/dropdown olarak profile ekleme",
                "PMP (Pull-Modify-Push) — OmniGFX'in 3 aşamalı dosya işlemi pipeline'ı",
                "Regex — Metin içinde kalıp bulma dili",
                "Remote Backup — Cihaz üzerinde oluşturulan orijinal dosya yedeği",
                "Rollback — Bozuk yazma sonrası yedekten geri yükleme",
                "Root — Android'in en yüksek yetki seviyesi (UID 0)",
                "SELinux — Android'in güvenlik modülü; dosya erişim politikalarını yönetir",
                "Shizuku — Uygulamalara ADB yetkisi veren açık kaynak araç",
                "UID/GID — Dosya sahibinin kullanıcı ve grup numarası",
                "Wireless ADB — Kablosuz hata ayıklama (Android 11+)",
                "XML — Etiket tabanlı yapılandırılmış veri formatı"
            ))
        )
    )
)
