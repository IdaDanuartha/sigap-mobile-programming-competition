package id.sigap.app.core.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import id.sigap.app.feature.alert.AlertDetailScreen
import id.sigap.app.feature.check.CheckScreen
import id.sigap.app.feature.checkup.CheckupScreen
import id.sigap.app.feature.emergency.EmergencyScreen
import id.sigap.app.feature.emergency.ReportDraftScreen
import id.sigap.app.feature.home.MainScreen
import id.sigap.app.feature.learn.AgilityQuizScreen
import id.sigap.app.feature.learn.ArticleDetailScreen
import id.sigap.app.feature.lookup.EntityLookupScreen
import id.sigap.app.feature.onboarding.OnboardingScreen
import id.sigap.app.feature.permissions.PermissionsScreen
import id.sigap.app.feature.simulation.NotificationSimulationScreen

private const val TRANSITION_MS = 250
private const val SLIDE_PX = 48

@Composable
fun SigapNavHost(
    startOnboarding: Boolean,
    onOnboardingFinished: () -> Unit,
    launchRequest: LaunchRequest?,
    onLaunchRequestHandled: () -> Unit,
) {
    val navController = rememberNavController()
    val fade = tween<Float>(TRANSITION_MS, easing = FastOutSlowInEasing)
    val clearBackStack: NavOptionsBuilder.() -> Unit = { popUpTo(0) { inclusive = true } }

    LaunchedEffect(launchRequest) {
        when (launchRequest) {
            null -> Unit
            is LaunchRequest.OpenAlert -> {
                if (!startOnboarding) navController.navigate("alert/${launchRequest.id}")
                onLaunchRequestHandled()
            }
            is LaunchRequest.SharedText, is LaunchRequest.SharedImage ->
                if (startOnboarding) onLaunchRequestHandled() else navController.navigate("check")
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (startOnboarding) "onboarding" else "main",
        enterTransition = {
            fadeIn(fade) + slideInHorizontally(tween(TRANSITION_MS, easing = FastOutSlowInEasing)) { SLIDE_PX }
        },
        exitTransition = { fadeOut(fade) },
        popEnterTransition = { fadeIn(fade) },
        popExitTransition = { fadeOut(fade) },
    ) {
        composable("onboarding") {
            OnboardingScreen(onFinished = { navController.navigate("permissions?fromSettings=false") })
        }
        composable(
            route = "permissions?fromSettings={fromSettings}",
            arguments = listOf(navArgument("fromSettings") { type = NavType.BoolType; defaultValue = false }),
        ) { entry ->
            val fromSettings = entry.arguments?.getBoolean("fromSettings") == true
            PermissionsScreen(
                allowManageGranted = fromSettings,
                onFinished = {
                    if (fromSettings) {
                        navController.popBackStack()
                    } else {
                        onOnboardingFinished()
                        navController.navigate("main", clearBackStack)
                    }
                },
            )
        }
        composable("main") {
            MainScreen(
                onFixProtection = { navController.navigate("permissions?fromSettings=true") },
                onOpenAlert = { navController.navigate("alert/$it") },
                onOpenCheck = { navController.navigate("check") },
                onOpenEmergency = { navController.navigate("emergency") },
                onOpenCheckup = { navController.navigate("checkup") },
                onOpenArticle = { navController.navigate("article/$it") },
                onOpenSimulation = { navController.navigate("simulation") },
                onOpenQuiz = { navController.navigate("quiz") },
            )
        }
        composable("quiz") {
            AgilityQuizScreen(onBack = { navController.popBackStack() })
        }
        composable("simulation") {
            NotificationSimulationScreen(
                onBack = { navController.popBackStack() },
                onOpenAlert = { id -> navController.navigate("alert/$id") },
            )
        }
        composable("check") {
            CheckScreen(
                shared = launchRequest,
                onSharedHandled = onLaunchRequestHandled,
                onBack = { navController.popBackStack() },
                onResult = { id ->
                    navController.navigate("alert/$id") { popUpTo("check") { inclusive = true } }
                },
                onOpenLookup = { navController.navigate("lookup") },
            )
        }
        composable("lookup") {
            EntityLookupScreen(onBack = { navController.popBackStack() })
        }
        composable("alert/{id}") { entry ->
            val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
            AlertDetailScreen(
                id = id,
                onBack = { navController.popBackStack() },
                onOpenEmergency = { cat -> navController.navigate("emergency?cat=$cat") },
            )
        }
        composable("emergency?cat={cat}") { entry ->
            val cat = entry.arguments?.getString("cat")
            EmergencyScreen(
                categoryName = cat,
                onBack = { navController.popBackStack() },
                onOpenDraft = { navController.navigate("report_draft") },
            )
        }
        composable("emergency") {
            EmergencyScreen(
                categoryName = null,
                onBack = { navController.popBackStack() },
                onOpenDraft = { navController.navigate("report_draft") },
            )
        }
        composable("report_draft") {
            ReportDraftScreen(onBack = { navController.popBackStack() })
        }
        composable("checkup") {
            CheckupScreen(onBack = { navController.popBackStack() })
        }
        composable("article/{id}") { entry ->
            val articleId = entry.arguments?.getString("id").orEmpty()
            ArticleDetailScreen(articleId = articleId, onBack = { navController.popBackStack() })
        }
    }
}
