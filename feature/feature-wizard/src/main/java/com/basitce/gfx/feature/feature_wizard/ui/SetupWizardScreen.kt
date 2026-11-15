package com.basitce.gfx.feature.feature_wizard.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.basitce.gfx.core.core_ui.theme.OmniRadius
import com.basitce.gfx.core.core_ui.theme.OmniTheme
import com.basitce.gfx.feature.feature_wizard.viewmodel.SetupWizardStep
import com.basitce.gfx.feature.feature_wizard.viewmodel.SetupWizardViewModel

@Composable
fun SetupWizardScreen(
    packageName: String,
    onFinish: () -> Unit,
    onBack: () -> Unit,
    viewModel: SetupWizardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = OmniTheme.spacing
    val motion = OmniTheme.motion

    BackHandler(enabled = !state.currentStep.isFirst) { viewModel.goBack() }

    LaunchedEffect(packageName) {
        viewModel.init(packageName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .imePadding()
    ) {
        WizardTopBar(
            currentStep = state.currentStep,
            gameName = state.gameName,
            onBack = {
                if (state.currentStep.isFirst) onBack() else viewModel.goBack()
            }
        )

        StepRail(currentStep = state.currentStep)

        Spacer(modifier = Modifier.height(spacing.lg))

        AnimatedContent(
            targetState = state.currentStep,
            transitionSpec = {
                if (motion.reduced) {
                    fadeIn(motion.shortTween()) togetherWith fadeOut(motion.shortTween())
                } else {
                    val direction = if (targetState.order > initialState.order) 1 else -1
                    (slideInHorizontally(motion.longTween()) { it / 4 * direction } +
                        fadeIn(motion.longTween())) togetherWith
                        (slideOutHorizontally(motion.longTween()) { -it / 4 * direction } +
                            fadeOut(motion.longTween()))
                }
            },
            label = "wizard_step_transition",
            modifier = Modifier.weight(1f)
        ) { step ->
            when (step) {
                SetupWizardStep.SELECT_FILE -> StepSelectFile(state = state, viewModel = viewModel)
                SetupWizardStep.PULL_ANALYZE -> StepPullAnalyze(state = state, viewModel = viewModel)
                SetupWizardStep.EDIT -> StepEdit(state = state, viewModel = viewModel)
                SetupWizardStep.PIN_VARIABLES -> StepPinVariables(state = state, viewModel = viewModel)
                SetupWizardStep.PUSH_APPLY -> StepPushApply(
                    state = state,
                    viewModel = viewModel,
                    onFinish = onFinish
                )
                SetupWizardStep.DONE -> {
                    LaunchedEffect(Unit) { onFinish() }
                }
            }
        }
    }
}

@Composable
private fun WizardTopBar(
    currentStep: SetupWizardStep,
    gameName: String,
    onBack: () -> Unit
) {
    val spacing = OmniTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.sm, vertical = spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Geri",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.width(spacing.xs))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currentStep.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (gameName.isNotBlank()) {
                Text(
                    text = gameName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = "${currentStep.order} / ${SetupWizardStep.entries.size - 1}",
            style = OmniTheme.mono.micro,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = spacing.sm)
        )
    }
}

/**
 * Adım rayı: noktalar yerine segmentli ilerleme. Gerçek bir sıra olduğu için
 * numaralandırma burada meşru. Tamamlanan = petrol, aktif = pirinç, gelecek = sönük.
 */
@Composable
private fun StepRail(currentStep: SetupWizardStep) {
    val steps = SetupWizardStep.entries.filter { it != SetupWizardStep.DONE }
    val spacing = OmniTheme.spacing
    val motion = OmniTheme.motion
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screen)
            .semantics {
                contentDescription = "Adım ${currentStep.order} / ${steps.size}: ${currentStep.title}"
            },
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEach { step ->
            val completed = step.order < currentStep.order
            val active = step == currentStep
            val target = when {
                completed -> colors.primary
                active -> colors.secondary
                else -> colors.outlineVariant
            }
            val segmentColor by animateColorAsState(
                targetValue = target,
                animationSpec = motion.mediumTween(),
                label = "step_segment_color"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(if (active) 4.dp else 2.dp)
                    .clip(RoundedCornerShape(OmniRadius.xs))
                    .background(segmentColor)
            )
        }
    }
}
