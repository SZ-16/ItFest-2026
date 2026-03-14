package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }

    MaterialTheme {
        when (currentScreen) {
            Screen.LOGIN -> LoginScreen(
                onLoginSuccess = { currentScreen = Screen.WELCOME },
                onGoToRegister = { currentScreen = Screen.REGISTER },
                onGoToForgotPassword = { currentScreen = Screen.FORGOT_PASSWORD }
            )
            Screen.REGISTER -> RegisterScreen(
                onRegisterSuccess = { currentScreen = Screen.LOGIN },
                onGoToLogin = { currentScreen = Screen.LOGIN }
            )
            Screen.FORGOT_PASSWORD -> ForgotPasswordScreen(
                onGoToLogin = { currentScreen = Screen.LOGIN }
            )
            Screen.WELCOME -> WelcomeScreen(
                onGetStarted = { currentScreen = Screen.DISABILITY_SELECT }
            )
            Screen.DISABILITY_SELECT -> DisabilitySelectScreen(
                onContinue = { currentScreen = Screen.MAIN }
            )
            Screen.MAIN -> MainScreen(
                onLogout = { currentScreen = Screen.LOGIN }
            )
        }
    }
}

@Composable
fun MapScreen() {
    MapContent(modifier = Modifier.fillMaxSize())
}