package com.example.recuerdago.screens.rutas


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

@Composable
fun RutasScreen(
    userId: String,
    isDarkTheme: Boolean = false,
    primaryColor: Color = Color(0xFF1976D2),
    textColor: Color = Color.Black,
    cardColors: CardColors = CardDefaults.cardColors(containerColor = Color.White)
) {
    var rutas by remember { mutableStateOf(listOf("Ruta 1", "Ruta 2")) }
    var showContent by remember { mutableStateOf(false) }
    var showNewRouteDialog by remember { mutableStateOf(false) }
    var newRouteName by remember { mutableStateOf("") }

    val sampleLocations = listOf(
        LocationItem("Casa", "Av. Principal 123, Quevedo", "85%"),
        LocationItem("Universidad", "Campus UTQ, Quevedo", "92%"),
        LocationItem("Trabajo", "Calle Secundaria 45, Quevedo", "70%")
    )

    LaunchedEffect(Unit) { delay(300); showContent = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- SECCIÓN PRINCIPAL: RUTAS INTELIGENTES ---
            SmartRoutesSection(
                isDarkTheme = isDarkTheme,
                primaryColor = primaryColor,
                textColor = textColor
            )

            // --- BOTONES DE ACCIÓN ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { showNewRouteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6)),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nueva Ubicación", color = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = { /* Acción para analizar con IA */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B)),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "IA",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analizar con IA", color = Color.White)
                }
            }

            // --- SECCIÓN: MIS UBICACIONES ---
            FrequentLocationsSection(
                locations = sampleLocations,
                isDarkTheme = isDarkTheme,
                textColor = textColor
            )
        }

        // Modal para agregar nueva ruta
        // Dentro de RutasScreen
        if (showNewRouteDialog) {
            Dialog(onDismissRequest = { showNewRouteDialog = false }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = cardColors, // ✅ Usa los colores que ya pasas a RutasScreen
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Agregar Nueva Ruta",
                            fontSize = 18.sp,
                            color = primaryColor // ✅ Usa tu color primario
                        )

                        OutlinedTextField(
                            value = newRouteName,
                            onValueChange = { newRouteName = it },
                            label = {
                                Text(
                                    "Nombre de la ruta",
                                    color = if (isDarkTheme) Color.LightGray else Color.DarkGray
                                )
                            },
                            textStyle = LocalTextStyle.current.copy(color = textColor), // ✅ Texto adaptable
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showNewRouteDialog = false }) {
                                Text("Cancelar", color = textColor)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newRouteName.isNotBlank()) {
                                        rutas = rutas + newRouteName
                                        newRouteName = ""
                                        showNewRouteDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                            ) {
                                Text("Agregar", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

    }
}