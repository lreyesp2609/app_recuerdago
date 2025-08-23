package com.example.recuerdago.screens.rutas

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.recuerdago.screens.GetCurrentLocation
import com.example.recuerdago.screens.MiniMap
import kotlinx.coroutines.delay

@Composable
fun RutasScreen(
    userId: String,
    isDarkTheme: Boolean = false,
    primaryColor: Color = Color(0xFF1976D2),
    textColor: Color = Color.Black,
    cardColors: CardColors = CardDefaults.cardColors(containerColor = Color.White)
) {
    val context = LocalContext.current

    var rutas by remember { mutableStateOf(listOf("Ruta 1", "Ruta 2")) }
    var showContent by remember { mutableStateOf(false) }
    var showNewRouteDialog by remember { mutableStateOf(false) }
    var newRouteName by remember { mutableStateOf("") }

    var hasLocationPermission by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var locationStatus by remember { mutableStateOf("Esperando...") }

    // Estados para controlar el diálogo de GPS
    var showGpsDialog by remember { mutableStateOf(false) }
    var gpsError by remember { mutableStateOf("") }

    val sampleLocations = listOf(
        LocationItem("Casa", "Av. Principal 123, Quevedo", "85%"),
        LocationItem("Universidad", "Campus UTQ, Quevedo", "92%"),
        LocationItem("Trabajo", "Calle Secundaria 45, Quevedo", "70%")
    )

    // Función para verificar si el GPS está activado
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    // Función para abrir la configuración de ubicación
    fun openLocationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }

    LaunchedEffect(Unit) { delay(300); showContent = true }

    // Añade esta verificación al inicio del diálogo
    LaunchedEffect(showNewRouteDialog) {
        if (showNewRouteDialog) {
            hasLocationPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    if (showNewRouteDialog) {
        GetCurrentLocation(
            onLocationResult = { lat, lng ->
                userLocation = Pair(lat, lng)
                locationStatus = "Ubicación obtenida: $lat, $lng"
                showGpsDialog = false
            },
            onError = { error ->
                locationStatus = error
                if (error.contains("GPS", ignoreCase = true) || error.contains("activar", ignoreCase = true)) {
                    showGpsDialog = true
                    gpsError = error
                }
            },
            onPermissionGranted = {
                hasLocationPermission = true // Actualizar el estado de permisos
            }
        )
    }

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
            SmartRoutesSection(
                isDarkTheme = isDarkTheme, primaryColor = primaryColor, textColor = textColor
            )

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

            FrequentLocationsSection(
                locations = sampleLocations, isDarkTheme = isDarkTheme, textColor = textColor
            )
        }

        // Modal para agregar nueva ruta
        if (showNewRouteDialog) {
            Dialog(onDismissRequest = {
                showNewRouteDialog = false
                userLocation = null
                locationStatus = "Esperando..."
            }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = cardColors,
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Agregar Nueva Ubicación", fontSize = 18.sp, color = primaryColor)

                        OutlinedTextField(
                            value = newRouteName,
                            onValueChange = { newRouteName = it },
                            label = {
                                Text(
                                    "Nombre de la ubicación (ej: Casa, Trabajo)",
                                    color = textColor.copy(alpha = 0.7f)
                                )
                            },
                            textStyle = LocalTextStyle.current.copy(color = textColor),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = {
                                Text(
                                    "Ingresa un nombre...", color = textColor.copy(alpha = 0.5f)
                                )
                            })

                        // En el diálogo, modifica la sección de estado de ubicación:
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFF5F5F5)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Estado de ubicación:",
                                    fontSize = 14.sp,
                                    color = primaryColor,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = locationStatus,
                                    fontSize = 12.sp,
                                    color = textColor.copy(alpha = 0.8f)
                                )

                                if (!hasLocationPermission) {
                                    Button(
                                        onClick = {
                                            // Abrir configuración de la app para otorgar permisos
                                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.fromParts("package", context.packageName, null)
                                            }
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B)),
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Text(
                                            "Otorgar permisos en configuración",
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        userLocation?.let { (lat, lng) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                            ) {
                                MiniMap(
                                    modifier = Modifier.fillMaxSize(),
                                    zoom = 17.0
                                ) { mapView ->
                                    mapView.controller.setCenter(
                                        org.osmdroid.util.GeoPoint(lat, lng)
                                    )
                                }
                            }
                        } ?: run {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFF3E0)
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (hasLocationPermission) {
                                        CircularProgressIndicator(
                                            color = primaryColor,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            "Sin permisos de ubicación",
                                            fontSize = 14.sp,
                                            color = Color(0xFFFF8F00)
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                showNewRouteDialog = false
                                userLocation = null
                                locationStatus = "Esperando..."
                            }) {
                                Text("Cancelar", color = textColor)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newRouteName.isNotBlank()) {
                                        rutas = rutas + newRouteName
                                        newRouteName = ""
                                        userLocation = null
                                        locationStatus = "Esperando..."
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

        // Diálogo para activar GPS
        if (showGpsDialog) {
            AlertDialog(
                onDismissRequest = { showGpsDialog = false },
                title = {
                    Text(text = "GPS inactivo", color = textColor)
                },
                text = {
                    Column {
                        Text(
                            "Para mejorar la experiencia, el dispositivo necesita la precisión de la ubicación.",
                            color = textColor.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            gpsError,
                            color = Color(0xFFFF6B6B),
                            fontSize = 14.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { openLocationSettings(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Habilitar GPS")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showGpsDialog = false }
                    ) {
                        Text("Cancelar", color = textColor)
                    }
                }
            )
        }
    }
}