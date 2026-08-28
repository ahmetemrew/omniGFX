package com.basitce.gfx.feature.feature_home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.basitce.gfx.core.core_ui.R
import com.basitce.gfx.core.core_ui.components.OmniCard
import com.basitce.gfx.core.core_ui.components.OmniEmptyState
import com.basitce.gfx.core.core_ui.components.OmniGameCard
import com.basitce.gfx.core.core_ui.components.OmniPrimaryButton
import com.basitce.gfx.core.core_ui.components.OmniShizukuBar
import com.basitce.gfx.core.core_ui.components.ShizukuGuideDialog
import com.basitce.gfx.feature.feature_home.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onAddGameClick: () -> Unit,
    onGameClick: (String) -> Unit,
    onOpenShizukuSetup: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val games by viewModel.games.collectAsStateWithLifecycle()
    val shizukuStatus by viewModel.shizukuStatus.collectAsStateWithLifecycle()
    val showShizukuGuide by viewModel.showShizukuGuide.collectAsStateWithLifecycle()

    if (showShizukuGuide) {
        ShizukuGuideDialog(onDismiss = { viewModel.dismissShizukuGuide() })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        OmniShizukuBar(
            status = shizukuStatus,
            onClick = { viewModel.onShizukuBarClick() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (games.isEmpty()) {
            OmniEmptyState(
                title = stringResource(R.string.home_empty_title),
                description = stringResource(R.string.home_empty_description),
                icon = Icons.Default.Settings,
                action = {
                    OmniPrimaryButton(
                        text = stringResource(R.string.home_add_game),
                        onClick = onAddGameClick
                    )
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OmniCard {
                        Text(
                            text = stringResource(R.string.home_my_games),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                items(games) { game ->
                    OmniGameCard(
                        name = game.name,
                        packageName = game.packageName,
                        iconUri = game.iconUri,
                        onClick = { onGameClick(game.id) }
                    )
                }
            }
        }
    }
}
