package com.example.recuerdago.screens.rutas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SmartRoutesSection(
    isDarkTheme: Boolean = false,
    primaryColor: Color = Color(0xFF1976D2),
    textColor: Color = Color.Black
) {
    val cardBackgroundColor = if (isDarkTheme) Color(0xFF2D2D44) else Color.White
    val cardTextColor = if (isDarkTheme) Color.White else Color.Black

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icono ML con flecha de ubicación
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        Color(0xFFFF6B6B),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Ubicación ML",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "ML",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Título y subtítulo
            Text(
                text = "Rutas Inteligentes",
                fontSize = 22.sp,
                color = primaryColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Encuentra el mejor camino con IA",
                fontSize = 14.sp,
                color = cardTextColor.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Leyendas informativas (como botones sin clic)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoCard(
                    icon = Icons.Default.Route,
                    number = "12",
                    label = "Rutas\nGuardadas",
                    iconColor = Color(0xFF4CAF50),
                    isDarkTheme = isDarkTheme
                )
                InfoCard(
                    icon = Icons.Default.Schedule,
                    number = "45min",
                    label = "Tiempo\nAhorrado",
                    iconColor = Color(0xFFFF5722),
                    isDarkTheme = isDarkTheme
                )
                InfoCard(
                    icon = Icons.Default.Eco,
                    number = "2.3kg",
                    label = "CO₂\nReducido",
                    iconColor = Color(0xFF4CAF50),
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

@Composable
fun InfoCard(
    icon: ImageVector,
    number: String,
    label: String,
    iconColor: Color,
    isDarkTheme: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = number,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) Color.White else Color.Black
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isDarkTheme) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}