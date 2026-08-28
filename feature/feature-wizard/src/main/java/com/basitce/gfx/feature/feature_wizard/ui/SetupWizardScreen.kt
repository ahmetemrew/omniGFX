package com.basitce.gfx.feature.feature_wizard.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.basitce.gfx.core.core_ui.theme.OmniOnSurfaceVariant
import com.basitce.gfx.core.core_ui.theme.OmniOutline
import com.basitce.gfx.core.core_ui.theme.OmniPrimary
import com.basitce.gfx.core.core_ui.theme.OmniSuccess
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

    LaunchedEffect(packageName) {
        viewModel.init(packageName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        WizardTopBar(
            currentStep = state.currentStep,
            gameName = state.gameName,
            onBack = {
                if (state.currentStep.isFirst) {
                    onBack()
                } else {
                    viewModel.goBack()
                }
            }
        )

        StepIndicator(currentStep = state.currentStep)

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(
            targetState = state.currentStep,
            transitionSpec = {
                val direction = if (targetState.order > initialState.order) 1 else -1
                (slideInHorizontally(tween(300)) { it * direction } + fadeIn(tween(300)))
                    .togetherWith(
                        slideOutHorizontally(tween(300)) { -it * direction } + fadeOut(tween(300))
                    )
            },
            label = "wizard_step_transition",
            modifier = Modifier.weight(1f)
        ) { step ->
            when (step) {
                SetupWizardStep.SELECT_FILE -> StepSelectFile(
                    state = state,
                    viewModel = viewModel
                )
                SetupWizardStep.PULL_ANALYZE -> StepPullAnalyze(
                    state = state,
                    viewModel = viewModel
                )
                SetupWizardStep.EDIT -> StepEdit(
                    state = state,
                    viewModel = viewModel
                )
                SetupWizardStep.PIN_VARIABLES -> StepPinVariables(
                    state = state,
                    viewModel = viewModel
                )
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Geri",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = currentStep.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (gameName.isNotBlank()) {
                Text(
                    text = gameName,
                    style = MaterialTheme.typography.bodySmall,
                    color = OmniOnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: SetupWizardStep) {
    val steps = SetupWizardStep.entries.filter { it != SetupWizardStep.DONE }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isCompleted = step.order < currentStep.order
            val isCurrent = step == currentStep

            val color = when {
                isCompleted -> OmniSuccess
                isCurrent -> OmniPrimary
                else -> OmniOutline
            }

            Box(
                modifier = Modifier
                    .size(if (isCurrent) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(color)
            )

            if (index < steps.lastIndex) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}
