package com.example.recuerdago.screens

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
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
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView as OsmMapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import com.example.recuerdago.R

class CenteredPinOverlay(private val context: Context) : Overlay() {
    private val pinDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_marker_red)

    override fun draw(canvas: Canvas?, mapView: OsmMapView?, shadow: Boolean) {
        if (canvas == null || mapView == null || shadow) return

        pinDrawable?.let { drawable ->
            val centerX = mapView.width / 2
            val centerY = mapView.height / 2
            val width = drawable.intrinsicWidth
            val height = drawable.intrinsicHeight
            val left = centerX - width / 2
            val top = centerY - height
            val right = left + width
            val bottom = centerY
            drawable.setBounds(left, top, right, bottom)
            drawable.draw(canvas)
        }
    }
}

// MapListener con debounce
class DebouncedMapListener(
    private val onMapCenterChanged: (Double, Double) -> Unit,
    private val debounceMs: Long = 800L // Esperar 800ms después del último evento
) : MapListener {
    private var lastEventTime = 0L
    private var pendingCenter: GeoPoint? = null

    override fun onScroll(event: ScrollEvent?): Boolean {
        event?.source?.mapCenter?.let { center ->
            lastEventTime = System.currentTimeMillis()
            pendingCenter = GeoPoint(center.latitude, center.longitude)
            scheduleReport()
        }
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        event?.source?.mapCenter?.let { center ->
            lastEventTime = System.currentTimeMillis()
            pendingCenter = GeoPoint(center.latitude, center.longitude)
            scheduleReport()
        }
        return true
    }

    private fun scheduleReport() {
        val currentEventTime = lastEventTime
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (currentEventTime == lastEventTime && pendingCenter != null) {
                onMapCenterChanged(pendingCenter!!.latitude, pendingCenter!!.longitude)
                pendingCenter = null
            }
        }, debounceMs)
    }
}

@Composable
fun MapView(
    modifier: Modifier = Modifier,
    onLocationSelected: ((Double, Double) -> Unit)? = null,
    onMapCenterChanged: ((Double, Double) -> Unit)? = null,
    moveToLocation: Pair<Double, Double>? = null
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
    Configuration.getInstance().load(context, prefs)

    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var mapInstance by remember { mutableStateOf<OsmMapView?>(null) }
    var showGpsButton by remember { mutableStateOf(false) }
    var retryCounter by remember { mutableIntStateOf(0) }

    // Función para centrar el mapa en la ubicación del usuario
    val centerOnUserLocation: () -> Unit = {
        userLocation?.let { (lat, lon) ->
            mapInstance?.let { map ->
                val geoPoint = GeoPoint(lat, lon)
                map.controller.animateTo(geoPoint)
            }
        }
        Unit // Asegurar que retorne Unit
    }

    // Función para reintentar GPS
    val retryGps = {
        showGpsButton = false
        errorMessage = null
        userLocation = null // Reset location to show loading again
        retryCounter += 1 // Increment counter to trigger retry
    }

    GetCurrentLocation(
        onLocationResult = { lat, lon ->
            userLocation = lat to lon
            onLocationSelected?.invoke(lat, lon)
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

    // Efecto para mover el mapa cuando se recibe una nueva ubicación de búsqueda
    LaunchedEffect(moveToLocation) {
        moveToLocation?.let { (lat, lon) ->
            mapInstance?.let { map ->
                val geoPoint = GeoPoint(lat, lon)
                map.controller.animateTo(geoPoint)
            }
        }
    }

    // Mostrar error de permisos u otros errores (no GPS)
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

    // Mostrar botón para habilitar GPS
    if (showGpsButton) {
        GpsEnableButton(
            onEnableGps = retryGps,
            modifier = modifier
        )
        return
    }

    // Mostrar loading mientras obtenemos ubicación
    if (userLocation == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = Color(0xFF64B5F6)
                )
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.Text(
                    text = "Obteniendo ubicación...",
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }
        }
        return
    }

    // Mostrar mapa con botón flotante
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                OsmMapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(16.0)
                    mapInstance = this

                    // Agregar el listener con debounce
                    onMapCenterChanged?.let { callback ->
                        addMapListener(DebouncedMapListener(callback))
                    }
                }
            },
            update = { map ->
                map.overlays.clear()

                userLocation?.let { (lat, lon) ->
                    val geoPoint = GeoPoint(lat, lon)

                    // Círculo azul estático
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

                    // Pin rojo centrado
                    val centeredPinOverlay = CenteredPinOverlay(context)
                    map.overlays.add(centeredPinOverlay)

                    // Centrar cámara inicialmente
                    val currentCenter = map.mapCenter
                    if (currentCenter.latitude == 0.0 && currentCenter.longitude == 0.0) {
                        map.controller.setCenter(geoPoint)
                    }
                }
            }
        )

        // Botón flotante para centrar en ubicación del usuario
        FloatingActionButton(
            onClick = centerOnUserLocation,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            containerColor = Color(0xFF2196F3),
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Centrar en mi ubicación",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}