package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
actual fun MapContent(modifier: Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                // Center on Timișoara
                controller.setZoom(14.0)
                controller.setCenter(GeoPoint(45.7489, 21.2087))

                // User location overlay
                val locationOverlay = MyLocationNewOverlay(this)
                locationOverlay.enableMyLocation()
                overlays.add(locationOverlay)
            }
        },
        update = { mapView ->
            mapView.onResume()
        }
    )
}
