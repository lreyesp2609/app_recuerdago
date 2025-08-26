import android.app.Activity
import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.recuerdago.network.NominatimClient
import com.example.recuerdago.screens.DebouncedMapListener
import com.example.recuerdago.screens.GetCurrentLocation
import com.example.recuerdago.screens.GpsEnableButton
import com.example.recuerdago.screens.rutas.cards.LocationCard
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView as OsmMapView
import org.osmdroid.views.overlay.Marker

@Composable
fun SimpleMapView(
    modifier: Modifier = Modifier,
    userLocation: Pair<Double, Double>? = null,
    onMapCenterChanged: ((Double, Double) -> Unit)? = null,
    moveToLocation: Pair<Double, Double>? = null
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
    Configuration.getInstance().load(context, prefs)

    var internalUserLocation by remember { mutableStateOf<Pair<Double, Double>?>(userLocation) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showGpsButton by remember { mutableStateOf(false) }
    var retryCounter by remember { mutableIntStateOf(0) }
    var mapInstance by remember { mutableStateOf<OsmMapView?>(null) }

    var currentAddress by remember { mutableStateOf("Obteniendo dirección...") }

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
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    modifier = Modifier.size(48.dp),
                    tint = Color.Red
                )
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(color = Color(0xFF64B5F6))
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.Text(
                    text = "Obteniendo ubicación...",
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }
        }
        return
    }

    val currentLocation = userLocation ?: internalUserLocation

    // Llamada a Nominatim para obtener dirección
    LaunchedEffect(currentLocation) {
        currentLocation?.let { (lat, lon) ->
            try {
                val response = NominatimClient.apiService.reverseGeocode(
                    lat = lat,
                    lon = lon,
                    format = "json"
                )
                // response ya es NominatimResponse
                currentAddress = response.display_name ?: "Dirección no encontrada"
            } catch (e: Exception) {
                currentAddress = "Error al obtener dirección"
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1️⃣ Mapa (usar una sola instancia)
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                OsmMapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(16.0)
                    mapInstance = this // guardar referencia
                }
            },
            update = { map ->
                // Limpiar overlays para no duplicarlos
                map.overlays.clear()

                currentLocation?.let { (lat, lon) ->
                    val geoPoint = GeoPoint(lat, lon)

                    // Círculo azul
                    val circleSize = 40
                    val shape = ShapeDrawable(OvalShape()).apply {
                        intrinsicHeight = circleSize
                        intrinsicWidth = circleSize
                        paint.color = android.graphics.Color.BLUE
                        paint.style = android.graphics.Paint.Style.FILL
                    }

                    val circleMarker = Marker(map).apply {
                        position = geoPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = shape
                    }
                    map.overlays.add(circleMarker)

                    // Centrar mapa solo si no se movió a otra ubicación
                    moveToLocation?.let { (mlat, mlon) ->
                        map.controller.animateTo(GeoPoint(mlat, mlon))
                    } ?: map.controller.setCenter(geoPoint)
                }
            }
        )

        // 2️⃣ Card con dirección
        currentLocation?.let {
            LocationCard(address = currentAddress, modifier = Modifier.align(Alignment.TopCenter))
        }

        // 3️⃣ Botón para centrar en ubicación actual
        FloatingActionButton(
            onClick = {
                currentLocation?.let { (lat, lon) ->
                    mapInstance?.let { map ->
                        val currentZoom = map.zoomLevelDouble // guardar zoom actual
                        map.controller.setCenter(GeoPoint(lat, lon)) // centrar
                        map.controller.setZoom(currentZoom)         // mantener zoom
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            containerColor = Color(0xFF2196F3),
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Centrar en mi ubicación"
            )
        }
    }
}