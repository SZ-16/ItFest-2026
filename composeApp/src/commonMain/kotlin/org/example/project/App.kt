package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
expect fun MapContent(modifier: Modifier = Modifier)

@Composable
fun App() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {

            // 1. THE MAP CONTENT (Platform specific)
            MapContent(modifier = Modifier.fillMaxSize())

            // 2. FLOATING SEARCH BAR
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .statusBarsPadding(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        "Căutare locații...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }

            // 3. REPORT BUTTON
            Button(
                onClick = { /* TODO: Open report form */ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC))
            ) {
                Text(
                    "RAPORTEAZĂ",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
