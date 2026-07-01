package com.cardstack.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cardstack.app.data.repository.SettingsRepository
import com.cardstack.app.ui.biometric.LockScreen
import com.cardstack.app.ui.biometric.showBiometricPrompt
import com.cardstack.app.ui.navigation.CardStackBottomBar
import com.cardstack.app.ui.navigation.CardStackNavHost
import com.cardstack.app.ui.navigation.bottomNavRoutes
import com.cardstack.app.ui.theme.CardStackTheme
import com.cardstack.app.ui.theme.ThemeChoice
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var settings: SettingsRepository

    private var backgroundedAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        setContent { CardStackApp() }
    }

    @Composable
    private fun CardStackApp() {
        val themeName by settings.themeChoiceFlow.collectAsStateWithLifecycle()
        val themeChoice = ThemeChoice.valueOf(themeName)
        CardStackTheme(themeChoice = themeChoice) {
            val biometricEnabled = settings.getBiometricEnabled()
            val lockTimeoutMillis = settings.getLockTimeoutMinutes() * 60_000L

            var isUnlocked by remember { mutableStateOf(!biometricEnabled) }
            var authError by remember { mutableStateOf<String?>(null) }

            DisposableEffect(Unit) {
                val observer = object : DefaultLifecycleObserver {
                    override fun onStop(owner: LifecycleOwner) {
                        backgroundedAt = System.currentTimeMillis()
                    }
                    override fun onStart(owner: LifecycleOwner) {
                        if (!settings.getBiometricEnabled()) return
                        val elapsed = System.currentTimeMillis() - backgroundedAt
                        if (backgroundedAt > 0 && elapsed > lockTimeoutMillis) {
                            isUnlocked = false
                        }
                    }
                }
                lifecycle.addObserver(observer)
                onDispose { lifecycle.removeObserver(observer) }
            }

            if (!isUnlocked) {
                LockScreen(
                    errorMessage = authError,
                    onRetry = {
                        authError = null
                        triggerBiometric(
                            onSuccess = { isUnlocked = true; authError = null },
                            onError = { msg -> authError = msg }
                        )
                    }
                )
                LaunchedEffect(Unit) {
                    triggerBiometric(
                        onSuccess = { isUnlocked = true; authError = null },
                        onError = { msg -> authError = msg }
                    )
                }
            } else {
                MainContent()
            }
        }
    }

    @Composable
    private fun MainContent() {
        val navController = rememberNavController()
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

        Scaffold(
            bottomBar = {
                if (currentRoute in bottomNavRoutes) CardStackBottomBar(navController)
            }
        ) { innerPadding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) {
                CardStackNavHost(navController = navController)
            }
        }
    }

    private fun triggerBiometric(onSuccess: () -> Unit, onError: (String) -> Unit) {
        showBiometricPrompt(activity = this, onSuccess = onSuccess, onError = onError)
    }
}
