package com.basitce.gfx.ui.guide

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_ui.components.OmniTopBar
import com.basitce.gfx.core.core_ui.theme.OmniError
import com.basitce.gfx.core.core_ui.theme.OmniInfo
import com.basitce.gfx.core.core_ui.theme.OmniOnSurface
import com.basitce.gfx.core.core_ui.theme.OmniOnSurfaceVariant
import com.basitce.gfx.core.core_ui.theme.OmniPrimary
import com.basitce.gfx.core.core_ui.theme.OmniSuccess
import com.basitce.gfx.core.core_ui.theme.OmniSurface
import com.basitce.gfx.core.core_ui.theme.OmniSurfaceElevated
import com.basitce.gfx.core.core_ui.theme.OmniWarning
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════════════════
//  REHBER VERİ MODELLERİ
// ═══════════════════════════════════════════════════════════════

private enum class CalloutType { INFO, TIP, WARNING, DANGER, CODE }

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
        val leftColor: Color,
        val rightTitle: String,
        val rightItems: List<String>,
        val rightColor: Color
    ) : GuideBlock()
}

private data class GuideSection(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val accentColor: Color,
    val blocks: List<GuideBlock>
)

// ═══════════════════════════════════════════════════════════════
//  ANA EKRAN
// ═══════════════════════════════════════════════════════════════

