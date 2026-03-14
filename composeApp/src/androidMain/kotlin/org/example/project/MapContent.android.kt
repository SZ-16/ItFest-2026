package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
actual fun MapContent(modifier: Modifier) {
    var locationOverlayRef by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    Box(modifier = modifier) {

        // Map
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    controller.setCenter(GeoPoint(45.7489, 21.2087)) // Timișoara

                    val overlay = MyLocationNewOverlay(this)
                    overlay.enableMyLocation()
                    overlay.enableFollowLocation()
                    overlays.add(overlay)

                    locationOverlayRef = overlay
                    mapViewRef = this
                }
            },
            update = { view ->
                mapViewRef = view
            }
        )

        // "Center on me" button
        FloatingActionButton(
            onClick = {
                locationOverlayRef?.myLocation?.let { location ->
                    mapViewRef?.controller?.animateTo(location)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
                .navigationBarsPadding(),
            containerColor = Color.White,
            contentColor = Color(0xFF0052CC)
        ) {
            Text("📍", fontSize = 22.sp)
        }
    }
}