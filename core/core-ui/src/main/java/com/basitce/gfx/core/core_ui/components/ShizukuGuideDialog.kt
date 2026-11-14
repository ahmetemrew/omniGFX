package com.basitce.gfx.core.core_ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService

/**
 * Shizuku kurulumunu 3 basit adımda gösteren rehber dialogu.
 */
@Composable
fun ShizukuGuideDialog(
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(com.basitce.gfx.core.core_ui.theme.OmniRadius.lg),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = "Shizuku kurulumu",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            val spacing = com.basitce.gfx.core.core_ui.theme.OmniTheme.spacing
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "OmniGFX dosya erişimi için Shizuku kullanır. Sınırlı erişimde yalnızca oyunun erişilebilir config dosyalarını açabilirsin.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(spacing.lg))
                Text(
                    text = "Adım 1: Shizuku uygulamasını indir.",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Adım 2: Shizuku içindeki kablosuz hata ayıklama veya bilgisayarla başlatma yönergelerini izle. Komut sürüme göre değişebilir.",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                OmniTextButton(
                    text = "Shizuku başlatma rehberi",
                    onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/guide/setup/"))) }
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                Text(
                    text = "Adım 3: OmniGFX'e izin ver.",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        },
        confirmButton = {
            OmniTextButton(
                text = "Shizuku'yu indir",
                onClick = { openShizukuDownload(context) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService<ClipboardManager>()
    clipboard?.setPrimaryClip(ClipData.newPlainText("Shizuku ADB komutu", text))
    Toast.makeText(context, "Komut kopyalandı", Toast.LENGTH_SHORT).show()
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
