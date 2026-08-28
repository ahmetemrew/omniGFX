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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    val adbCommand = "adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Shizuku Kurulumu",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "OmniGFX, oyun ayarlarını root olmadan değiştirmek için Shizuku'yu kullanır.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Adım 1: Shizuku uygulamasını indirin.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = "Adım 2: Bilgisayardan şu komutu bir kez çalıştırın:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { copyToClipboard(context, adbCommand) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(adbCommand)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Adım 3: OmniGFX'e izin verin.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { openShizukuDownload(context) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Shizuku'yu İndir")
            }
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
