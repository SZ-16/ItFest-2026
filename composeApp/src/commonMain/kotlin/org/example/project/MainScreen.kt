package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class BottomTab { HOME, MAP, PROFILE }

@Composable
fun MainScreen(onLogout: () -> Unit) {
    var currentTab by remember { mutableStateOf(BottomTab.HOME) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == BottomTab.HOME,
                    onClick = { currentTab = BottomTab.HOME },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0052CC),
                        selectedTextColor = Color(0xFF0052CC),
                        indicatorColor = Color(0xFFE8F0FE)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == BottomTab.MAP,
                    onClick = { currentTab = BottomTab.MAP },
                    icon = { Icon(Icons.Filled.Map, contentDescription = "Map") },
                    label = { Text("Map") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0052CC),
                        selectedTextColor = Color(0xFF0052CC),
                        indicatorColor = Color(0xFFE8F0FE)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == BottomTab.PROFILE,
                    onClick = { currentTab = BottomTab.PROFILE },
                    icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0052CC),
                        selectedTextColor = Color(0xFF0052CC),
                        indicatorColor = Color(0xFFE8F0FE)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentTab) {
                BottomTab.HOME    -> HomeScreen()
                BottomTab.MAP     -> MapScreen()
                BottomTab.PROFILE -> ProfileScreen(onLogout = onLogout)
            }
        }
    }
}