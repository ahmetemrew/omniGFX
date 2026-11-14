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
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.basitce.gfx.core.core_engine.shizuku.ShizukuPrivilegeLevel
import com.basitce.gfx.core.core_ui.R
import com.basitce.gfx.core.core_ui.components.OmniEmptyState
import com.basitce.gfx.core.core_ui.components.OmniGameCard
import com.basitce.gfx.core.core_ui.components.OmniPrimaryButton
import com.basitce.gfx.core.core_ui.components.OmniSectionHeader
import com.basitce.gfx.core.core_ui.components.OmniShizukuBar
import com.basitce.gfx.core.core_ui.components.ShizukuGuideDialog
import com.basitce.gfx.core.core_ui.theme.OmniTheme
import com.basitce.gfx.feature.feature_home.viewmodel.HomeViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(
    onAddGameClick: () -> Unit,
    onGameClick: (String) -> Unit,
    onOpenShizukuSetup: () -> Unit = {},
    onEmptyStateChange: (Boolean) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val games by viewModel.games.collectAsStateWithLifecycle()
    val shizukuStatus by viewModel.shizukuStatus.collectAsStateWithLifecycle()
    val shizukuState by viewModel.shizukuState.collectAsStateWithLifecycle()
    val showShizukuGuide by viewModel.showShizukuGuide.collectAsStateWithLifecycle()
    val spacing = OmniTheme.spacing

    LaunchedEffect(games.isEmpty()) { onEmptyStateChange(games.isEmpty()) }

    if (showShizukuGuide) {
        ShizukuGuideDialog(onDismiss = { viewModel.dismissShizukuGuide() })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = spacing.screen)
    ) {
        Spacer(modifier = Modifier.height(spacing.lg))

        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = spacing.xs)
        )

        Spacer(modifier = Modifier.height(spacing.xl))

        OmniShizukuBar(
            status = shizukuStatus,
            connectionDetail = when (shizukuState.privilegeLevel) {
                ShizukuPrivilegeLevel.ADB_SHELL -> stringResource(R.string.shizuku_access_limited)
                ShizukuPrivilegeLevel.ROOT -> stringResource(R.string.shizuku_access_full)
                else -> stringResource(R.string.shizuku_access_unknown)
            },
            onClick = { viewModel.onShizukuBarClick() }
        )

        Spacer(modifier = Modifier.height(spacing.betweenSections))

        if (games.isEmpty()) {
            OmniEmptyState(
                title = stringResource(R.string.home_empty_title),
                description = stringResource(R.string.home_empty_description),
                icon = Icons.Outlined.SportsEsports,
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
                contentPadding = PaddingValues(bottom = spacing.section),
                verticalArrangement = Arrangement.spacedBy(spacing.listItem)
            ) {
                item(key = "header") {
                    OmniSectionHeader(
                        title = stringResource(R.string.home_my_games),
                        count = games.size
                    )
                }
                items(items = games, key = { it.id }) { game ->
                    OmniGameCard(
                        name = game.name,
                        packageName = game.packageName,
                        iconUri = game.iconUri,
                        onClick = { onGameClick(game.id) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}
