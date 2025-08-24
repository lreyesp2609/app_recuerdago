package com.example.recuerdago.screens.rutas

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import com.example.recuerdago.screens.MapView

@Composable
fun RutasScreen(
    userId: String,
    isDarkTheme: Boolean = false,
    primaryColor: Color = Color(0xFF1976D2),
    textColor: Color = Color.Black
) {
    var showNewRouteDialog by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var selectedLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var searchLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    // Estado para el nombre personalizado
    var locationCustomName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Button(
            onClick = { showNewRouteDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6)),
            shape = RoundedCornerShape(25.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Nueva Ubicación", color = Color.White)
        }

        if (showNewRouteDialog) {
            Dialog(
                onDismissRequest = {
                    showNewRouteDialog = false
                    locationCustomName = "" // Limpiar el nombre al cerrar
                },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    color = Color.White
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Mapa en el fondo
                        MapView(
                            modifier = Modifier.fillMaxSize(),
                            onLocationSelected = { lat, lng ->
                                userLocation = lat to lng
                            },
                            onMapCenterChanged = { lat, lng ->
                                selectedLocation = lat to lng
                            },
                            moveToLocation = searchLocation
                        )

                        // Tarjeta de información en la parte superior
                        LocationInfoCard(
                            userLocation = userLocation,
                            selectedLocation = selectedLocation,
                            locationCustomName = locationCustomName,
                            onCustomNameChange = { locationCustomName = it },
                            onLocationSearch = { lat, lng ->
                                // Mover el mapa a la ubicación buscada
                                searchLocation = lat to lng
                                // Resetear después de un momento para permitir futuras búsquedas
                                Handler(Looper.getMainLooper()).postDelayed({
                                    searchLocation = null
                                }, 1000)
                            },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(16.dp)
                        )

                        // Botón de confirmar (DESHABILITADO si no hay nombre)
                        FloatingActionButton(
                            onClick = {
                                if (locationCustomName.isNotBlank()) {
                                    selectedLocation?.let { (lat, lng) ->
                                        println("Ubicación confirmada: $lat, $lng con nombre: $locationCustomName")
                                        showNewRouteDialog = false
                                        locationCustomName = "" // Limpiar después de confirmar
                                    }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            containerColor = if (locationCustomName.isNotBlank()) Color(0xFF64B5F6) else Color.Gray,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Confirmar ubicación",
                                tint = Color.White
                            )
                        }

                        FloatingActionButton(
                            onClick = {
                                showNewRouteDialog = false
                                locationCustomName = ""
                            },
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp),
                            containerColor = Color.Gray
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}