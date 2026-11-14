package com.basitce.gfx.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_ui.theme.OmniTheme
import com.basitce.gfx.feature.feature_home.ui.HomeScreen
import com.basitce.gfx.ui.profile.ProfileListScreen

private enum class MainTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Games("Oyunlar", Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports),
    Profiles("Profiller", Icons.Filled.Tune, Icons.Outlined.Tune),
    Settings("Ayarlar", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainScreen(
    onAddGame: () -> Unit,
    onGameClick: (String) -> Unit,
    onLoadProfile: (gameId: String, profileId: String) -> Unit,
    onOpenBackupManager: () -> Unit,
    onOpenMarketplace: () -> Unit,
    onOpenSync: () -> Unit,
    onOpenShizukuSetup: () -> Unit,
    onOpenFileWorkflow: () -> Unit,
    onOpenGuide: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Games) }
    // Boş durumda davet butonu tek birincil eylemdir; FAB aynı eylemi tekrarlamasın.
    var gamesEmpty by rememberSaveable { mutableStateOf(true) }
    val colors = MaterialTheme.colorScheme
    val motion = OmniTheme.motion

    Scaffold(
        containerColor = colors.background,
        floatingActionButton = {
            if (selectedTab == MainTab.Games && !gamesEmpty) {
                ExtendedFloatingActionButton(
                    onClick = onAddGame,
                    icon = {
                        Icon(Icons.Filled.Add, contentDescription = null)
                    },
                    text = { Text("Oyun ekle", style = MaterialTheme.typography.labelLarge) },
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = colors.surfaceContainer,
                tonalElevation = 0.dp
            ) {
                MainTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = null
                            )
                        },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelMedium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colors.onSecondaryContainer,
                            selectedTextColor = colors.onSurface,
                            indicatorColor = colors.secondaryContainer,
                            unselectedIconColor = colors.onSurfaceVariant,
                            unselectedTextColor = colors.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                if (motion.reduced) {
                    fadeIn(animationSpec = motion.shortTween()) togetherWith
                        fadeOut(animationSpec = motion.shortTween())
                } else {
                    val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                    (slideInHorizontally(animationSpec = motion.mediumTween()) { it / 6 * direction } +
                        fadeIn(motion.mediumTween())) togetherWith
                        (slideOutHorizontally(animationSpec = motion.mediumTween()) { -it / 6 * direction } +
                            fadeOut(motion.mediumTween()))
                }
            },
            label = "main_tab_switch",
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) { tab ->
            when (tab) {
                MainTab.Games -> HomeScreen(
                    onAddGameClick = onAddGame,
                    onGameClick = onGameClick,
                    onOpenShizukuSetup = onOpenShizukuSetup,
                    onEmptyStateChange = { gamesEmpty = it }
                )
                MainTab.Profiles -> ProfileListScreen(
                    onLoadProfile = onLoadProfile,
                    onOpenBackupManager = onOpenBackupManager,
                    onOpenMarketplace = onOpenMarketplace,
                    onOpenSync = onOpenSync,
                    onOpenShizukuSetup = onOpenShizukuSetup,
                    onAddGame = onAddGame
                )
                MainTab.Settings -> SettingsScreen(
                    onOpenShizukuSetup = onOpenShizukuSetup,
                    onOpenFileWorkflow = onOpenFileWorkflow,
                    onOpenGuide = onOpenGuide
                )
            }
        }
    }
}
