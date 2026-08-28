package com.basitce.gfx.feature.feature_wizard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.basitce.gfx.core.core_database.entity.ProfileEntity
import com.basitce.gfx.core.core_ui.theme.OmniError
import com.basitce.gfx.core.core_ui.theme.OmniOnPrimary
import com.basitce.gfx.core.core_ui.theme.OmniOnSurface
import com.basitce.gfx.core.core_ui.theme.OmniOnSurfaceVariant
import com.basitce.gfx.core.core_ui.theme.OmniPrimary
import com.basitce.gfx.core.core_ui.theme.OmniSuccess
import com.basitce.gfx.core.core_ui.theme.OmniSurface
import com.basitce.gfx.feature.feature_wizard.viewmodel.ProfileApplyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileApplySheet(
    profile: ProfileEntity,
    onDismiss: () -> Unit,
    viewModel: ProfileApplyViewModel = hiltViewModel()
) {
    val state by viewModel.applyState.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = OmniSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Profili Uygula",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = OmniOnSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "\"${profile.name}\"",
                style = MaterialTheme.typography.bodyMedium,
                color = OmniOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            when {
                state.isApplying -> {
                    CircularProgressIndicator(
                        color = OmniPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.progressMessage.ifBlank { "Uygulanıyor..." },
                        style = MaterialTheme.typography.bodyLarge,
                        color = OmniOnSurfaceVariant
                    )
                }

                state.isSuccess == true -> {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = OmniSuccess,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Başarıyla Uygulandı!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = OmniSuccess
                    )
                    state.message?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OmniOnSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OmniPrimary)
                    ) {
                        Text("Kapat", color = OmniOnPrimary)
                    }
                }

                state.isSuccess == false -> {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = OmniError,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Uygulama Başarısız",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = OmniError
                    )
                    state.message?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OmniOnSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("İptal", color = OmniOnSurfaceVariant)
                        }
                        Button(
                            onClick = { viewModel.applyProfile(profile) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OmniError)
                        ) {
                            Text("Tekrar Dene", color = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                }

                else -> {
                    val typeText = if (profile.isManual) {
                        "Ham dosya profili. Dosya olduğu gibi hedefe yazılacak."
                    } else {
                        "Dinamik profil. Değişkenler PMP pipeline ile uygulanacak."
                    }
                    Text(
                        text = typeText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OmniOnSurfaceVariant
                    )

                    if (profile.targetFilePath.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Hedef: ${profile.targetFilePath}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = OmniOnSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.applyProfile(profile) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OmniPrimary)
                    ) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Uygula",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = OmniOnPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
