package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen() {
    val profile = CurrentUser.profile

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Hello, ${CurrentUser.username} 👋",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0052CC)
        )
        Text(
            "Here's your personalized accessibility overview.",
            fontSize = 15.sp,
            color = Color.Gray
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE)),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Your Active Needs", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0052CC))
                if (profile.wheelchair)        NeedRow("🦽", "Wheelchair / Mobility")
                if (profile.hearingSensitivity) NeedRow("🔇", "Hearing Sensitivity")
                if (profile.autismSensory)      NeedRow("🧠", "Autism / Sensory Overload")
                if (profile.chronicPain)        NeedRow("💢", "Chronic Pain / Fatigue")
                if (profile.visualImpairment)   NeedRow("👁️", "Visual Impairment")
                if (!profile.wheelchair && !profile.hearingSensitivity &&
                    !profile.autismSensory && !profile.chronicPain && !profile.visualImpairment
                ) {
                    Text("No needs selected. Go to Profile to update.", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("💡 How It Works", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF1A1A1A))
                Text("1. Go to Map to see hazards near you", fontSize = 14.sp, color = Color.Gray)
                Text("2. Tap Report to flag a new obstacle", fontSize = 14.sp, color = Color.Gray)
                Text("3. Community reports are reviewed by admins", fontSize = 14.sp, color = Color.Gray)
                Text("4. Approved hazards show on everyone's map", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun NeedRow(emoji: String, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(emoji, fontSize = 22.sp)
        Text(label, fontSize = 15.sp, color = Color(0xFF1A1A1A), fontWeight = FontWeight.Medium)
    }
}