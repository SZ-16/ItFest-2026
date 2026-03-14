package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class ReportType(val label: String) {
    BLOCKED_PATH("Cale blocată"),
    OTHER("Altele")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportForm(onDismiss: () -> Unit) {
    var selectedType by remember { mutableStateOf<ReportType?>(null) }
    var description by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = "Raportează un obstacol",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0D1B2A)
        )

        Text(
            text = "Ajută comunitatea marcând zonele inaccesibile.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        HorizontalDivider()

        // Report type selector
        Text(
            text = "Tip problemă",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF0D1B2A)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReportType.entries.forEach { type ->
                val isSelected = selectedType == type
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedType = type },
                    label = { Text(type.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0052CC),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Description field
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descriere (opțional)") },
            placeholder = { Text("Ex: Trotuar blocat de mașină parcată...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            shape = RoundedCornerShape(12.dp)
        )

        // Submit button
        Button(
            onClick = {
                // TODO: send report to your backend
                submitted = true
            },
            enabled = selectedType != null && !submitted,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC))
        ) {
            Text(
                if (submitted) "✓ Trimis!" else "Trimite raportul",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }

        // Auto-dismiss after submit
        if (submitted) {
            LaunchedEffect(Unit) {
                delay(1200)
                onDismiss()
            }
        }
    }
}
