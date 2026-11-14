package com.basitce.gfx.ui.shizuku

import android.content.Intent
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.basitce.gfx.core.core_ui.components.OmniCard
import com.basitce.gfx.core.core_ui.components.OmniEmphasisPanel
import com.basitce.gfx.core.core_ui.components.OmniLoading
import com.basitce.gfx.core.core_ui.components.OmniPrimaryButton
import com.basitce.gfx.core.core_ui.components.OmniSecondaryButton
import com.basitce.gfx.core.core_ui.components.OmniTextButton
import com.basitce.gfx.core.core_ui.components.ShizukuGuideDialog
import com.basitce.gfx.core.core_ui.theme.OmniTheme

@Composable
fun ShizukuSetupScreen(
    onContinue: () -> Unit,
    viewModel: ShizukuSetupViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showGuide by remember { mutableStateOf(false) }
    val spacing = OmniTheme.spacing

    if (showGuide) {
        ShizukuGuideDialog(
            onDismiss = {
                showGuide = false
                viewModel.check()
            }
        )
    }

    LaunchedEffect(state.canContinue) {
        if (state.canContinue) {
            onContinue()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = spacing.screen),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(spacing.lg))
            Text(
                text = "Önce bağlantıyı kuralım",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(spacing.sm))
            Text(
                text = "OmniGFX, oyunların config dosyalarına Shizuku üzerinden erişir. " +
                    "İki adımda hazır olursun.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(spacing.xxxl))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.lg)
            ) {
                item {
                    SetupStepCard(
                        stepNumber = 1,
                        title = "Shizuku'yu yükle",
                        description = "Önce Shizuku uygulamasını telefonuna kur.",
                        actionText = "İndir",
                        onAction = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = "market://details?id=moe.shizuku.privileged.api".toUri()
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                                    data = "https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api".toUri()
                                }
                                context.startActivity(webIntent)
                            }
                        }
                    )
                }
                item {
                    SetupStepCard(
                        stepNumber = 2,
                        title = "Servisi başlat",
                        description = "Shizuku uygulamasını aç ve servisi başlat. " +
                            "Bilgisayarın varsa ADB ile de başlatabilirsin.",
                        actionText = "Shizuku'yu aç",
                        onAction = {
                            val intent = context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                            if (intent != null) {
                                context.startActivity(intent)
                            }
                        },
                        secondaryActionText = "Nasıl yapılır?",
                        onSecondaryAction = { showGuide = true }
                    )
                }
                item {
                    OmniEmphasisPanel {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (state.canContinue) Icons.Outlined.CheckCircle
                                    else Icons.Outlined.Info,
                                contentDescription = null,
                                tint = if (state.canContinue) OmniTheme.semantic.success
                                    else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(spacing.sm))
                            Text(
                                text = state.message ?: "Durum kontrol ediliyor…",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(spacing.lg))
                            if (state.isChecking) {
                                OmniLoading(label = "Bağlantı denetleniyor")
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                                    OmniSecondaryButton(
                                        text = "Tekrar denetle",
                                        onClick = { viewModel.check() },
                                        fillWidth = false
                                    )
                                    if (!state.permissionGranted) {
                                        OmniPrimaryButton(
                                            text = "İzin iste",
                                            onClick = { viewModel.requestPermission() },
                                            fillWidth = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.lg))
            OmniPrimaryButton(
                text = "OmniGFX'i başlat",
                onClick = onContinue,
                enabled = state.canContinue
            )
            Spacer(modifier = Modifier.height(spacing.xxl))
        }
    }
}

@Composable
fun SetupStepCard(
    stepNumber: Int,
    title: String,
    description: String,
    actionText: String,
    onAction: () -> Unit,
    secondaryActionText: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    val spacing = OmniTheme.spacing

    OmniCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber.toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(modifier = Modifier.width(spacing.lg))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(spacing.xxs))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(spacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    OmniTextButton(text = actionText, onClick = onAction)
                    secondaryActionText?.let {
                        OmniTextButton(text = it, onClick = { onSecondaryAction?.invoke() })
                    }
                }
            }
        }
    }
}