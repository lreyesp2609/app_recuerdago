package com.example.recuerdago.screens

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.recuerdago.R
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Composable
fun InteractiveMap(
    modifier: Modifier = Modifier,
    zoom: Double = 15.0,
    userLocation: Pair<Double, Double>? = null,
    onMarkerMoved: (Double, Double) -> Unit = { _, _ -> },
    onMapReady: (MapView) -> Unit = {}
) {
    val context = LocalContext.current
    val view = LocalView.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var userLocationMarker by remember { mutableStateOf<org.osmdroid.views.overlay.Marker?>(null) }
    var centerMarker by remember { mutableStateOf<org.osmdroid.views.overlay.Marker?>(null) }

    // Configurar barras SOLO al inicializar el composable
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
            it.navigationBarColor = Color.Black.toArgb()
            it.statusBarColor = Color.Black.toArgb()

            val insetsController = WindowCompat.getInsetsController(it, it.decorView)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }

        onDispose { }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    // IMPORTANTE: Configurar para permitir que el sistema maneje los gestos
                    setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS)
                    isFocusable = true
                    isFocusableInTouchMode = true

                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(zoom)

                    // Configurar para no interferir con las system bars
                    systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                }.also { map ->
                    // Marcador azul del usuario
                    userLocation?.let { (lat, lng) ->
                        val userMarker = org.osmdroid.views.overlay.Marker(map)
                        userMarker.position = GeoPoint(lat, lng)
                        userMarker.setAnchor(
                            org.osmdroid.views.overlay.Marker.ANCHOR_CENTER,
                            org.osmdroid.views.overlay.Marker.ANCHOR_CENTER
                        )
                        userMarker.title = "Tu ubicación actual"
                        userMarker.isDraggable = false
                        userMarker.icon =
                            ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)
                        map.overlays.add(userMarker)
                        userLocationMarker = userMarker

                        map.controller.setCenter(GeoPoint(lat, lng))
                    }

                    // Marcador rojo en el centro
                    val redMarker = org.osmdroid.views.overlay.Marker(map).apply {
                        setAnchor(
                            org.osmdroid.views.overlay.Marker.ANCHOR_CENTER,
                            org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM
                        )
                        title = "Ubicación seleccionada"
                        icon = try {
                            ContextCompat.getDrawable(context, R.drawable.ic_marker_red)
                        } catch (e: Exception) {
                            ContextCompat.getDrawable(context, android.R.drawable.ic_dialog_map)
                        }
                        position = userLocation?.let { GeoPoint(it.first, it.second) } ?: GeoPoint(0.0, 0.0)
                    }
                    map.overlays.add(redMarker)
                    centerMarker = redMarker

                    mapView = map
                    onMapReady(map)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { map ->
                mapView = map

                map.addMapListener(object : org.osmdroid.events.MapListener {
                    override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean {
                        val center = map.mapCenter
                        centerMarker?.position = GeoPoint(center.latitude, center.longitude)
                        onMarkerMoved(center.latitude, center.longitude)
                        map.invalidate()
                        return true
                    }

                    override fun onZoom(event: org.osmdroid.events.ZoomEvent?): Boolean {
                        val center = map.mapCenter
                        centerMarker?.position = GeoPoint(center.latitude, center.longitude)
                        onMarkerMoved(center.latitude, center.longitude)
                        map.invalidate()
                        return true
                    }
                })
            }
        )

        // Botón para centrar en el usuario
        userLocation?.let { location ->
            FloatingActionButton(
                onClick = {
                    mapView?.controller?.setCenter(GeoPoint(location.first, location.second))
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .navigationBarsPadding(),
                containerColor = Color.White,
                contentColor = Color(0xFF1976D2),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Ir a mi ubicación",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}