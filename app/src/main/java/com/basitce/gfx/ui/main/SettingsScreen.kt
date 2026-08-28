package com.basitce.gfx.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_ui.components.OmniCard
import com.basitce.gfx.core.core_ui.components.OmniPrimaryButton
import com.basitce.gfx.core.core_ui.components.OmniSecondaryButton
import com.basitce.gfx.core.core_ui.components.OmniTopBar

@Composable
fun SettingsScreen(
    onOpenShizukuSetup: () -> Unit,
    onOpenFileWorkflow: () -> Unit,
    onOpenGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        OmniTopBar(title = "Ayarlar")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OmniCard {
                Text(
                    text = "OmniGFX",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Sürüm 2.0.0-PRO",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OmniCard {
                Text(
                    text = "Shizuku",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Gelişmiş dosya erişimi için Shizuku servisi gerekli. Bağlantı sorunu yaşıyorsanız kurulum ekranını açın.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OmniPrimaryButton(
                    text = "Shizuku Kurulumu",
                    onClick = onOpenShizukuSetup,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            OmniCard {
                Text(
                    text = "Dosya Atölyesi",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Herhangi bir oyun dosyasını çek, çözümle, düzenle ve geri yaz. Sadece dosya yolunu bilmen yeterli.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OmniPrimaryButton(
                    text = "Dosya Atölyesini Aç",
                    onClick = onOpenFileWorkflow,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            OmniCard {
                Text(
                    text = "Rehber",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Uygulamayı nasıl kullanacağını öğren. Adım adım kurulum ve kullanım kılavuzu.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OmniSecondaryButton(
                    text = "Rehberi Aç",
                    onClick = onOpenGuide,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}
