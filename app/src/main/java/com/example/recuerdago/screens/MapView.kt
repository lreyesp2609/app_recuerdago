package com.example.recuerdago.screens

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Composable
fun MiniMap(
    modifier: Modifier = Modifier,
    zoom: Double = 15.0,
    onMapReady: (MapView) -> Unit = {}
) {
    AndroidView(
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                controller.setZoom(zoom)
                controller.setCenter(GeoPoint(0.0, 0.0)) // Inicialmente
                setMultiTouchControls(true)
                onMapReady(this)
            }
        },
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .height(180.dp) // mini-mapa
    )
}
