package com.example.recuerdago.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.recuerdago.R
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Composable
fun MiniMap(
    modifier: Modifier = Modifier,
    zoom: Double = 15.0,
    userLocation: Pair<Double, Double>? = null,
    onMapReady: (MapView) -> Unit = {},
    onMarkerAdded: (Double, Double) -> Unit = { _, _ -> },
    selectedMarker: Pair<Double, Double>? = null,
    showRecenterButton: Boolean = true
) {
    var mapView by remember { mutableStateOf<MapView?>(null) }

    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    controller.setZoom(zoom)
                    controller.setCenter(GeoPoint(0.0, 0.0))
                    setMultiTouchControls(true)

                    // Vector para los marcadores dinámicos
                    val markersOverlay =
                        org.osmdroid.views.overlay.ItemizedIconOverlay<org.osmdroid.views.overlay.OverlayItem>(
                            mutableListOf(),
                            object :
                                org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener<org.osmdroid.views.overlay.OverlayItem> {
                                override fun onItemSingleTapUp(
                                    index: Int, item: org.osmdroid.views.overlay.OverlayItem?
                                ): Boolean {
                                    return true
                                }

                                override fun onItemLongPress(
                                    index: Int, item: org.osmdroid.views.overlay.OverlayItem?
                                ): Boolean {
                                    return false
                                }
                            },
                            context
                        )

                    // Agregar el overlay de marcadores al mapa
                    this.overlays.add(markersOverlay)

                    // Listener para detectar toques en el mapa
                    val mapEventsReceiver = object : org.osmdroid.events.MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            p?.let { geoPoint ->
                                onMarkerAdded(geoPoint.latitude, geoPoint.longitude)
                            }
                            return true
                        }

                        override fun longPressHelper(p: GeoPoint?): Boolean {
                            return false
                        }
                    }

                    // Agregar el receptor de eventos del mapa
                    val mapEventsOverlay =
                        org.osmdroid.views.overlay.MapEventsOverlay(mapEventsReceiver)
                    this.overlays.add(0, mapEventsOverlay)

                    // Marcador de ubicación del usuario (si existe)
                    userLocation?.let { (lat, lng) ->
                        val userMarker = org.osmdroid.views.overlay.Marker(this)
                        userMarker.position = GeoPoint(lat, lng)
                        userMarker.setAnchor(
                            org.osmdroid.views.overlay.Marker.ANCHOR_CENTER,
                            org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM
                        )
                        userMarker.title = "Mi ubicación"
                        userMarker.icon = ContextCompat.getDrawable(context, R.drawable.ic_marker_red)
                        this.overlays.add(userMarker)
                        this.controller.setCenter(GeoPoint(lat, lng))
                    }

                    // Guardar referencia del mapa
                    mapView = this
                    onMapReady(this)
                }
            },
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .fillMaxSize(),
            update = { map ->
                // Actualizar referencia del mapa
                mapView = map

                // Esta función se ejecuta cuando selectedMarker cambia
                selectedMarker?.let { (lat, lng) ->
                    // Encontrar el overlay de marcadores
                    val markersOverlay = map.overlays.find {
                        it is org.osmdroid.views.overlay.ItemizedIconOverlay<*>
                    } as? org.osmdroid.views.overlay.ItemizedIconOverlay<org.osmdroid.views.overlay.OverlayItem>

                    markersOverlay?.let { overlay ->
                        // Limpiar marcadores anteriores
                        overlay.removeAllItems()

                        // Crear nuevo marcador
                        val newMarker = org.osmdroid.views.overlay.OverlayItem(
                            "Marcador seleccionado",
                            "Lat: $lat, Lng: $lng",
                            GeoPoint(lat, lng)
                        )
                        newMarker.setMarker(
                            ContextCompat.getDrawable(map.context, R.drawable.ic_marker_blue)
                        )

                        // Agregar el marcador
                        overlay.addItem(newMarker)

                        // Refrescar el mapa
                        map.invalidate()
                    }
                }
            }
        )

        // Botón flotante para recentrar en la ubicación del usuario
        if (showRecenterButton && userLocation != null) {
            FloatingActionButton(
                onClick = {
                    userLocation?.let { (lat, lng) ->
                        mapView?.controller?.animateTo(
                            GeoPoint(lat, lng),
                            zoom,
                            1000L // Duración de la animación en ms
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(40.dp),
                containerColor = Color.White,
                contentColor = Color(0xFF1976D2),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Centrar en mi ubicación",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}