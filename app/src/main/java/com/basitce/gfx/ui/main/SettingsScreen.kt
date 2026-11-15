package com.basitce.gfx.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.basitce.gfx.BuildConfig
import com.basitce.gfx.core.core_ui.components.OmniListRow
import com.basitce.gfx.core.core_ui.components.OmniSectionHeader
import com.basitce.gfx.core.core_ui.components.OmniTabHeader
import com.basitce.gfx.core.core_ui.theme.OmniRadius
import com.basitce.gfx.core.core_ui.theme.OmniTheme

/**
 * Ayarlar: dört özdeş kart yerine gruplanmış liste. Her satır tek eylem,
 * ikon + başlık + açıklama + ok. Hiyerarşi bölüm başlıklarıyla kurulur.
 */
@Composable
fun SettingsScreen(
    onOpenShizukuSetup: () -> Unit,
    onOpenFileWorkflow: () -> Unit,
    onOpenGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = OmniTheme.spacing
    val colors = MaterialTheme.colorScheme
    // Büyük fontta gruplar arası boşluk sıkılaşır; içerik zaten kaydırılabilir
    // ve alt barın üstünde ≥24dp pay bırakır (tur 2 P0-2).
    val largeFont = LocalDensity.current.fontScale > 1.15f

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        OmniTabHeader(
            title = "Ayarlar",
            subtitle = "Bağlantı, araçlar ve rehber"
        )
        Spacer(modifier = Modifier.height(if (largeFont) spacing.md else spacing.xl))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.screen)
                .padding(bottom = spacing.section),
            verticalArrangement = Arrangement.spacedBy(
                if (largeFont) spacing.lg else spacing.betweenSections
            )
        ) {
            SettingsGroup(title = "Bağlantı") {
                OmniListRow(
                    title = "Shizuku",
                    subtitle = "Dosya erişimi için gerekli. Bağlantı sorunu yaşıyorsan kurulumu aç.",
                    icon = Icons.Outlined.Bolt,
                    onClick = onOpenShizukuSetup,
                    trailing = { RowChevron() }
                )
            }

            SettingsGroup(title = "Araçlar") {
                OmniListRow(
                    title = "Dosya atölyesi",
                    subtitle = "Bir oyun dosyasını çek, çözümle, düzenle ve geri yaz.",
                    icon = Icons.Outlined.FolderOpen,
                    onClick = onOpenFileWorkflow,
                    trailing = { RowChevron() }
                )
                OmniListRow(
                    title = "Rehber",
                    subtitle = "Kurulum ve kullanım, adım adım.",
                    icon = Icons.AutoMirrored.Outlined.MenuBook,
                    onClick = onOpenGuide,
                    trailing = { RowChevron() }
                )
            }

            SettingsGroup(title = "Hakkında") {
                OmniListRow(
                    title = "OmniGFX",
                    subtitle = "Sürüm ${BuildConfig.VERSION_NAME}",
                    icon = Icons.Outlined.Info
                )
            }
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit
) {
    val spacing = OmniTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        OmniSectionHeader(title = title)
        Surface(
            shape = RoundedCornerShape(OmniRadius.md),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun RowChevron() {
    Icon(
        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
