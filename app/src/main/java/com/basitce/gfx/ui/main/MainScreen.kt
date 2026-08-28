package com.basitce.gfx.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.basitce.gfx.core.core_ui.theme.OmniBackground
import com.basitce.gfx.core.core_ui.theme.OmniOnPrimary
import com.basitce.gfx.core.core_ui.theme.OmniOnSurfaceVariant
import com.basitce.gfx.core.core_ui.theme.OmniPrimary
import com.basitce.gfx.core.core_ui.theme.OmniSurface
import com.basitce.gfx.feature.feature_home.ui.HomeScreen
import com.basitce.gfx.ui.profile.ProfileListScreen

private enum class MainTab(
    val label: String,
    val icon: ImageVector
) {
    Games("Oyunlar", Icons.Default.Home),
    Profiles("Profiller", Icons.Default.Person),
    Settings("Ayarlar", Icons.Default.Settings)
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

    Scaffold(
        containerColor = OmniBackground,
        floatingActionButton = {
            if (selectedTab == MainTab.Games) {
                FloatingActionButton(
                    onClick = onAddGame,
                    containerColor = OmniPrimary,
                    contentColor = OmniOnPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Oyun Ekle")
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = OmniSurface,
                tonalElevation = 4.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                MainTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OmniPrimary,
                            selectedTextColor = OmniPrimary,
                            indicatorColor = OmniPrimary.copy(alpha = 0.15f),
                            unselectedIconColor = OmniOnSurfaceVariant,
                            unselectedTextColor = OmniOnSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                (slideInHorizontally(animationSpec = tween(250)) { it * direction } + fadeIn(tween(250))) togetherWith
                    (slideOutHorizontally(animationSpec = tween(250)) { -it * direction } + fadeOut(tween(250)))
            },
            label = "main_tab_switch",
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) { tab ->
            when (tab) {
                MainTab.Games -> HomeScreen(
                    onAddGameClick = onAddGame,
                    onGameClick = onGameClick,
                    onOpenShizukuSetup = onOpenShizukuSetup
                )
                MainTab.Profiles -> ProfileListScreen(
                    onLoadProfile = onLoadProfile,
                    onOpenBackupManager = onOpenBackupManager,
                    onOpenMarketplace = onOpenMarketplace,
                    onOpenSync = onOpenSync,
                    onOpenShizukuSetup = onOpenShizukuSetup
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
