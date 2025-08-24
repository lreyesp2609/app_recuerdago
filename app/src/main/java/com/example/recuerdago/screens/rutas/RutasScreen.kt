package com.example.recuerdago.screens.rutas

import kotlinx.coroutines.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.net.Uri
import android.Manifest
import android.app.Activity
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.recuerdago.network.NominatimClient
import com.example.recuerdago.screens.GetCurrentLocation
import com.example.recuerdago.screens.InteractiveMap
import org.osmdroid.util.GeoPoint
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.core.view.WindowInsetsControllerCompat
import com.example.recuerdago.SystemBarsUtils

@Composable
fun RutasScreen(
    userId: String,
    isDarkTheme: Boolean = false,
    primaryColor: Color = Color(0xFF1976D2),
    textColor: Color = Color.Black,
    cardColors: CardColors = CardDefaults.cardColors(containerColor = Color.White)
) {
    val context = LocalContext.current
    val view = LocalView.current

    // Controlar el estado de las system bars
    var systemBarsConfigured by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val activity = context as? Activity
        activity?.let {
            SystemBarsUtils.setTransparentSystemBars(it)
            systemBarsConfigured = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Restaurar las system bars cuando el composable se destruya
            val activity = context as? Activity
            activity?.let {
                SystemBarsUtils.setDefaultSystemBars(it)
            }
        }
    }

    var rutas by remember { mutableStateOf(listOf("Ruta 1", "Ruta 2")) }
    var showNewRouteDialog by remember { mutableStateOf(false) }
    var newRouteName by remember { mutableStateOf("") }

    var hasLocationPermission by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var locationStatus by remember { mutableStateOf("Esperando...") }
    var currentLocationAddress by remember { mutableStateOf("Obteniendo ubicación...") }

    var showGpsDialog by remember { mutableStateOf(false) }
    var gpsError by remember { mutableStateOf("") }

    var selectedLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var selectedLocationName by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    var isLoadingAddress by remember { mutableStateOf(false) }

    val addressCache = remember { mutableMapOf<String, String>() }
    var geocodingJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val debounceDelayMs = 800L

    val sampleLocations = listOf(
        LocationItem("Casa", "Av. Principal 123, Quevedo", "85%"),
        LocationItem("Universidad", "Campus UTQ, Quevedo", "92%"),
        LocationItem("Trabajo", "Calle Secundaria 45, Quevedo", "70%")
    )

    fun openLocationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }

    fun getAddressFromCoordinates(lat: Double, lng: Double, onResult: (String) -> Unit = {}) {
        val cacheKey = "${String.format("%.4f", lat)},${String.format("%.4f", lng)}"
        addressCache[cacheKey]?.let { cachedAddress ->
            selectedLocationName = cachedAddress
            onResult(cachedAddress)
            return
        }

        geocodingJob?.cancel()
        if (selectedLocationName.isEmpty() || selectedLocationName == "Obteniendo dirección...") {
            isLoadingAddress = true
            selectedLocationName = "Obteniendo dirección..."
        }

        geocodingJob = coroutineScope.launch {
            try {
                delay(debounceDelayMs)
                if (!isActive) return@launch
                val address = NominatimClient.apiService.reverseGeocode(lat = lat, lon = lng)
                val result = address.display_name ?: "Dirección no encontrada"
                addressCache[cacheKey] = result
                selectedLocationName = result
                onResult(result)
            } catch (e: Exception) {
                if (isActive) {
                    val errorMsg = if (e is CancellationException) "Actualizando..." else "Error al obtener dirección"
                    selectedLocationName = errorMsg
                    onResult(errorMsg)
                }
            } finally {
                if (isActive) { isLoadingAddress = false }
            }
        }
    }

    fun getAddressImmediately(lat: Double, lng: Double, onResult: (String) -> Unit = {}) {
        val cacheKey = "${String.format("%.4f", lat)},${String.format("%.4f", lng)}"
        addressCache[cacheKey]?.let { cachedAddress ->
            selectedLocationName = cachedAddress
            onResult(cachedAddress)
            return
        }

        geocodingJob?.cancel()
        isLoadingAddress = true

        geocodingJob = coroutineScope.launch {
            try {
                val address = NominatimClient.apiService.reverseGeocode(lat = lat, lon = lng)
                val result = address.display_name ?: "Dirección no encontrada"
                addressCache[cacheKey] = result
                selectedLocationName = result
                onResult(result)
            } catch (e: Exception) {
                if (isActive) {
                    selectedLocationName = "Error al obtener dirección"
                    onResult("Error al obtener dirección")
                }
            } finally {
                if (isActive) { isLoadingAddress = false }
            }
        }
    }

    LaunchedEffect(userLocation) {
        userLocation?.let { (lat, lng) ->
            selectedLocation = Pair(lat, lng)
            getAddressImmediately(lat, lng)
        }
    }

    LaunchedEffect(showNewRouteDialog) {
        if (showNewRouteDialog) {
            hasLocationPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            geocodingJob?.cancel()
            geocodingJob = null
        }
    }

    if (showNewRouteDialog) {
        GetCurrentLocation(
            onLocationResult = { lat, lng ->
                userLocation = Pair(lat, lng)
                selectedLocation = Pair(lat, lng)
                locationStatus = "Ubicación obtenida correctamente"
                getAddressImmediately(lat, lng) { address ->
                    currentLocationAddress = address
                    selectedLocationName = address
                }
                showGpsDialog = false
            },
            onError = { error ->
                locationStatus = error
                currentLocationAddress = "No se pudo obtener la ubicación"
                if (error.contains("GPS", ignoreCase = true) || error.contains("activar", ignoreCase = true)) {
                    showGpsDialog = true
                    gpsError = error
                }
            },
            onPermissionGranted = { hasLocationPermission = true }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
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
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar", modifier = Modifier.size(18.dp))
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
                    Icon(imageVector = Icons.Default.Psychology, contentDescription = "IA", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analizar con IA", color = Color.White)
                }
            }

            FrequentLocationsSection(
                locations = sampleLocations, isDarkTheme = isDarkTheme, textColor = textColor
            )
        }

        // ========== DIÁLOGO COMPLETO CORREGIDO ==========
        if (showNewRouteDialog) {
            Dialog(
                onDismissRequest = {
                    geocodingJob?.cancel()
                    geocodingJob = null
                    showNewRouteDialog = false
                    userLocation = null
                    selectedLocation = null
                    locationStatus = "Esperando..."
                    currentLocationAddress = "Obteniendo ubicación..."
                    selectedLocationName = ""
                },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding() // Añade padding para la status bar
                        .navigationBarsPadding(), // Añade padding para la navigation bar
                    color = Color.Black
                ) {

                Box(modifier = Modifier.fillMaxSize()) {

                        // =================== MAPA ===================
                        if (userLocation != null) {
                            val (lat, lng) = userLocation!!
                            InteractiveMap(
                                modifier = Modifier.fillMaxSize(),
                                zoom = 16.0,
                                userLocation = userLocation,
                                onMarkerMoved = { newLat, newLng ->
                                    selectedLocation = Pair(newLat, newLng)
                                    getAddressFromCoordinates(newLat, newLng) { address ->
                                        selectedLocationName = address
                                    }
                                },
                                onMapReady = { mapView ->
                                    mapView.controller.setCenter(GeoPoint(lat, lng))
                                }
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFF5F5F5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (hasLocationPermission) {
                                        CircularProgressIndicator(
                                            color = primaryColor,
                                            strokeWidth = 3.dp,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            "Obteniendo ubicación...",
                                            fontSize = 16.sp,
                                            color = primaryColor,
                                            textAlign = TextAlign.Center
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.LocationOff,
                                            contentDescription = null,
                                            tint = Color(0xFFFF8F00),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            "Se necesitan permisos de ubicación",
                                            fontSize = 16.sp,
                                            color = Color(0xFFFF8F00),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // =================== CARD SUPERIOR ===================
                        Card(
                            shape = RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 0.dp,
                                bottomStart = 20.dp,
                                bottomEnd = 20.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .statusBarsPadding()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Header con botón de cerrar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Agregar Ubicación",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryColor
                                        )
                                        Text(
                                            "Selecciona el punto en el mapa",
                                            fontSize = 14.sp,
                                            color = textColor.copy(alpha = 0.6f)
                                        )
                                    }

                                    Card(
                                        shape = CircleShape,
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color(0xFFF5F5F5)
                                        ),
                                        modifier = Modifier.size(40.dp),
                                        onClick = {
                                            geocodingJob?.cancel()
                                            geocodingJob = null
                                            showNewRouteDialog = false
                                            userLocation = null
                                            selectedLocation = null
                                            locationStatus = "Esperando..."
                                            currentLocationAddress = "Obteniendo ubicación..."
                                            selectedLocationName = ""
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Cerrar",
                                                tint = textColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                // Input de nombre
                                OutlinedTextField(
                                    value = newRouteName,
                                    onValueChange = { newRouteName = it },
                                    label = { Text("Nombre de la ubicación", color = textColor.copy(alpha = 0.7f)) },
                                    textStyle = LocalTextStyle.current.copy(color = textColor),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    placeholder = { Text("Ej: Casa, Trabajo, Universidad...", color = textColor.copy(alpha = 0.5f)) },
                                    leadingIcon = { Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = primaryColor) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = textColor.copy(alpha = 0.3f)
                                    )
                                )

                                // Información de ubicación seleccionada
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color(0xFFF8F9FA)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Card(
                                            shape = CircleShape,
                                            colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.1f)),
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isLoadingAddress) {
                                                    CircularProgressIndicator(
                                                        color = primaryColor,
                                                        strokeWidth = 2.dp,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.MyLocation,
                                                        contentDescription = null,
                                                        tint = primaryColor,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Ubicación seleccionada",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = primaryColor
                                            )

                                            Text(
                                                text = when {
                                                    isLoadingAddress -> "Obteniendo dirección..."
                                                    selectedLocationName.isNotBlank() &&
                                                            selectedLocationName != "Obteniendo dirección..." &&
                                                            selectedLocationName != "Actualizando..." -> selectedLocationName
                                                    else -> currentLocationAddress
                                                },
                                                fontSize = 12.sp,
                                                color = textColor.copy(alpha = 0.8f),
                                                lineHeight = 16.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            selectedLocation?.let { (lat, lng) ->
                                                Text(
                                                    text = "Lat: ${String.format("%.6f", lat)}, Lng: ${String.format("%.6f", lng)}",
                                                    fontSize = 10.sp,
                                                    color = textColor.copy(alpha = 0.5f),
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }

                                    if (!hasLocationPermission) {
                                        HorizontalDivider(color = textColor.copy(alpha = 0.1f))
                                        Button(
                                            onClick = {
                                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                    data = Uri.fromParts("package", context.packageName, null)
                                                }
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B)),
                                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.LocationOff, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Otorgar permisos de ubicación", fontSize = 12.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        // =================== CARD INFERIOR FIJA ===================
                        if (showNewRouteDialog) {
                            Card(
                                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDarkTheme) Color.Black else Color.White
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .navigationBarsPadding() // Añade padding para la barra de navegación
                                    .imePadding() // Añade padding para el teclado si es necesario
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { /* ... */ },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
                                    ) {
                                        Text("Cancelar", color = textColor)
                                    }

                                    Button(
                                        onClick = { /* ... */ },
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                        enabled = newRouteName.isNotBlank() && selectedLocation != null,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Save,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Guardar", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diálogo GPS
        if (showGpsDialog) {
            AlertDialog(
                onDismissRequest = { showGpsDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.GpsOff, contentDescription = null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "GPS inactivo", color = textColor)
                    }
                },
                text = {
                    Column {
                        Text("Para una mejor experiencia, activa la ubicación de alta precisión en tu dispositivo.", color = textColor.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(gpsError, color = Color(0xFFFF6B6B), fontSize = 14.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { openLocationSettings(context); showGpsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) { Text("Abrir Configuración") }
                },
                dismissButton = {
                    TextButton(onClick = { showGpsDialog = false }) {
                        Text("Cancelar", color = textColor)
                    }
                }
            )
        }
    }
}