@Composable
fun GuideScreen(onBack: () -> Unit) {
    val sections = remember { buildGuideSections() }
    val expandedSections = remember {
        mutableStateOf(setOf(sections.firstOrNull()?.id ?: ""))
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            
            .navigationBarsPadding()
    ) {
        OmniTopBar(
            title = "Kullanım Rehberi",
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

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ─── HERO ──────────────────────────────────────
            item { HeroSection() }

            // ─── İÇİNDEKİLER ───────────────────────────────
            item {
                TableOfContents(
                    sections = sections,
                    onJumpTo = { index ->
                        scope.launch {
                            listState.animateScrollToItem(index + 2)
                        }
                    }
                )
            }

            // ─── BÖLÜMLER ─────────────────────────────────
            itemsIndexed(sections, key = { _, s -> s.id }) { _, section ->
                SectionCard(
                    section = section,
                    isExpanded = expandedSections.value.contains(section.id),
                    onToggle = {
                        val current = expandedSections.value.toMutableSet()
                        if (current.contains(section.id)) {
                            current.remove(section.id)
                        } else {
                            current.add(section.id)
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        OmniPrimary.copy(alpha = 0.25f),
                        OmniSurface
                    )
                )
            )
            .border(1.dp, OmniPrimary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(OmniPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        tint = OmniPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "OmniGFX Rehberi",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = OmniOnSurface
                    )
                    Text(
                        text = "Sıfırdan ustalaşmaya",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OmniOnSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Bu rehber, oyunu hiç bilmeyen bir kullanıcının bile adım adım ilerleyebilmesi için hazırlandı. Her bölüm kendi içinde bağımsızdır; istediğin yerden başlayabilirsin. İleri düzey konular da ayrı başlıklar altında detaylıca anlatıldı.",
                style = MaterialTheme.typography.bodyMedium,
                color = OmniOnSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  İÇİNDEKİLER
// ═══════════════════════════════════════════════════════════════

@Composable
private fun TableOfContents(
    sections: List<GuideSection>,
    onJumpTo: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(OmniSurface)
            .border(1.dp, OmniSurfaceElevated, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.HelpOutline,
                contentDescription = null,
                tint = OmniPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "İçindekiler",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OmniOnSurface
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        sections.forEachIndexed { index, section ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onJumpTo(index) }
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(section.accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = null,
                        tint = section.accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = OmniOnSurface
                    )
                    Text(
                        text = section.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = OmniOnSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = OmniOnSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  SECTION CARD (ACCORDION)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SectionCard(
    section: GuideSection,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(OmniSurface)
            .border(
                width = if (isExpanded) 1.5.dp else 1.dp,
                color = if (isExpanded) section.accentColor.copy(alpha = 0.5f)
                else OmniSurfaceElevated,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        // ─── HEADER (tıklanabilir) ─────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(section.accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = null,
                    tint = section.accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = OmniOnSurface
                )
                Text(
                    text = section.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OmniOnSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Daralt" else "Genişlet",
                tint = OmniOnSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
        }

        // ─── İÇERİK (animasyonlu) ─────────────────────
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  BLOK RENDERER'LARI
// ═══════════════════════════════════════════════════════════════

@Composable
private fun TextBlock(block: GuideBlock.Text) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        block.paragraphs.forEach { p ->
            if (p.isCode) {
                SelectionContainer {
                    Text(
                        text = p.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = OmniPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(OmniSurfaceElevated)
                            .padding(12.dp)
                    )
                }
            } else {
                Text(
                    text = p.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = OmniOnSurface,
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
    val icon: ImageVector
)

@Composable
private fun CalloutBlock(callout: GuideCallout) {
    val style = when (callout.type) {
        CalloutType.INFO -> CalloutStyle(Color(0xFF1E3A5F), OmniInfo.copy(alpha = 0.5f), OmniInfo, Icons.Default.Info)
        CalloutType.TIP -> CalloutStyle(Color(0xFF1A3D2E), OmniSuccess.copy(alpha = 0.5f), OmniSuccess, Icons.Default.Lightbulb)
        CalloutType.WARNING -> CalloutStyle(Color(0xFF3D2E1A), OmniWarning.copy(alpha = 0.5f), OmniWarning, Icons.Default.Warning)
        CalloutType.DANGER -> CalloutStyle(Color(0xFF3D1A1A), OmniError.copy(alpha = 0.5f), OmniError, Icons.Default.Dangerous)
        CalloutType.CODE -> CalloutStyle(Color(0xFF1A1A2E), OmniPrimary.copy(alpha = 0.5f), OmniPrimary, Icons.Default.Terminal)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(style.bgColor)
            .border(1.dp, style.borderColor, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Icon(
            imageVector = style.icon,
            contentDescription = null,
            tint = style.iconColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = callout.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = style.iconColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            SelectionContainer {
                Text(
                    text = callout.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OmniOnSurface
                )
            }
        }
    }
}

@Composable
private fun StepsBlock(steps: List<GuideStep>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        steps.forEach { step ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(OmniPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = step.number.toString(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = OmniOnSurface
                    )
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OmniOnSurfaceVariant
                    )
                    step.code?.let { code ->
                        Spacer(modifier = Modifier.height(6.dp))
                        SelectionContainer {
                            Text(
                                text = code,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = OmniSuccess,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0A1A14))
                                    .padding(10.dp)
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
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { item ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OmniPrimary,
                    modifier = Modifier.width(16.dp)
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyLarge,
                    color = OmniOnSurface
                )
            }
        }
    }
}

@Composable
private fun CodeBlock(block: GuideBlock.CodeBlock) {
    Column {
        SelectionContainer {
            Text(
                text = block.code,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = OmniSuccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0A1A14))
                    .border(1.dp, OmniSuccess.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            )
        }
        block.caption?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = OmniOnSurfaceVariant
            )
        }
    }
}

@Composable
private fun ComparisonBlock(block: GuideBlock.Comparison) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ComparisonColumn(
            title = block.leftTitle,
            items = block.leftItems,
            color = block.leftColor,
            modifier = Modifier.weight(1f)
        )
        ComparisonColumn(
            title = block.rightTitle,
            items = block.rightItems,
            color = block.rightColor,
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
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Spacer(modifier = Modifier.height(8.dp))
        items.forEach { item ->
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text(
                    text = "✓",
                    color = color,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(16.dp)
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodySmall,
                    color = OmniOnSurface
                )
            }
        }
    }
}

@Composable
private fun FooterNote() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(OmniSurfaceElevated.copy(alpha = 0.5f))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = OmniWarning,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Bir sorun mu yaşıyorsun? OmniGFX açık kaynaklıdır. GitHub üzerinden sorun kaydı açabilir veya topluluğa katılabilirsin.",
                style = MaterialTheme.typography.bodySmall,
                color = OmniOnSurfaceVariant
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
        icon = Icons.Default.Games,
        title = "OmniGFX Nedir?",
        subtitle = "Uygulamanın ne yaptığını 2 dakikada anla",
        accentColor = OmniPrimary,
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
                title = "OmniGFX ne DEĞİLDİR?",
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
        icon = Icons.Default.Key,
        title = "Shizuku Kurulumu",
        subtitle = "OmniGFX'in çalışması için gereken temel araç",
        accentColor = OmniSuccess,
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
                    "Hangi Android sürümünü kullandığını bilmiyorsan Ayarlar → Telefon Hakkında " +
                    "bölümünden kontrol edebilirsin."
            )),

            // ── Yöntem A: Bilgisayar ile ──
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "━━━  YÖNTEM A: Bilgisayar ile Kurulum  ━━━",
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
                    description = "Ayarlar → Telefon Hakkında → Derleme Numarası'na 7 kez " +
                        "dokunarak Geliştirici Seçenekleri'ni aktif et. Sonra Ayarlar → " +
                        "Geliştirici Seçenekleri → USB Hata Ayıklama'yı aç."
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
                    title = "Bilgisayarında terminal/aç ve şu komutu gir",
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
                    description = "OmniGFX'i aç → Shizuku izni iste → Onayla."
                )
            )),

            // ── Yöntem B: Bilgisayarsız ──
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "━━━  YÖNTEM B: Bilgisayarsız Kurulum (Android 11+)  ━━━",
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
                    description = "Ayarlar → Telefon Hakkında → Derleme Numarası'na 7 kez dokun."
                ),
                GuideStep(
                    number = 3,
                    title = "Wireless Hata Ayıklama'yı aç",
                    description = "Ayarlar → Geliştirici Seçenekleri → Kablosuz Hata Ayıklama → " +
                        "Aç. (Wi-Fi'ya bağlı olman gerekir.)"
                ),
                GuideStep(
                    number = 4,
                    title = "Shizuku'da \"Wireless Debugging\" ile başlat",
                    description = "Shizuku uygulamasını aç → \"Start via Wireless Debugging\" " +
                        "seçeneğine dokun → Eşleştirme kodunu gir."
                ),
                GuideStep(
                    number = 5,
                    title = "OmniGFX'e izin ver",
                    description = "OmniGFX'i aç, Shizuku izni iste, onayla."
                )
            )),
            GuideBlock.Callout(GuideCallout(
                type = CalloutType.WARNING,
                title = "Telefonu yeniden başlatırsan...",
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
        icon = Icons.Default.Shield,
        title = "Root vs ADB Shell: Farklar",
        subtitle = "Yetki seviyeleri ve erişim sınırları",
        accentColor = OmniWarning,
        blocks = listOf(
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "Android'de farklı yetki seviyeleri vardır. Bunları bir binadaki " +
                    "erişim kartları gibi düşün:"
                )
            )),
            GuideBlock.Comparison(
                leftTitle = "ADB Shell (UID 2000)",
                leftItems = listOf(
                    "OmniGFX'in varsayılan modu",
                    "Root gerektirmez",
                    "/sdcard/Android/data/ erişilebilir",
                    "PUBG, CoD Mobile gibi oyunlar çalışır",
                    "/data/data/ çoğu cihazda KISITLI",
                    "Garanti bozulmaz"
                ),
                leftColor = OmniSuccess,
                rightTitle = "Root (UID 0)",
                rightItems = listOf(
                    "Magisk/KernelSU gerekir",
                    "Tam sistem erişimi",
                    "/data/data/ TAM erişilebilir",
                    "Tüm oyunlar çalışır",
                    "Sistem dosyaları değiştirilebilir",
                    "Garanti geçersiz olabilir"
                ),
                rightColor = OmniError
            ),
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "Hangi oyunlar hangi yetkiyle çalışır?",
                    isBold = true
                )
            )),
            GuideBlock.BulletList(listOf(
                "PUBG Mobile → Config dosyası /sdcard/ altında → ADB Shell yeterli ✓",
                "Call of Duty Mobile → Config dosyası /sdcard/ altında → ADB Shell yeterli ✓",
                "Genshin Impact → Config /data/data/ altında → Root gerekli ✗ (ADB ile sınırlı)",
                "Fortnite → Config /data/data/ altında → Root gerekli ✗",
                "Çoğu Unity oyunu → /data/data/ altında → Root gerekli ✗"
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
        icon = Icons.Default.Build,
        title = "Oyun Ekleme ve Sihirbaz",
        subtitle = "5 adımda ilk profilini oluştur",
        accentColor = OmniPrimary,
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
                    title = "Oyun Seç",
                    description = "Ana ekrandaki + butonuna bas. Cihazında yüklü olan oyunlar " +
                        "listelenir. Değiştirmek istediğin oyunu seç."
                ),
                GuideStep(
                    number = 2,
                    title = "Hedef Dosyayı Belirle",
                    description = "Oyunun config (ayar) dosyasını seç. Bilinen oyunlar için " +
                        "hazır şablonlar sunulur. Bilmiyorsan Dosya Gezgini ile bulabilirsin."
                ),
                GuideStep(
                    number = 3,
                    title = "Dosyayı Çek ve Analiz Et",
                    description = "OmniGFX dosyayı Shizuku aracılığıyla çeker, formatını " +
                        "algılar (INI, JSON, XML) ve düzenlenebilir hale getirir."
                ),
                GuideStep(
                    number = 4,
                    title = "Akıllı Editör ile Düzenle",
                    description = "Satır satır config dosyasını gör. Değerleri değiştir, " +
                        "satır ekle/sil, pin'le."
                ),
                GuideStep(
                    number = 5,
                    title = "Değişkenleri Pin'le ve Kaydet",
                    description = "Sık değiştirdiğin ayarları slider, dropdown veya toggle " +
                        "olarak pin'le. Profil adı ver ve kaydet."
                )
            )),
            GuideBlock.Callout(GuideCallout(
                type = CalloutType.TIP,
                title = "Bilinen Oyun Şablonları",
                body = "PUBG Mobile, CoD Mobile, Genshin Impact, Honkai Star Rail gibi " +
                    "popüler oyunlar için OmniGFX hazır dosya yolları sunar. Oyunu seçtiğinde " +
                    "\"Bilinen Config Dosyaları\" bölümünde bu şablonları göreceksin. " +
                    "Dosya yolunu elle aramana gerek kalmaz."
            )),
            GuideBlock.Callout(GuideCallout(
                type = CalloutType.WARNING,
                title = "Oyunu ilk kez aç",
                body = "Eğer oyunu hiç açmadıysan config dosyası henüz oluşmamış olabilir. " +
                    "Dosya bulunamadı hatası alırsan: oyunu bir kez aç → birkaç saniye bekle " +
                    "→ kapat → OmniGFX'ten tekrar dene."
            ))
        )
    ),

    // ─────────────────────────────────────────────────────────
    // 5. CONFIG DOSYALARI
    // ─────────────────────────────────────────────────────────
    GuideSection(
        id = "config-files",
        icon = Icons.Default.Folder,
        title = "Config Dosyaları Hakkında",
        subtitle = "INI, JSON, XML — oyunların ayarlarını nasıl sakladığı",
        accentColor = OmniInfo,
        blocks = listOf(
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "Her oyun, ayarlarını bir config (yapılandırma) dosyasında saklar. " +
                    "Bu dosyalar düz metindir ve üç yaygın format kullanılır:"
                )
            )),

            // INI
            GuideBlock.Text(listOf(
                GuideParagraph("1. INI Formatı", isBold = true),
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
                GuideParagraph("2. JSON Formatı", isBold = true),
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
                GuideParagraph("3. XML Formatı", isBold = true),
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
        icon = Icons.Default.Code,
        title = "Regex Nedir? (Basit Anlatım)",
        subtitle = "Metin içinde kalıp bulma ve değiştirme sanatı",
        accentColor = Color(0xFFE879F9),
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
                "r\\.PUBGDeviceFPSLow → Tam olarak bu metni ara (\\. nokta anlamına gelir)",
                "= → Eşittir işareti",
                "(\\d+) → Bir veya daha fazla rakam yakala (bu \"grup 1\" olur)",
                "{{value}} → OmniGFX buraya kullanıcının slider'dan seçtiği değeri koyar"
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
                GuideParagraph("Yaygın Regex Kalıpları (İleri Düzey)", isBold = true)
            )),
            GuideBlock.BulletList(listOf(
                "\\d+ → Bir veya daha fazla rakam",
                "[a-zA-Z]+ → Bir veya daha fazla harf",
                "^ → Satır başı",
                "$ → Satır sonu",
                ".* → Herhangi bir karakter, sıfır veya daha fazla kez",
                "(grup) → Yakalama grubu — sonradan {{value}} ile değiştirilebilir"
            ))
        )
    ),

    // ─────────────────────────────────────────────────────────
    // 7. PROFİL TİPLERİ
    // ─────────────────────────────────────────────────────────
    GuideSection(
        id = "profile-types",
        icon = Icons.Default.Settings,
        title = "Profil Tipleri: Dinamik vs Ham Dosya",
        subtitle = "Hangi profil tipi ne zaman kullanılır?",
        accentColor = OmniPrimary,
        blocks = listOf(
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "OmniGFX iki farklı profil tipi destekler. Her birinin kullanım " +
                    "alanı farklıdır:"
                )
            )),
            GuideBlock.Comparison(
                leftTitle = "Dinamik Profil",
                leftItems = listOf(
                    "Slider, dropdown, toggle içerir",
                    "Tek tek ayar değiştirilebilir",
                    "Küçük dosya boyutu",
                    "Her seferinde patch uygulanır",
                    "Önerilen: Günlük kullanım"
                ),
                leftColor = OmniPrimary,
                rightTitle = "Ham Dosya Profili",
                rightItems = listOf(
                    "Dosyanın tam yedeğini saklar",
                    "Tek tıkla komple geri yazar",
                    "Büyük dosya boyutu",
                    "Tam geri yükleme garantisi",
                    "Önerilen: Yedekleme, deneme"
                ),
                rightColor = OmniWarning
            ),
            GuideBlock.Text(listOf(
                GuideParagraph("Ne zaman Dinamik Profil kullanmalısın?", isBold = true)
            )),
            GuideBlock.BulletList(listOf(
                "Sık sık FPS, grafik kalitesi gibi ayarları değiştiriyorsan",
                "Farklı oyun modları için farklı profiller istiyorsan (Rekabet, Yayın, Pil)",
                "Bir ayarı slider ile hızlıca ayarlamak istiyorsan"
            )),
            GuideBlock.Text(listOf(
                GuideParagraph("Ne zaman Ham Dosya Profili kullanmalısın?", isBold = true)
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
        icon = Icons.Default.Security,
        title = "Güvenlik ve Doğrulama",
        subtitle = "OmniGFX dosyanı nasıl korur?",
        accentColor = OmniSuccess,
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
                    title = "Remote Backup (Uzak Yedek)",
                    description = "Değişiklik yapmadan ÖNCE orijinal dosyanın bir yedeği " +
                        "aynı cihazda oluşturulur. Dosya adı: .config.omnigfx.<tarih>.bak"
                ),
                GuideStep(
                    number = 2,
                    title = "Local Backup (Yerel Yedek)",
                    description = "Çekilen dosyanın bir kopyası OmniGFX'in kendi " +
                        "cache dizinine kaydedilir."
                ),
                GuideStep(
                    number = 3,
                    title = "Atomic Push (Atomik Yazma)",
                    description = "Yeni dosya önce geçici bir isimle yazılır, sonra " +
                        "orijinal dosya ile tek işlemde değiştirilir. Yazma sırasında " +
                        "telefon kapanırsa bile orijinal dosya bozulmaz."
                ),
                GuideStep(
                    number = 4,
                    title = "Metadata Koruma",
                    description = "Dosyanın sahibi (UID/GID), izinleri (chmod) ve " +
                        "SELinux context'i (chcon) korunur. Oyun, dosyayı kendi " +
                        "yazmış gibi görür."
                ),
                GuideStep(
                    number = 5,
                    title = "Hash Doğrulama",
                    description = "Yazma işleminden sonra dosyanın SHA-256 hash'i " +
                        "hesaplanır ve beklenen değerle karşılaştırılır. Eşleşmezse " +
                        "otomatik rollback yapılır."
                )
            )),
            GuideBlock.Callout(GuideCallout(
                type = CalloutType.INFO,
                title = "Otomatik Rollback",
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
        icon = Icons.Default.HelpOutline,
        title = "Sık Karşılaşılan Sorunlar",
        subtitle = "Çözümler ve ipuçları",
        accentColor = OmniWarning,
        blocks = listOf(
            // Sorun 1
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "❌ \"Shizuku çalışmıyor\" hatası",
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
                    "❌ \"Dosya bulunamadı\" hatası",
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
                    "❌ \"Dosya okunabiliyor ama yazılamıyor\" hatası",
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
                    "❌ \"Push doğrulanamadı\" hatası",
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
                    "❌ \"Shizuku izni reddedildi\" hatası",
                    isBold = true
                )
            )),
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "Çözüm: Shizuku uygulamasını aç → \"Authorized apps\" bölümünden " +
                    "OmniGFX'i bul → İzni manuel olarak ver. Veya OmniGFX'ten " +
                    "\"Tekrar İzin İste\" butonuna bas."
                )
            )),

            // Sorun 6
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "❌ \"Config parse edilemedi\" hatası",
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
                    "❌ Xiaomi/Redmi cihazlarda Shizuku duruyor",
                    isBold = true
                )
            )),
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "MIUI, arka plan uygulamalarını agresif şekilde kapatır."
                )
            )),
            GuideBlock.BulletList(listOf(
                "Ayarlar → Uygulamalar → Shizuku → Pil tasarrufu → Kısıtlama yok",
                "Ayarlar → Uygulamalar → Shizuku → Otomatik başlatma → Açık",
                "Shizuku'yu kilitli uygulamalar listesine ekle"
            ))
        )
    ),

    // ─────────────────────────────────────────────────────────
    // 10. İLERİ DÜZEY İPUÇLARI
    // ─────────────────────────────────────────────────────────
    GuideSection(
        id = "advanced",
        icon = Icons.Default.Psychology,
        title = "İleri Düzey İpuçları",
        subtitle = "Uzmanlar için ince detaylar",
        accentColor = Color(0xFFE879F9),
        blocks = listOf(
            GuideBlock.Text(listOf(
                GuideParagraph(
                    "Bu bölüm, Android sistem bilgisi olan ve OmniGFX'in tüm " +
                    "gücünü kullanmak isteyen kullanıcılar içindir."
                )
            )),

            // İpucu 1
            GuideBlock.Text(listOf(
                GuideParagraph("1. Özel Regex Kuralları Yazma", isBold = true),
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
                GuideParagraph("2. Dosya Gezgini ile Bilinmeyen Dosyaları Bulma", isBold = true),
                GuideParagraph(
                    "Sihirbazın dosya gezgini, Shizuku üzerinden cihazın tüm dosya " +
                    "sistemini gösterir. Bilinmeyen bir oyun için config dosyasını " +
                    "şu yollarda arayabilirsin:"
                )
            )),
            GuideBlock.BulletList(listOf(
                "/sdcard/Android/data/<paket_adı>/files/ → En yaygın konum",
                "/data/data/<paket_adı>/files/ → Root gerekli",
                "/data/data/<paket_adı>/shared_prefs/ → XML tercih dosyaları",
                "/sdcard/Android/data/<paket_adı>/Saved/Config/ → Unreal Engine oyunları"
            )),

            // İpucu 3
            GuideBlock.Text(listOf(
                GuideParagraph("3. Metadata'nın Önemi", isBold = true),
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
                GuideParagraph("4. Profil Import/Export", isBold = true),
                GuideParagraph(
                    "OmniGFX profillerini JSON olarak dışa aktarabilir ve başka " +
                    "cihazlarda içe aktarabilirsin. Marketplace bölümünde topluluk " +
                    "tarafından paylaşılan profilleri güvenlik taramasından geçirerek " +
                    "import edebilirsin."
                )
            )),
            GuideBlock.Callout(GuideCallout(
                type = CalloutType.DANGER,
                title = "Güvenlik uyarısı: Import profilleri",
                body = "İnternetten indirilen profiller, /system/ veya /data/system/ " +
                    "gibi kritik dizinlere yazmaya çalışabilir. OmniGFX'in güvenlik " +
                    "tarayıcısı bunları engeller ama yine de dikkatli ol. Sadece " +
                    "güvenilir kaynaklardan profil import et. Marketplace'teki " +
                    "\"Risk Skoru\" değerini kontrol et."
            )),

            // İpucu 5
            GuideBlock.Text(listOf(
                GuideParagraph("5. Dry Run (Kuru Çalıştırma)", isBold = true),
                GuideParagraph(
                    "Bir profili gerçekten uygulamadan önce \"Dry Run\" modunda " +
                    "test edebilirsin. Dry Run, tüm adımları yapar ama son yazma " +
                    "işlemini gerçekleştirmez. Diff Preview ile orijinal ve " +
                    "değiştirilmiş dosya arasındaki farkları görebilirsin."
                )
            )),

            // İpucu 6
            GuideBlock.Text(listOf(
                GuideParagraph("6. Config Dosyası Formatını Elle Belirleme", isBold = true),
                GuideParagraph(
                    "OmniGFX dosya uzantısına ve içeriğe bakarak formatı otomatik " +
                    "algılar. Ama bazen yanlış algılayabilir. Editör ekranında " +
                    "format seçimini manuel olarak değiştirebilirsin: AUTO, INI, " +
                    "JSON veya XML."
                )
            )),

            // İpucu 7
            GuideBlock.Text(listOf(
                GuideParagraph("7. Backup Retention (Yedek Saklama)", isBold = true),
                GuideParagraph(
                    "Varsayılan olarak OmniGFX her dosya için son 3 yedeği saklar. " +
                    "Eski yedekler otomatik temizlenir. Bu sayıyı Profil Seçenekleri'nden " +
                    "değiştirebilir veya tamamen kapatabilirsin."
                )
            )),

            // İpucu 8
            GuideBlock.Text(listOf(
                GuideParagraph("8. Oyunu Force-Stop Etme", isBold = true),
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
        icon = Icons.Default.Speed,
        title = "Terim Sözlüğü",
        subtitle = "Karşına çıkabilecek teknik terimler",
        accentColor = OmniInfo,
        blocks = listOf(
            GuideBlock.BulletList(listOf(
                "ADB (Android Debug Bridge) → Bilgisayar ile telefon arasında köprü kuran araç",
                "Atomic Push → Dosya yazma işleminin tek seferde, kesintisiz yapılması",
                "Config → Oyunun ayarlarını saklayan yapılandırma dosyası",
                "Dry Run → İşlemi gerçekten yapmadan sadece simüle etme",
                "Force-Stop → Bir uygulamanın tüm süreçlerini zorla durdurma",
                "Hash (SHA-256) → Dosya içeriğinin benzersiz dijital parmak izi",
                "INI → Bölümler ve Anahtar=Değer çiftlerinden oluşan config formatı",
                "JSON → İçiçe nesneler ve dizilerden oluşan veri formatı",
                "JSONPath → JSON içinde belirli bir alana erişim yolu (örn: graphics.fps)",
                "Metadata → Dosyanın sahibi, izinleri ve SELinux context'i",
                "Patch → Config dosyasına yapılan tek bir değişiklik",
                "Pin → Bir ayarı slider/dropdown olarak profil'e ekleme",
                "PMP (Pull-Modify-Push) → OmniGFX'in 3 aşamalı dosya işlemi pipeline'ı",
                "Regex → Metin içinde kalıp bulma dili",
                "Remote Backup → Cihaz üzerinde oluşturulan orijinal dosya yedeği",
                "Rollback → Bozuk yazma sonrası yedekten geri yükleme",
                "Root → Android'in en yüksek yetki seviyesi (UID 0)",
                "SELinux → Android'in güvenlik modülü; dosya erişim politikalarını yönetir",
                "Shizuku → Uygulamalara ADB yetkisi veren açık kaynak araç",
                "UID/GID → Dosya sahibinin kullanıcı ve grup numarası",
                "Wireless ADB → Kablosuz hata ayıklama (Android 11+)",
                "XML → Etiket tabanlı yapılandırılmış veri formatı"
            ))
        )
    )
)
