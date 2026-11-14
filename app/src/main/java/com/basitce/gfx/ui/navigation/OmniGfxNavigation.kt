package com.basitce.gfx.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.basitce.gfx.core.core_ui.theme.OmniTheme
import com.basitce.gfx.feature.feature_wizard.ui.AppPickerScreen
import com.basitce.gfx.feature.feature_wizard.ui.GameDetailScreen
import com.basitce.gfx.feature.feature_wizard.ui.SetupWizardScreen
import com.basitce.gfx.ui.backup.BackupManagerScreen
import com.basitce.gfx.ui.guide.GuideScreen
import com.basitce.gfx.ui.main.MainScreen
import com.basitce.gfx.ui.marketplace.ProfileMarketplaceScreen
import com.basitce.gfx.ui.shizuku.ShizukuSetupScreen
import com.basitce.gfx.ui.sync.ProfileSyncScreen

object Routes {
    const val HOME = "home"
    const val APP_PICKER = "app_picker"
    const val GAME_DETAIL = "game_detail/{gameId}?profileId={profileId}"
    const val SETUP_WIZARD = "wizard/{packageName}"
    const val SHIZUKU_SETUP = "shizuku_setup"
    const val BACKUP_MANAGER = "backup_manager"
    const val MARKETPLACE = "marketplace"
    const val SYNC = "sync"
    const val GUIDE = "guide"

    fun gameDetail(gameId: String, profileId: String = "") = "game_detail/$gameId?profileId=$profileId"
    fun setupWizard(packageName: String) = "wizard/$packageName"
}

@Composable
fun OmniGfxNavHost(
    navController: NavHostController = rememberNavController()
) {
    val motion = OmniTheme.motion
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = {
            if (motion.reduced) fadeIn(motion.shortTween())
            else slideInHorizontally(motion.mediumTween()) { it / 4 } + fadeIn(motion.mediumTween())
        },
        exitTransition = {
            if (motion.reduced) fadeOut(motion.shortTween())
            else slideOutHorizontally(motion.mediumTween()) { -it / 4 } + fadeOut(motion.mediumTween())
        },
        popEnterTransition = {
            if (motion.reduced) fadeIn(motion.shortTween())
            else slideInHorizontally(motion.mediumTween()) { -it / 4 } + fadeIn(motion.mediumTween())
        },
        popExitTransition = {
            if (motion.reduced) fadeOut(motion.shortTween())
            else slideOutHorizontally(motion.mediumTween()) { it / 4 } + fadeOut(motion.mediumTween())
        }
    ) {
        composable(Routes.HOME) {
            MainScreen(
                onAddGame = { navController.navigate(Routes.APP_PICKER) },
                onGameClick = { gameId -> navController.navigate(Routes.gameDetail(gameId)) },
                onLoadProfile = { gameId, profileId -> navController.navigate(Routes.gameDetail(gameId, profileId)) },
                onOpenBackupManager = { navController.navigate(Routes.BACKUP_MANAGER) },
                onOpenMarketplace = { navController.navigate(Routes.MARKETPLACE) },
                onOpenSync = { navController.navigate(Routes.SYNC) },
                onOpenShizukuSetup = { navController.navigate(Routes.SHIZUKU_SETUP) },
                onOpenFileWorkflow = { navController.navigate(Routes.APP_PICKER) },
                onOpenGuide = { navController.navigate(Routes.GUIDE) }
            )
        }

        composable(Routes.APP_PICKER) {
            AppPickerScreen(
                onGameSelected = { packageName ->
                    navController.navigate(Routes.setupWizard(packageName)) {
                        popUpTo(Routes.HOME)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.GAME_DETAIL,
            arguments = listOf(navArgument("gameId") { type = NavType.StringType }, navArgument("profileId") { type = NavType.StringType; defaultValue = "" })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: return@composable
            GameDetailScreen(
                gameId = gameId,
                onNewProfile = { packageName -> navController.navigate(Routes.setupWizard(packageName)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.SETUP_WIZARD,
            arguments = listOf(navArgument("packageName") { type = NavType.StringType })
        ) { backStackEntry ->
            val packageName = backStackEntry.arguments?.getString("packageName") ?: return@composable
            SetupWizardScreen(
                packageName = packageName,
                onFinish = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SHIZUKU_SETUP) {
            ShizukuSetupScreen(onContinue = { navController.popBackStack() })
        }

        composable(Routes.BACKUP_MANAGER) {
            BackupManagerScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.MARKETPLACE) {
            ProfileMarketplaceScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SYNC) {
            ProfileSyncScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.GUIDE) {
            GuideScreen(onBack = { navController.popBackStack() })
        }
    }
}
