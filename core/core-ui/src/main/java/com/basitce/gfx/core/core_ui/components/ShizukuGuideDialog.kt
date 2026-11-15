package com.basitce.gfx.core.core_ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_ui.theme.OmniRadius
import com.basitce.gfx.core.core_ui.theme.OmniTheme

/**
 * Shizuku kurulumunu 3 numaralı adımda gösteren alt sayfa.
 * Dar diyalog yerine tam genişlik sheet: arka plan kartları kenarlardan taşmaz,
 * scrim %65 ile net ayrışır. Adım rozetleri gerçek sıra taşır (1-2-3).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShizukuGuideDialog(
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val spacing = OmniTheme.spacing
    val colors = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = OmniRadius.lg, topEnd = OmniRadius.lg),
        containerColor = colors.surfaceContainerHigh,
        contentColor = colors.onSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.screen)
                .padding(bottom = spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(spacing.lg)
        ) {
            Text(
                text = "Shizuku kurulumu",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface
            )
            Text(
                text = "OmniGFX dosya erişimi için Shizuku kullanır. Sınırlı erişimde yalnızca oyunun erişilebilir config dosyalarını açabilirsin.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )

            GuideStep(number = 1, title = "Shizuku uygulamasını indir") {
                Text(
                    text = "Aşağıdaki düğme seni indirme sayfasına götürür.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
            GuideStep(number = 2, title = "Shizuku'yu başlat") {
                Text(
                    text = "Shizuku içindeki kablosuz hata ayıklama veya bilgisayarla başlatma yönergelerini izle. Komut sürüme göre değişebilir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(spacing.sm))
                OutlinedButton(
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/guide/setup/"))
                        )
                    },
                    shape = RoundedCornerShape(OmniRadius.sm),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = spacing.lg,
                        vertical = spacing.sm
                    ),
                    modifier = Modifier.heightIn(min = 36.dp)
                ) {
                    Text(
                        text = "Shizuku başlatma rehberi",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            GuideStep(number = 3, title = "OmniGFX'e izin ver") {
                Text(
                    text = "Shizuku çalışırken OmniGFX'e dön ve izin isteğine onay ver.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OmniPrimaryButton(
                    text = "Shizuku'yu indir",
                    onClick = { openShizukuDownload(context) },
                    modifier = Modifier.weight(1f)
                )
                OmniTextButton(text = "Kapat", onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun GuideStep(
    number: Int,
    title: String,
    content: @Composable () -> Unit
) {
    val spacing = OmniTheme.spacing
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(colors.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onPrimaryContainer
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface
            )
            Spacer(modifier = Modifier.height(spacing.xxs))
            content()
        }
    }
}

private fun openShizukuDownload(context: Context) {
    val uri = Uri.parse("https://github.com/RikkaApps/Shizuku/releases")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        val playUri = Uri.parse("market://details?id=moe.shizuku.privileged.api")
        val playIntent = Intent(Intent.ACTION_VIEW, playUri)
        try {
            context.startActivity(playIntent)
        } catch (_: Exception) {
            // Tarayıcı/Play Store yoksa sessizce kapat
        }
    }
}
