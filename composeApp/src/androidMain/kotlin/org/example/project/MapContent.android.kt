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
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import android.graphics.Paint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor

@Composable
actual fun MapContent(modifier: Modifier) {
    var locationOverlayRef by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    controller.setCenter(GeoPoint(45.7489, 21.2087))

                    val overlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                    overlay.enableMyLocation()
                    overlay.enableFollowLocation()

                    // Create a blue person icon bitmap
                    val size = 48
                    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)

                    // Outer blue circle
                    val paintOuter = Paint().apply {
                        color = AndroidColor.argb(60, 0, 82, 204)
                        isAntiAlias = true
                    }
                    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paintOuter)

                    // Inner solid blue circle
                    val paintInner = Paint().apply {
                        color = AndroidColor.rgb(0, 82, 204)
                        isAntiAlias = true
                    }
                    canvas.drawCircle(size / 2f, size / 2f, size / 4f, paintInner)

                    // White border
                    val paintBorder = Paint().apply {
                        color = AndroidColor.WHITE
                        style = Paint.Style.STROKE
                        strokeWidth = 3f
                        isAntiAlias = true
                    }
                    canvas.drawCircle(size / 2f, size / 2f, size / 4f, paintBorder)

                    overlay.setPersonIcon(bitmap)
                    overlay.setDirectionIcon(bitmap)

                    overlays.add(overlay)
                    locationOverlayRef = overlay
                    mapViewRef = this
                }
            },
            update = { view ->
                mapViewRef = view
            }
        )

        // Center on me button
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