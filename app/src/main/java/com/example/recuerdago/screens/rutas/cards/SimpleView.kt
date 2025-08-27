import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recuerdago.network.NominatimClient
import com.example.recuerdago.screens.GetCurrentLocation
import com.example.recuerdago.screens.GpsEnableButton
import com.example.recuerdago.viewmodel.decodePolyline
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView as OsmMapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun SimpleMapView(
    modifier: Modifier = Modifier,
    userLocation: Pair<Double, Double>? = null,
    selectedLocation: Pair<Double, Double>? = null,
    moveToLocation: Pair<Double, Double>? = null,
    savedAddress: String? = null
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
    Configuration.getInstance().load(context, prefs)

    var internalUserLocation by rememberSaveable { mutableStateOf<Pair<Double, Double>?>(userLocation) }
    var currentAddress by rememberSaveable { mutableStateOf("Obteniendo dirección...") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showGpsButton by remember { mutableStateOf(false) }
    var retryCounter by remember { mutableIntStateOf(0) }
    var mapInstance by remember { mutableStateOf<OsmMapView?>(null) }
    var hasInitializedLocation by remember { mutableStateOf(false) } // ✅ Cambio clave
    var showRouteDialog by remember { mutableStateOf(false) } // ✅ Estado para mostrar diálogo de ruta

    // Estado de transporte seleccionado: "walking", "cycling", "driving"
    var selectedMode by rememberSaveable { mutableStateOf("walking") }

    val isDarkTheme = isSystemInDarkTheme()
    val cardBackgroundColor = if (isDarkTheme) Color(0xFF2D2D44) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val secondaryTextColor = if (isDarkTheme) Color(0xFFB0BEC5) else Color.Gray
    val accentColor = Color(0xFF1976D2)

    // Obtener ubicación si no se proporciona
    if (userLocation == null) {
        GetCurrentLocation(
            onLocationResult = { lat, lon ->
                internalUserLocation = lat to lon
                showGpsButton = false
                errorMessage = null
            },
            onError = { error ->
                if (error.contains("GPS no activado") || error.contains("ubicación deshabilitada")) {
                    showGpsButton = true
                    errorMessage = null
                } else {
                    errorMessage = error
                    showGpsButton = false
                }
            },
            onGpsDisabled = {
                showGpsButton = true
                errorMessage = null
            },
            onPermissionGranted = {},
            retryCounter = retryCounter
        )
    }

    val retryGps = {
        showGpsButton = false
        errorMessage = null
        internalUserLocation = null
        retryCounter += 1
    }

    if (errorMessage != null && !showGpsButton) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(imageVector = Icons.Default.Error, contentDescription = "Error", modifier = Modifier.size(48.dp), tint = Color.Red)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = errorMessage!!, color = Color.Red, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
            }
        }
        return
    }

    if (showGpsButton) {
        GpsEnableButton(onEnableGps = retryGps, modifier = modifier)
        return
    }

    val currentLocation = userLocation ?: internalUserLocation
    if (currentLocation == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = accentColor)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Obteniendo ubicación...", color = textColor.copy(alpha = 0.7f))
            }
        }
        return
    }

    val viewModel: MapViewModel = viewModel()
    val route by viewModel.route

    // ✅ Obtener dirección y centrar mapa cuando la ubicación esté disponible
    LaunchedEffect(currentLocation) {
        currentLocation?.let { (lat, lon) ->
            try {
                val response = NominatimClient.apiService.reverseGeocode(lat = lat, lon = lon, format = "json")
                currentAddress = response.display_name ?: "Dirección no encontrada"
            } catch (e: Exception) {
                currentAddress = "Error al obtener dirección"
            }

            // ✅ Centrar el mapa cuando tengamos la ubicación
            if (!hasInitializedLocation) {
                mapInstance?.let { map ->
                    map.controller.setCenter(GeoPoint(lat, lon))
                    map.controller.setZoom(16.0)
                    hasInitializedLocation = true
                }
            }
        }
    }

    // Fetch ruta cuando haya ubicación y selección
    LaunchedEffect(currentLocation, selectedLocation, selectedMode) {
        if (currentLocation != null && selectedLocation != null) {
            viewModel.setMode(selectedMode)
            viewModel.fetchRoute(currentLocation, selectedLocation)
        }
    }

    // ✅ Actualizar overlays cuando cambie la ruta o ubicaciones
    LaunchedEffect(route, currentLocation, selectedLocation, mapInstance) {
        mapInstance?.let { map ->
            map.overlays.clear()

            // Marcador usuario (azul)
            currentLocation?.let { (lat, lon) ->
                val userPoint = GeoPoint(lat, lon)
                val userMarker = Marker(map).apply {
                    position = userPoint
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    icon = ShapeDrawable(OvalShape()).apply {
                        intrinsicHeight = 40
                        intrinsicWidth = 40
                        paint.color = android.graphics.Color.BLUE
                        paint.style = android.graphics.Paint.Style.FILL
                    }
                }
                map.overlays.add(userMarker)
            }

            // Marcador destino (rojo)
            selectedLocation?.let { (lat, lon) ->
                val destPoint = GeoPoint(lat, lon)
                val destMarker = Marker(map).apply {
                    position = destPoint
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    icon = ShapeDrawable(OvalShape()).apply {
                        intrinsicHeight = 40
                        intrinsicWidth = 40
                        paint.color = android.graphics.Color.RED
                        paint.style = android.graphics.Paint.Style.FILL
                    }
                }
                map.overlays.add(destMarker)

                // ✅ Centrar en el marcador rojo cuando se seleccione
                map.controller.animateTo(destPoint)
            }

            // Dibujar polyline de la ruta
            route?.routes?.firstOrNull()?.let { routeData ->
                val polyline = Polyline().apply {
                    setPoints(routeData.geometry.decodePolyline())
                    outlinePaint.color = android.graphics.Color.BLUE
                    outlinePaint.strokeWidth = 6f
                }
                map.overlays.add(polyline)
            }

            // ✅ Mover a ubicación específica si se proporciona
            moveToLocation?.let { (mlat, mlon) ->
                map.controller.animateTo(GeoPoint(mlat, mlon))
            }

            // Refrescar el mapa
            map.invalidate()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Mapa
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                OsmMapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(16.0) // ✅ Mismo zoom que el botón centrar
                    mapInstance = this
                }
            }
        )

        // Card superior: ubicación actual
        currentLocation?.let {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Tu Ubicación", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = accentColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = currentAddress, fontSize = 14.sp, color = secondaryTextColor)
                }
            }
        }

        // Card inferior: ubicación guardada
        savedAddress?.let { address ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .fillMaxWidth(0.8f),
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Ubicación Guardada", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = accentColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = address, fontSize = 14.sp, color = secondaryTextColor)
                }
            }
        }

        // Botones transporte y centrar
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Botón de ruta
            FloatingActionButton(
                onClick = {
                    if (selectedLocation != null) {
                        showRouteDialog = true
                    }
                },
                containerColor = if (selectedLocation != null) Color(0xFF4CAF50) else Color.Gray,
                contentColor = Color.White,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Directions, contentDescription = "Mostrar ruta")
            }

            FloatingActionButton(
                onClick = { selectedMode = "walking" },
                containerColor = if (selectedMode == "walking") accentColor else Color.LightGray,
                contentColor = Color.White,
                modifier = Modifier.size(56.dp)
            ) { Icon(Icons.Default.DirectionsWalk, contentDescription = "Caminar") }

            FloatingActionButton(
                onClick = { selectedMode = "cycling" },
                containerColor = if (selectedMode == "cycling") accentColor else Color.LightGray,
                contentColor = Color.White,
                modifier = Modifier.size(56.dp)
            ) { Icon(Icons.Default.DirectionsBike, contentDescription = "Bicicleta") }

            FloatingActionButton(
                onClick = { selectedMode = "driving" },
                containerColor = if (selectedMode == "driving") accentColor else Color.LightGray,
                contentColor = Color.White,
                modifier = Modifier.size(56.dp)
            ) { Icon(Icons.Default.DirectionsCar, contentDescription = "Carro") }

            FloatingActionButton(
                onClick = {
                    currentLocation?.let { (lat, lon) ->
                        mapInstance?.controller?.apply {
                            animateTo(GeoPoint(lat, lon))
                            setZoom(16.0)
                        }
                    }
                },
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White,
                modifier = Modifier.size(56.dp)
            ) { Icon(Icons.Default.MyLocation, contentDescription = "Centrar en mi ubicación") }
        }

        // Diálogo de selección de ruta
        if (showRouteDialog) {
            AlertDialog(
                onDismissRequest = { showRouteDialog = false },
                title = { Text("Seleccionar modo de transporte") },
                text = {
                    Column {
                        Text("¿Cómo quieres llegar a tu destino?")
                        Spacer(modifier = Modifier.height(16.dp))

                        // Opción Caminar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedMode = "walking"
                                    showRouteDialog = false
                                    // Forzar recálculo de ruta
                                    if (currentLocation != null && selectedLocation != null) {
                                        viewModel.setMode(selectedMode)
                                        viewModel.fetchRoute(currentLocation, selectedLocation)
                                    }
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = accentColor)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Caminando", fontSize = 16.sp)
                        }

                        // Opción Bicicleta
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedMode = "cycling"
                                    showRouteDialog = false
                                    if (currentLocation != null && selectedLocation != null) {
                                        viewModel.setMode(selectedMode)
                                        viewModel.fetchRoute(currentLocation, selectedLocation)
                                    }
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DirectionsBike, contentDescription = null, tint = accentColor)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("En bicicleta", fontSize = 16.sp)
                        }

                        // Opción Carro
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedMode = "driving"
                                    showRouteDialog = false
                                    if (currentLocation != null && selectedLocation != null) {
                                        viewModel.setMode(selectedMode)
                                        viewModel.fetchRoute(currentLocation, selectedLocation)
                                    }
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = accentColor)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("En carro", fontSize = 16.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRouteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}