package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val profile = CurrentUser.profile
    var showChangePassword by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var passwordMessage by remember { mutableStateOf("") }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(Color(0xFF0052CC), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                CurrentUser.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Text(CurrentUser.username, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
        Text(CurrentUser.email, fontSize = 14.sp, color = Color.Gray)

        // Disability badges
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE)),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("My Accessibility Needs", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0052CC))
                if (profile.wheelchair)         BadgeRow("🦽", "Wheelchair / Mobility")
                if (profile.hearingSensitivity)  BadgeRow("🔇", "Hearing Sensitivity")
                if (profile.autismSensory)       BadgeRow("🧠", "Autism / Sensory Overload")
                if (profile.chronicPain)         BadgeRow("💢", "Chronic Pain / Fatigue")
                if (profile.visualImpairment)    BadgeRow("👁️", "Visual Impairment")
                if (!profile.wheelchair && !profile.hearingSensitivity &&
                    !profile.autismSensory && !profile.chronicPain && !profile.visualImpairment
                ) {
                    Text("No needs selected.", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }

        // Change password
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Account Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    TextButton(onClick = { showChangePassword = !showChangePassword }) {
                        Text(if (showChangePassword) "Cancel" else "Change Password", color = Color(0xFF0052CC))
                    }
                }

                if (showChangePassword) {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Current Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    if (passwordMessage.isNotEmpty()) {
                        Text(
                            passwordMessage,
                            color = if (passwordMessage.startsWith("✓")) Color(0xFF2E7D32) else Color.Red,
                            fontSize = 13.sp
                        )
                    }
                    Button(
                        onClick = {
                            when {
                                oldPassword.isBlank() || newPassword.isBlank() ->
                                    passwordMessage = "Please fill in both fields"
                                newPassword.length < 6 ->
                                    passwordMessage = "New password must be at least 6 characters"
                                else -> {
                                    // TODO: connect to backend /api/change-password
                                    passwordMessage = "✓ Password updated successfully"
                                    oldPassword = ""
                                    newPassword = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC))
                    ) {
                        Text("Update Password", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Logout
        if (showLogoutConfirm) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Are you sure you want to logout?", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { showLogoutConfirm = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Cancel") }
                        Button(
                            onClick = {
                                CurrentUser.userId = -1
                                CurrentUser.username = ""
                                CurrentUser.email = ""
                                CurrentUser.profile = DisabilityProfile()
                                onLogout()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC0000))
                        ) { Text("Logout") }
                    }
                }
            }
        } else {
            OutlinedButton(
                onClick = { showLogoutConfirm = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFCC0000))
            ) {
                Text("Logout", color = Color(0xFFCC0000), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun BadgeRow(emoji: String, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(emoji, fontSize = 22.sp)
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A1A1A))
        Spacer(modifier = Modifier.weight(1f))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0052CC)
        ) {
            Text("Active", modifier = androidx.compose.ui.Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}