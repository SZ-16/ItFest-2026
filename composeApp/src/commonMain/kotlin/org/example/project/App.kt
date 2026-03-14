package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import kotlinprojectitfest1.composeapp.generated.resources.Res
import kotlinprojectitfest1.composeapp.generated.resources.map_placeholder

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
                onContinue = { currentScreen = Screen.MAP }
            )
            Screen.MAP -> MapScreen()
        }
    }
}

@Composable
fun MapScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.map_placeholder),
            contentDescription = "Map Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp).statusBarsPadding(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp).fillMaxWidth()) {
                Text("Căutare locații...", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            }
        }
        Button(
            onClick = { },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).navigationBarsPadding(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC))
        ) {
            Text("RAPORTEAZĂ", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.titleMedium)
        }
    }
}