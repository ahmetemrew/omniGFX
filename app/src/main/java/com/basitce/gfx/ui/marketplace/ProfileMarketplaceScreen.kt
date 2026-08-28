package com.basitce.gfx.ui.marketplace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.basitce.gfx.core.core_engine.profile.FindingSeverity
import com.basitce.gfx.core.core_engine.profile.ProfileRiskLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMarketplaceScreen(
    onBack: () -> Unit,
    viewModel: ProfileMarketplaceViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Marketplace") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.jsonInput,
                onValueChange = viewModel::onJsonInputChange,
                label = { Text("Profile JSON") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            Row {
                Button(onClick = { viewModel.analyze() }) {
                    Text("Analyze")
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { viewModel.importProfile() },
                    enabled = state.analysis?.canImport == true && !state.isImporting
                ) {
                    Text("Import")
                }
            }

            if (state.isAnalyzing || state.isImporting) {
                CircularProgressIndicator()
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }

            state.message?.let { message ->
                Text(text = message)
            }

            state.analysis?.let { analysis ->
                val riskColor = when (analysis.riskLevel) {
                    ProfileRiskLevel.SAFE -> Color(0xFF4CAF50)
                    ProfileRiskLevel.LOW -> Color(0xFF8BC34A)
                    ProfileRiskLevel.MEDIUM -> Color(0xFFFFC107)
                    ProfileRiskLevel.HIGH -> Color(0xFFFF5722)
                    ProfileRiskLevel.CRITICAL -> Color(0xFFE53935)
                }

                Text(
                    text = "Score: ${analysis.score}/100",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Risk: ${analysis.riskLevel.name}",
                    color = riskColor,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Findings",
                    style = MaterialTheme.typography.titleSmall
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(analysis.findings) { finding ->
                        val severityColor = when (finding.severity) {
                            FindingSeverity.INFO -> Color(0xFF2196F3)
                            FindingSeverity.WARNING -> Color(0xFFFFC107)
                            FindingSeverity.HIGH -> Color(0xFFFF5722)
                            FindingSeverity.CRITICAL -> Color(0xFFE53935)
                        }

                        Text(
                            text = "[${finding.severity.name}] ${finding.message}",
                            color = severityColor,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
