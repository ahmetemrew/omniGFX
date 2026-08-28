package com.basitce.gfx.feature.feature_wizard.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_ui.theme.OmniBackground
import com.basitce.gfx.core.core_ui.theme.OmniError
import com.basitce.gfx.core.core_ui.theme.OmniOnPrimary
import com.basitce.gfx.core.core_ui.theme.OmniOnSurface
import com.basitce.gfx.core.core_ui.theme.OmniOnSurfaceVariant
import com.basitce.gfx.core.core_ui.theme.OmniPrimary
import com.basitce.gfx.core.core_ui.theme.OmniSuccess
import com.basitce.gfx.core.core_ui.theme.OmniSurface
import com.basitce.gfx.core.core_ui.theme.OmniWarning
import com.basitce.gfx.feature.feature_wizard.viewmodel.ProfileType
import com.basitce.gfx.feature.feature_wizard.viewmodel.PushProgressStep
import com.basitce.gfx.feature.feature_wizard.viewmodel.PushStepStatus
import com.basitce.gfx.feature.feature_wizard.viewmodel.SetupWizardState
import com.basitce.gfx.feature.feature_wizard.viewmodel.SetupWizardViewModel

@Composable
fun StepPushApply(
    state: SetupWizardState,
    viewModel: SetupWizardViewModel,
    onFinish: () -> Unit
) {
    if (state.showGameRunningWarning) {
        AlertDialog(
            onDismissRequest = { viewModel.confirmGameRunningAndPush() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = OmniWarning
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Oyun Şu An Çalışıyor", color = OmniOnSurface)
                }
            },
            text = {
                Text(
                    "Değişikliklerin uygulanması için oyunun kapalı olması önerilir. Oyunu kapatıp yazmak ister misiniz?",
                    color = OmniOnSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.forceStopGameAndPush() },
                    colors = ButtonDefaults.buttonColors(containerColor = OmniPrimary)
                ) {
                    Text("Oyunu Kapat ve Yaz", color = OmniOnPrimary)
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { viewModel.confirmGameRunningAndPush() }) {
                        Text("Yine de Yaz", color = OmniWarning)
                    }
                }
            },
            containerColor = OmniSurface
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OmniBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        when {
            state.isPushing -> {
                PushingContent(state = state)
            }
            state.pushSuccess == true -> {
                SuccessContent(state = state, onFinish = onFinish)
            }
            state.pushSuccess == false || state.error != null -> {
                ErrorContent(state = state, viewModel = viewModel, onFinish = onFinish)
            }
            else -> {
                IdleContent(state = state, viewModel = viewModel)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun PushingContent(state: SetupWizardState) {
    val infiniteTransition = rememberInfiniteTransition(label = "push_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "push_rotation"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Sync,
                contentDescription = null,
                tint = OmniPrimary.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(100.dp)
                    .rotate(rotation)
            )
            Icon(
                Icons.Default.Code,
                contentDescription = null,
                tint = OmniPrimary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Dosya Uygulanıyor",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = OmniOnSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        state.pushProgressSteps.forEachIndexed { index, step ->
            ProgressStepRow(
                label = step.label,
                status = step.status,
                isCurrent = index == state.currentPushStep &&
                    step.status == PushStepStatus.RUNNING
            )
        }
    }
}

@Composable
private fun ProgressStepRow(
    label: String,
    status: PushStepStatus,
    isCurrent: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (status) {
            PushStepStatus.COMPLETED -> {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = OmniSuccess,
                    modifier = Modifier.size(20.dp)
                )
            }
            PushStepStatus.RUNNING -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = OmniPrimary
                )
            }
            PushStepStatus.FAILED -> {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = OmniError,
                    modifier = Modifier.size(20.dp)
                )
            }
            PushStepStatus.SKIPPED -> {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = OmniOnSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
            PushStepStatus.PENDING -> {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(OmniOnSurfaceVariant.copy(alpha = 0.3f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
            ),
            color = when (status) {
                PushStepStatus.COMPLETED -> OmniSuccess
                PushStepStatus.RUNNING -> OmniOnSurface
                PushStepStatus.FAILED -> OmniError
                PushStepStatus.SKIPPED -> OmniOnSurfaceVariant.copy(alpha = 0.4f)
                PushStepStatus.PENDING -> OmniOnSurfaceVariant.copy(alpha = 0.6f)
            }
        )
    }
}

@Composable
private fun SuccessContent(
    state: SetupWizardState,
    onFinish: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(OmniSuccess.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = OmniSuccess,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "İşlem Başarılı!",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = OmniSuccess
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            color = OmniSurface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Uygulama Özeti",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OmniOnSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                SummaryRow("Oyun", state.gameName)
                SummaryRow("Dosya", state.targetFilePath.substringAfterLast('/'))
                SummaryRow("Format", state.detectedFormat.label)
                SummaryRow(
                    "Profil",
                    if (state.profileType == ProfileType.DYNAMIC)
                        "Dinamik (${state.pinnedVariables.size} değişken)"
                    else "Ham Dosya"
                )
                if (state.verificationPassed == true) {
                    SummaryRow("Doğrulama", "✅ Hash eşleşti", isHighlighted = true)
                }
                SummaryRow(
                    "Anti-Cheat Koruması",
                    "Metadata Geri Yüklendi",
                    isHighlighted = true
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OmniPrimary)
        ) {
            Icon(
                Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ana Ekrana Dön",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OmniOnPrimary
            )
        }
    }
}

@Composable
private fun ErrorContent(
    state: SetupWizardState,
    viewModel: SetupWizardViewModel,
    onFinish: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(OmniError.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = OmniError,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "İşlem Başarısız",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = OmniError
        )

        Spacer(modifier = Modifier.height(16.dp))

        state.error?.let { error ->
            Surface(
                color = OmniSurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = error,
                    color = OmniOnSurface,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        state.rollbackInfo?.let { info ->
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = OmniWarning.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = OmniWarning,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = info,
                        style = MaterialTheme.typography.bodySmall,
                        color = OmniWarning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onFinish,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("İptal", color = OmniOnSurface)
            }
            Button(
                onClick = { viewModel.pushAndApply() },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OmniError)
            ) {
                Text("Tekrar Dene", color = Color.White)
            }
        }
    }
}

@Composable
private fun IdleContent(
    state: SetupWizardState,
    viewModel: SetupWizardViewModel
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(OmniSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Code,
                contentDescription = null,
                tint = OmniPrimary,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Dosyayı Cihaza Yaz",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = OmniOnSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Düzenlenen config dosyası Shizuku üzerinden\n" +
                "hedefe güvenli şekilde yazılacak.\n" +
                "Metadata (UID/GID/SELinux) korunacak.",
            style = MaterialTheme.typography.bodyMedium,
            color = OmniOnSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            color = OmniSurface,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SummaryRow("Hedef", state.targetFilePath)
                SummaryRow("Format", state.detectedFormat.label)
                SummaryRow("Boyut", "${state.fileContent.length} karakter")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.pushAndApply() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OmniPrimary)
        ) {
            Text(
                text = "Dosyayı Yaz ve Uygula",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OmniOnPrimary
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = OmniOnSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.SemiBold,
                fontFamily = if (isHighlighted) FontFamily.Default else FontFamily.Monospace
            ),
            color = if (isHighlighted) OmniSuccess else OmniOnSurface,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}
