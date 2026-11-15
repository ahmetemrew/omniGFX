package com.basitce.gfx.feature.feature_wizard.ui

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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_ui.components.OmniCard
import com.basitce.gfx.core.core_ui.components.OmniErrorCard
import com.basitce.gfx.core.core_ui.components.OmniLoading
import com.basitce.gfx.core.core_ui.components.OmniPrimaryButton
import com.basitce.gfx.core.core_ui.components.OmniSecondaryButton
import com.basitce.gfx.core.core_ui.components.OmniSectionHeader
import com.basitce.gfx.core.core_ui.components.OmniStatusChip
import com.basitce.gfx.core.core_ui.components.OmniTextButton
import com.basitce.gfx.core.core_ui.components.OmniTone
import com.basitce.gfx.core.core_ui.components.OmniValueReadout
import com.basitce.gfx.core.core_ui.theme.OmniRadius
import com.basitce.gfx.core.core_ui.theme.OmniTheme
import com.basitce.gfx.feature.feature_wizard.viewmodel.ProfileType
import com.basitce.gfx.feature.feature_wizard.viewmodel.PushStepStatus
import com.basitce.gfx.feature.feature_wizard.viewmodel.SetupWizardState
import com.basitce.gfx.feature.feature_wizard.viewmodel.SetupWizardViewModel
import com.basitce.gfx.feature.feature_wizard.viewmodel.UiComponentType

@Composable
fun StepPushApply(
    state: SetupWizardState,
    viewModel: SetupWizardViewModel,
    onFinish: () -> Unit
) {
    val spacing = OmniTheme.spacing

    if (state.showGameRunningWarning) {
        AlertDialog(
            onDismissRequest = { viewModel.confirmGameRunningAndPush() },
            icon = {
                Icon(
                    Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = OmniTheme.semantic.warning
                )
            },
            title = { Text("Oyun şu an çalışıyor") },
            text = {
                Text(
                    "Değişikliklerin uygulanması için oyunun kapalı olması önerilir. " +
                        "Oyunu kapatıp yazmak ister misin?"
                )
            },
            confirmButton = {
                OmniTextButton(
                    text = "Oyunu kapat ve yaz",
                    onClick = { viewModel.forceStopGameAndPush() }
                )
            },
            dismissButton = {
                OmniTextButton(
                    text = "Yine de yaz",
                    onClick = { viewModel.confirmGameRunningAndPush() }
                )
            },
            shape = RoundedCornerShape(OmniRadius.lg),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.screen),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(spacing.xxl))

        when {
            state.isPushing -> PushingContent(state = state)
            state.pushSuccess == true -> SuccessContent(state = state, onFinish = onFinish)
            state.pushSuccess == false || state.error != null ->
                ErrorContent(state = state, viewModel = viewModel, onFinish = onFinish)
            else -> IdleContent(state = state, viewModel = viewModel)
        }

        Spacer(modifier = Modifier.height(spacing.xxl))
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun PushingContent(state: SetupWizardState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Dosya uygulanıyor",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        OmniLoading(label = "Güvenli yazma sürüyor. Bu işlem birkaç saniye sürebilir.")

        OmniCard {
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
}

@Composable
private fun ProgressStepRow(
    label: String,
    status: PushStepStatus,
    isCurrent: Boolean
) {
    val spacing = OmniTheme.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (status) {
            PushStepStatus.COMPLETED -> Icon(
                Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = OmniTheme.semantic.success,
                modifier = Modifier.size(20.dp)
            )
            PushStepStatus.RUNNING -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            PushStepStatus.FAILED -> Icon(
                Icons.Outlined.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            PushStepStatus.SKIPPED -> Icon(
                Icons.Outlined.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
            PushStepStatus.PENDING -> Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(OmniRadius.xs))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            )
        }

        Spacer(modifier = Modifier.width(spacing.md))

        Text(
            text = label,
            style = if (isCurrent) MaterialTheme.typography.titleSmall
            else MaterialTheme.typography.bodyMedium,
            color = when (status) {
                PushStepStatus.COMPLETED -> OmniTheme.semantic.success
                PushStepStatus.RUNNING -> MaterialTheme.colorScheme.onSurface
                PushStepStatus.FAILED -> MaterialTheme.colorScheme.error
                PushStepStatus.SKIPPED -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                PushStepStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun SuccessContent(
    state: SetupWizardState,
    onFinish: () -> Unit
) {
    val spacing = OmniTheme.spacing

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(OmniRadius.xl))
                .background(OmniTheme.semantic.successContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = OmniTheme.semantic.onSuccessContainer,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(spacing.xxl))

        Text(
            text = "Dosya uygulandı",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(spacing.sm))

        Text(
            text = state.pushMessage?.takeIf { it.isNotBlank() }
                ?: "Değişiklikler cihaza yazıldı.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(spacing.xxl))

        OmniCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Uygulama özeti",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(spacing.md))
            SummaryRow("Oyun", state.gameName)
            SummaryRow("Dosya", state.targetFilePath.substringAfterLast('/'))
            SummaryRow("Biçim", state.detectedFormat.label)
            SummaryRow(
                "Profil",
                if (state.profileType == ProfileType.DYNAMIC)
                    "Dinamik (${state.pinnedVariables.size} değişken)"
                else "Ham dosya"
            )
            if (state.verificationPassed == true) {
                SummaryRow("Doğrulama", "İçerik eşleşti", isHighlighted = true)
            }
            SummaryRow("Dosya kimliği", "İzinler ve sahiplik korundu", isHighlighted = true)
        }

        VariableReadouts(state = state, applied = true)

        Spacer(modifier = Modifier.height(spacing.xxxl))

        OmniPrimaryButton(
            text = "Ana ekrana dön",
            onClick = onFinish,
            icon = Icons.Outlined.Home
        )
    }
}

@Composable
private fun ErrorContent(
    state: SetupWizardState,
    viewModel: SetupWizardViewModel,
    onFinish: () -> Unit
) {
    val spacing = OmniTheme.spacing

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Dosya yazılamadı",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(spacing.lg))

        OmniErrorCard(
            title = "Yazma başarısız",
            message = state.error ?: "Beklenmeyen bir sorun oluştu. Tekrar deneyebilirsin.",
            modifier = Modifier.fillMaxWidth()
        )

        state.rollbackInfo?.let { info ->
            Spacer(modifier = Modifier.height(spacing.md))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(OmniRadius.md))
                    .background(OmniTheme.semantic.warningContainer)
                    .padding(spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                Icon(
                    Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = OmniTheme.semantic.onWarningContainer,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = info,
                    style = MaterialTheme.typography.bodySmall,
                    color = OmniTheme.semantic.onWarningContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.xxxl))

        OmniPrimaryButton(
            text = "Tekrar dene",
            onClick = { viewModel.pushAndApply() }
        )
        Spacer(modifier = Modifier.height(spacing.md))
        OmniSecondaryButton(
            text = "Vazgeç",
            onClick = onFinish
        )
    }
}

@Composable
private fun IdleContent(
    state: SetupWizardState,
    viewModel: SetupWizardViewModel
) {
    val spacing = OmniTheme.spacing

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(OmniRadius.xl))
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Code,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(spacing.xxl))

        Text(
            text = "Dosyayı cihaza yaz",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(spacing.sm))

        Text(
            text = "Düzenlenen config dosyası hedefe güvenle yazılacak. " +
                "Önce yedek alınır, yazma sonrası içerik doğrulanır; " +
                "sorun olursa eski dosya otomatik geri gelir.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(spacing.xxl))

        OmniCard(modifier = Modifier.fillMaxWidth()) {
            SummaryRow("Hedef", state.targetFilePath)
            SummaryRow("Biçim", state.detectedFormat.label)
            SummaryRow("Boyut", "${state.fileContent.length} karakter")
        }

        VariableReadouts(state = state, applied = false)

        Spacer(modifier = Modifier.height(spacing.xxxl))

        OmniPrimaryButton(
            text = "Dosyayı yaz ve uygula",
            onClick = { viewModel.pushAndApply() }
        )
    }
}

