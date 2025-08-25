package com.example.recuerdago.screens.rutas.cards

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun UbicacionCard(
    ubicacion: Any, // Tu modelo de ubicación
    primaryColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    cardBackgroundColor: Color,
    accentColor: Color,
    isDarkTheme: Boolean
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = ""
    )

    // Extraer datos usando reflection (ajusta según tu modelo)
    val nombre = try {
        ubicacion::class.java.getDeclaredField("nombre").apply { isAccessible = true }.get(ubicacion) as? String ?: "Sin nombre"
    } catch (e: Exception) { "Sin nombre" }

    val latitud = try {
        ubicacion::class.java.getDeclaredField("latitud").apply { isAccessible = true }.get(ubicacion) as? Double ?: 0.0
    } catch (e: Exception) { 0.0 }

    val longitud = try {
        ubicacion::class.java.getDeclaredField("longitud").apply { isAccessible = true }.get(ubicacion) as? Double ?: 0.0
    } catch (e: Exception) { 0.0 }

    val direccionCompleta = try {
        ubicacion::class.java.getDeclaredField("direccion_completa").apply { isAccessible = true }.get(ubicacion) as? String ?: ""
    } catch (e: Exception) { "" }

    val activo = try {
        ubicacion::class.java.getDeclaredField("activo").apply { isAccessible = true }.get(ubicacion) as? Boolean ?: true
    } catch (e: Exception) { true }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                isPressed = true
                // Aquí puedes agregar acciones como ver en mapa, editar, etc.
            }
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header de la tarjeta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                primaryColor.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = nombre,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (activo) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (activo) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (activo) "Activa" else "Inactiva",
                                fontSize = 12.sp,
                                color = if (activo) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Menú de opciones
                IconButton(
                    onClick = { /* Mostrar menú de opciones */ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = secondaryTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dirección
            if (direccionCompleta.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = direccionCompleta,
                        fontSize = 14.sp,
                        color = secondaryTextColor,
                        lineHeight = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Coordenadas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CoordinateChip(
                    label = "Lat",
                    value = String.format("%.6f", latitud),
                    primaryColor = primaryColor
                )

                CoordinateChip(
                    label = "Lng",
                    value = String.format("%.6f", longitud),
                    primaryColor = primaryColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Ver en mapa */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = primaryColor
                    ),
                    border = BorderStroke(1.dp, primaryColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver", fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = { /* Editar */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = accentColor
                    ),
                    border = BorderStroke(1.dp, accentColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar", fontSize = 14.sp)
                }
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

@Composable
fun CoordinateChip(
    label: String,
    value: String,
    primaryColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$label:",
            fontSize = 12.sp,
            color = primaryColor,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = primaryColor.copy(alpha = 0.8f),
            fontFamily = FontFamily.Monospace
        )
    }
}