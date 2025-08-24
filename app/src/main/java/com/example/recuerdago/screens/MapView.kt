package com.example.recuerdago.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    onMarkerAdded: (Double, Double) -> Unit = { _, _ -> }, // Parámetro existente
    selectedMarker: Pair<Double, Double>? = null // NUEVO parámetro para estado compartido
) {
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
                            // Solo notificar al callback, no crear el marcador aquí
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
                    controller.setCenter(GeoPoint(lat, lng))
                }

                onMapReady(this)
            }
        },
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .fillMaxSize(),
        update = { mapView ->
            // Esta función se ejecuta cuando selectedMarker cambia
            selectedMarker?.let { (lat, lng) ->
                // Encontrar el overlay de marcadores
                val markersOverlay = mapView.overlays.find {
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
                        ContextCompat.getDrawable(mapView.context, R.drawable.ic_marker_blue)
                    )

                    // Agregar el marcador
                    overlay.addItem(newMarker)

                    // Refrescar el mapa
                    mapView.invalidate()
                }
            }
        }
    )
}