import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
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
import com.example.recuerdago.network.NominatimClient
import com.example.recuerdago.screens.GetCurrentLocation
import com.example.recuerdago.screens.GpsEnableButton
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView as OsmMapView
import org.osmdroid.views.overlay.Marker

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

    if (userLocation == null && internalUserLocation == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator(color = accentColor)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Obteniendo ubicación...", color = textColor.copy(alpha = 0.7f))
            }
        }
        return
    }

    val currentLocation = userLocation ?: internalUserLocation

    // Obtener dirección mediante Nominatim
    LaunchedEffect(currentLocation) {
        currentLocation?.let { (lat, lon) ->
            try {
                val response = NominatimClient.apiService.reverseGeocode(lat = lat, lon = lon, format = "json")
                currentAddress = response.display_name ?: "Dirección no encontrada"
            } catch (e: Exception) {
                currentAddress = "Error al obtener dirección"
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

        // 1️⃣ Mapa
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                OsmMapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(16.0)
                    mapInstance = this
                }
            },
            update = { map ->
                map.overlays.clear()

                // Marcador azul: usuario actual
                currentLocation?.let { (lat, lon) ->
                    val geoPoint = GeoPoint(lat, lon)
                    val shape = ShapeDrawable(OvalShape()).apply {
                        intrinsicHeight = 40
                        intrinsicWidth = 40
                        paint.color = android.graphics.Color.BLUE
                        paint.style = android.graphics.Paint.Style.FILL
                    }
                    val marker = Marker(map).apply {
                        position = geoPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = shape
                    }
                    map.overlays.add(marker)
                }

                // Marcador rojo: ubicación seleccionada
                selectedLocation?.let { (lat, lon) ->
                    val geoPoint = GeoPoint(lat, lon)
                    val shape = ShapeDrawable(OvalShape()).apply {
                        intrinsicHeight = 40
                        intrinsicWidth = 40
                        paint.color = android.graphics.Color.RED
                        paint.style = android.graphics.Paint.Style.FILL
                    }
                    val marker = Marker(map).apply {
                        position = geoPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = shape
                    }
                    map.overlays.add(marker)
                    map.controller.animateTo(geoPoint)
                }

                moveToLocation?.let { (mlat, mlon) ->
                    map.controller.animateTo(GeoPoint(mlat, mlon))
                }
            }
        )

        // 2️⃣ Card superior: tu ubicación
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

        // 3️⃣ Card inferior: ubicación guardada
        savedAddress?.let { address ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart) // 👈 ahora va a la izquierda
                    .padding(16.dp)
                    .fillMaxWidth(0.8f), // 👈 ocupa 80% del ancho
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

        // 4️⃣ Botones de transporte + centrar (abajo derecha)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            // 🚶 A pie
            FloatingActionButton(
                onClick = { selectedMode = "walking" },
                containerColor = if (selectedMode == "walking") accentColor else Color.LightGray,
                contentColor = Color.White,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.DirectionsWalk, contentDescription = "Caminar")
            }

            // 🚲 Bicicleta
            FloatingActionButton(
                onClick = { selectedMode = "cycling" },
                containerColor = if (selectedMode == "cycling") accentColor else Color.LightGray,
                contentColor = Color.White,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.DirectionsBike, contentDescription = "Bicicleta")
            }

            // 🚗 Carro
            FloatingActionButton(
                onClick = { selectedMode = "driving" },
                containerColor = if (selectedMode == "driving") accentColor else Color.LightGray,
                contentColor = Color.White,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.DirectionsCar, contentDescription = "Carro")
            }

            // 🎯 Centrar mapa
            FloatingActionButton(
                onClick = {
                    currentLocation?.let { (lat, lon) ->
                        mapInstance?.let { map ->
                            val currentZoom = map.zoomLevelDouble
                            map.controller.setCenter(GeoPoint(lat, lon))
                            map.controller.setZoom(currentZoom)
                        }
                    }
                },
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Centrar en mi ubicación")
            }
        }
    }
}


@Composable
fun TransportIcon(icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(
                if (selected) Color(0xFF1976D2) else Color.Transparent,
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Color.White else Color.Gray
        )
    }
}
