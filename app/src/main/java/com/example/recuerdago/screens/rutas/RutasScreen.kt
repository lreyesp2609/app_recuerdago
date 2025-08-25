package com.example.recuerdago.screens.rutas

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import com.example.recuerdago.data.models.UbicacionUsuarioCreate
import com.example.recuerdago.screens.MapView
import com.example.recuerdago.screens.rutas.cards.UbicacionCard
import com.example.recuerdago.screens.rutas.views.UbicacionesViewModel

@Composable
fun RutasScreen(
    token: String, // Token de autenticación
    isDarkTheme: Boolean = false,
    primaryColor: Color = Color(0xFF1976D2),
    textColor: Color = Color.Black
) {
    val viewModel = remember { UbicacionesViewModel(token) }

    var showNewRouteDialog by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var selectedLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var searchLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var locationCustomName by remember { mutableStateOf("") }
    var selectedAddress by remember { mutableStateOf<String?>(null) }

    val isLoading by remember { derivedStateOf { viewModel.isLoading } }
    val errorMessage by remember { derivedStateOf { viewModel.errorMessage } }
    val ubicaciones by remember { derivedStateOf { viewModel.ubicaciones } }

    // Cargar ubicaciones al iniciar la pantalla
    LaunchedEffect(Unit) {
        viewModel.cargarUbicaciones()
    }

    // Colores para el tema
    val backgroundColor = if (isDarkTheme) Color(0xFF1A1A2E) else Color(0xFFF8F9FA)
    val cardBackgroundColor = if (isDarkTheme) Color(0xFF2D2D44) else Color.White
    val secondaryTextColor = if (isDarkTheme) Color(0xFFB0BEC5) else Color(0xFF616161)
    val accentColor = Color(0xFFFF6B6B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDarkTheme) {
                        listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
                    } else {
                        listOf(Color(0xFFF8F9FA), Color(0xFFE3F2FD))
                    }
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header con botón de nueva ubicación
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(primaryColor.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Mis Ubicaciones",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = "${ubicaciones.size} ubicaciones guardadas",
                                fontSize = 14.sp,
                                color = secondaryTextColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showNewRouteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Agregar Nueva Ubicación",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Lista de ubicaciones
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = primaryColor,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Cargando ubicaciones...",
                                color = secondaryTextColor,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                errorMessage != null -> {
                    val currentError = errorMessage // Variable local para evitar smart cast issues
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = currentError ?: "Error desconocido",
                                color = Color(0xFFD32F2F),
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                ubicaciones.isEmpty() -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOff,
                                contentDescription = null,
                                tint = secondaryTextColor,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No tienes ubicaciones guardadas",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Agrega tu primera ubicación para comenzar a usar RecuerdaGo",
                                fontSize = 14.sp,
                                color = secondaryTextColor,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(ubicaciones) { ubicacion ->
                            UbicacionCard(
                                ubicacion = ubicacion,
                                primaryColor = primaryColor,
                                textColor = textColor,
                                secondaryTextColor = secondaryTextColor,
                                cardBackgroundColor = cardBackgroundColor,
                                accentColor = accentColor,
                                isDarkTheme = isDarkTheme
                            )
                        }

                        // Spacer al final para evitar que el último elemento se corte
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }

        // Dialog para agregar nueva ubicación (sin cambios)
        if (showNewRouteDialog) {
            Dialog(
                onDismissRequest = {
                    showNewRouteDialog = false
                    locationCustomName = ""
                    selectedAddress = null
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
                        // Mapa de fondo
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

                        // Tarjeta de información
                        LocationInfoCard(
                            userLocation = userLocation,
                            selectedLocation = selectedLocation,
                            locationCustomName = locationCustomName,
                            onCustomNameChange = { locationCustomName = it },
                            onLocationSearch = { lat, lng ->
                                searchLocation = lat to lng
                                Handler(Looper.getMainLooper()).postDelayed({
                                    searchLocation = null
                                }, 1000)
                            },
                            onAddressChange = { address ->
                                selectedAddress = address
                            },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(16.dp)
                        )

                        // Botón Confirmar
                        FloatingActionButton(
                            onClick = {
                                if (locationCustomName.isNotBlank()) {
                                    selectedLocation?.let { (lat, lng) ->
                                        val nuevaUbicacion = UbicacionUsuarioCreate(
                                            nombre = locationCustomName,
                                            latitud = lat,
                                            longitud = lng,
                                            direccion_completa = selectedAddress ?: "",
                                        )
                                        viewModel.crearUbicacion(nuevaUbicacion) {
                                            showNewRouteDialog = false
                                            locationCustomName = ""
                                            selectedAddress = null
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            containerColor = if (locationCustomName.isNotBlank()) primaryColor else Color.Gray
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Confirmar ubicación",
                                    tint = Color.White
                                )
                            }
                        }

                        // Botón cerrar dialog
                        FloatingActionButton(
                            onClick = {
                                showNewRouteDialog = false
                                locationCustomName = ""
                                selectedAddress = null
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

                        // Mostrar error si ocurre
                        errorMessage?.let { error ->
                            Card(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFEBEE)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = error,
                                    color = Color(0xFFD32F2F),
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}