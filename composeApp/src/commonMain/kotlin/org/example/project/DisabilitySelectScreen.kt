package org.example.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DisabilityOption(
    val emoji: String,
    val title: String,
    val description: String
)

val disabilityOptions = listOf(
    DisabilityOption("🦽", "Wheelchair / Mobility", "Avoid stairs, steep ramps & inaccessible paths"),
    DisabilityOption("🔇", "Hearing Sensitivity", "Avoid loud areas, construction & crowded zones"),
    DisabilityOption("🧠", "Autism / Sensory Overload", "Avoid overwhelming lights, sounds & crowds"),
    DisabilityOption("💢", "Chronic Pain / Fatigue", "Find rest spots & avoid long-distance routes"),
    DisabilityOption("👁️", "Visual Impairment", "Highlight safe crossings & audio-friendly paths")
)

@Composable
fun DisabilitySelectScreen(onContinue: (DisabilityProfile) -> Unit) {
    val selected = remember { mutableStateListOf<Int>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "What are your needs?",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0052CC),
            textAlign = TextAlign.Center
        )

        Text(
            "Select all that apply.\nThis helps us tailor your map.",
            fontSize = 15.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        disabilityOptions.forEachIndexed { index, option ->
            val isSelected = selected.contains(index)
            Card(
                onClick = {
                    if (isSelected) selected.remove(index)
                    else selected.add(index)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Color(0xFF0052CC) else Color(0xFFDDDDDD)
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFFE8F0FE) else Color.White
                ),
                elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(option.emoji, fontSize = 36.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            option.title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = if (isSelected) Color(0xFF0052CC) else Color(0xFF1A1A1A)
                        )
                        Text(
                            option.description,
                            fontSize = 13.sp,
                            color = Color.Gray,
                            lineHeight = 18.sp
                        )
                    }
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = {
                            if (isSelected) selected.remove(index)
                            else selected.add(index)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF0052CC)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val profile = DisabilityProfile(
                    wheelchair = selected.contains(0),
                    hearingSensitivity = selected.contains(1),
                    autismSensory = selected.contains(2),
                    chronicPain = selected.contains(3),
                    visualImpairment = selected.contains(4)
                )
                ProfileStore.profile = profile
                onContinue(profile)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selected.isEmpty()) Color(0xFFAAAAAA) else Color(0xFF0052CC)
            ),
            enabled = selected.isNotEmpty()
        ) {
            Text(
                if (selected.isEmpty()) "Select at least one" else "Continue →",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        TextButton(onClick = { onContinue(DisabilityProfile()) }) {
            Text("Skip for now", color = Color.Gray, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}