/**
 * İMZA ANI 1 — uygulama özetinde değer okumaları: her sabitlenmiş değişken
 * dosyadaki eski değer → yeni değer olarak tik raylı okuma penceresinde gösterilir.
 * "Eski" taraf pin anındaki değer değil, dosyadan çekilen ilk değerdir (originalValue);
 * böylece bu uygulamanın gerçekten neyi değiştirdiği görünür. Ray uçlarında ölçek
 * sınırları (min–max) mikro etiketle yazılır.
 */
@Composable
private fun VariableReadouts(state: SetupWizardState, applied: Boolean) {
    val spacing = OmniTheme.spacing

    if (state.profileType != ProfileType.DYNAMIC || state.pinnedVariables.isEmpty()) return

    Spacer(modifier = Modifier.height(spacing.xxl))
    OmniSectionHeader(title = "Değer okumaları")
    Spacer(modifier = Modifier.height(spacing.sm))
    state.pinnedVariables.values.forEach { variable ->
        val node = state.configNodes.firstOrNull { it.path == variable.path }
        val current = node?.value ?: variable.defaultValue.orEmpty()
        val previous = node?.originalValue?.takeIf { it.isNotEmpty() } ?: variable.defaultValue
        val isSlider = variable.uiComponentType == UiComponentType.SLIDER &&
            variable.max > variable.min
        val fraction = if (isSlider) {
            ((current.toFloatOrNull() ?: variable.min) - variable.min) / (variable.max - variable.min)
        } else null
        OmniValueReadout(
            label = variable.label,
            value = current,
            previousValue = previous,
            fraction = fraction,
            minLabel = if (isSlider) formatScale(variable.min) else null,
            maxLabel = if (isSlider) formatScale(variable.max) else null,
            trailingLabel = if (applied) "Uygulandı" else null
        )
        Spacer(modifier = Modifier.height(spacing.sm))
    }
}

private fun formatScale(value: Float): String =
    if (value == value.toLong().toFloat()) value.toLong().toString() else value.toString()

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    val spacing = OmniTheme.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(spacing.md))
        if (isHighlighted) {
            OmniStatusChip(text = value, tone = OmniTone.Success)
        } else {
            Text(
                text = value,
                style = OmniTheme.mono.key,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }
    }
}