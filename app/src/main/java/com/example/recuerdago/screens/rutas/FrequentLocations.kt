package com.example.recuerdago.screens.rutas

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LocationItem(
    val name: String,
    val address: String,
    val frequency: String
)

@Composable
fun FrequentLocationsSection(
    locations: List<LocationItem>,
    isDarkTheme: Boolean = false,
    textColor: Color = Color.Black
) {
    val sectionTextColor = if (isDarkTheme) Color.White else textColor

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Mis Ubicaciones",
            fontSize = 22.sp,
            color = sectionTextColor,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Lugares que visitas frecuentemente",
            fontSize = 14.sp,
            color = sectionTextColor.copy(alpha = 0.8f)
        )

        // Usar Column en lugar de LazyColumn
        locations.forEach { location ->
            LocationCard(
                location = location,
                isDarkTheme = isDarkTheme
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun LocationCard(
    location: LocationItem,
    isDarkTheme: Boolean = false
) {
    // Determinar color del borde basado en la frecuencia
    val frequencyPercentage = location.frequency.replace("%", "").toIntOrNull() ?: 0
    val borderColor = when {
        frequencyPercentage >= 85 -> Color(0xFFFF5722) // Rojito para alta frecuencia
        frequencyPercentage >= 70 -> Color(0xFFFF9800) // Naranja para frecuencia media
        else -> Color(0xFF4CAF50) // Verde para frecuencia baja
    }

    val backgroundColor = if (isDarkTheme) Color(0xFF2D2D44) else Color.White
    val cardTextColor = if (isDarkTheme) Color.White else Color.Black

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icono según el tipo de ubicación
                val icon = when {
                    location.name.contains("Casa", ignoreCase = true) -> Icons.Default.Home
                    location.name.contains("Universidad", ignoreCase = true) ||
                            location.name.contains("Escuela", ignoreCase = true) -> Icons.Default.School
                    location.name.contains("Trabajo", ignoreCase = true) ||
                            location.name.contains("Oficina", ignoreCase = true) -> Icons.Default.Work
                    else -> Icons.Default.LocationOn
                }

                Icon(
                    imageVector = icon,
                    contentDescription = location.name,
                    tint = cardTextColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = location.name,
                        fontSize = 16.sp,
                        color = cardTextColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = location.address,
                        fontSize = 14.sp,
                        color = cardTextColor.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${location.frequency} frecuencia",
                        fontSize = 12.sp,
                        color = borderColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Icono de navegación
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "Navegar",
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(20.dp)
                )

                // Indicador de tiempo (ejemplo)
                Text(
                    text = when (location.name) {
                        "Casa" -> "Hace 2 horas"
                        "Universidad" -> "Ayer"
                        else -> "Hace 1 día"
                    },
                    fontSize = 11.sp,
                    color = cardTextColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}