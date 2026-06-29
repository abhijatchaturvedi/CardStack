package com.cardstack.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cardstack.app.ui.analytics.AnalyticsScreen
import com.cardstack.app.ui.card.AddEditCardScreen
import com.cardstack.app.ui.card.CardDetailScreen
import com.cardstack.app.ui.home.HomeScreen
import com.cardstack.app.ui.rewards.RewardsScreen
import com.cardstack.app.ui.settings.SettingsScreen
import com.cardstack.app.ui.transactions.TransactionsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Cards : Screen("cards")
    data object AddCard : Screen("add_card")
    data object EditCard : Screen("edit_card/{cardId}") {
        fun withId(id: Long) = "edit_card/$id"
    }
    data object CardDetail : Screen("card_detail/{cardId}") {
        fun withId(id: Long) = "card_detail/$id"
    }
    data object Transactions : Screen("transactions?cardId={cardId}") {
        fun forCard(id: Long?) = if (id != null) "transactions?cardId=$id" else "transactions?cardId=-1"
    }
    data object Analytics : Screen("analytics")
    data object Rewards : Screen("rewards")
    data object Settings : Screen("settings")
}

@Composable
fun CardStackNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onCardClick = { id -> navController.navigate(Screen.CardDetail.withId(id)) },
                onAddCard = { navController.navigate(Screen.AddCard.route) }
            )
        }
        composable(Screen.AddCard.route) {
            AddEditCardScreen(
                cardId = null,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.EditCard.route,
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) { back ->
            AddEditCardScreen(
                cardId = back.arguments?.getLong("cardId"),
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.CardDetail.route,
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) { back ->
            val cardId = back.arguments!!.getLong("cardId")
            CardDetailScreen(
                cardId = cardId,
                onEdit = { navController.navigate(Screen.EditCard.withId(cardId)) },
                onViewTransactions = { navController.navigate(Screen.Transactions.forCard(cardId)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Transactions.route,
            arguments = listOf(navArgument("cardId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { back ->
            val cardId = back.arguments?.getLong("cardId")?.takeIf { it != -1L }
            TransactionsScreen(
                cardId = cardId,
                onBack = { navController.popBackStack() }
            )
        }
        // Cards tab re-uses Home for now; a dedicated card list can be added later
        composable(Screen.Cards.route) {
            HomeScreen(
                onCardClick = { id -> navController.navigate(Screen.CardDetail.withId(id)) },
                onAddCard = { navController.navigate(Screen.AddCard.route) }
            )
        }
        composable(Screen.Analytics.route) { AnalyticsScreen() }
        composable(Screen.Rewards.route) { RewardsScreen() }
        composable(Screen.Settings.route) { SettingsScreen() }
    }
}
