package org.example.project

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun MapContent(modifier: Modifier) {
    var locationOverlayRef by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var selectedPOI by remember { mutableStateOf<POI?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    controller.setCenter(GeoPoint(45.7489, 21.2087))

                    // Location overlay
                    val overlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                    overlay.enableMyLocation()
                    overlay.enableFollowLocation()
                    val personBitmap = createLocationBitmap(48,
                        AndroidColor.rgb(0, 82, 204),
                        AndroidColor.argb(60, 0, 82, 204)
                    )
                    val arrowBitmap = createArrowBitmap(64, AndroidColor.rgb(0, 82, 204))
                    overlay.setPersonIcon(personBitmap)
                    overlay.setDirectionArrow(personBitmap, arrowBitmap)
                    overlays.add(overlay)
                    locationOverlayRef = overlay
                    mapViewRef = this

                    // Add POI markers
                    POIStore.pois.forEach { poi ->
                        val marker = Marker(this)
                        marker.position = GeoPoint(poi.lat, poi.lon)
                        marker.title = poi.name
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.icon = android.graphics.drawable.BitmapDrawable(
                            context.resources,
                            createPOIMarker(poi.emoji)
                        )
                        marker.setOnMarkerClickListener { _, _ ->
                            selectedPOI = poi
                            true
                        }
                        overlays.add(marker)
                    }
                }
            },
            update = { view -> mapViewRef = view }
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

    // POI bottom sheet
    if (selectedPOI != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedPOI = null },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            POIBottomSheet(
                poi = selectedPOI!!,
                onVote = { featureId, isUpvote ->
                    POIStore.vote(selectedPOI!!.id, featureId, isUpvote)
                    refreshTrigger++ // force recomposition
                },
                getUserVote = { featureId ->
                    POIStore.getUserVote(selectedPOI!!.id, featureId)
                },
                refreshTrigger = refreshTrigger
            )
        }
    }
}

@Composable
fun POIBottomSheet(
    poi: POI,
    onVote: (featureId: String, isUpvote: Boolean) -> Unit,
    getUserVote: (featureId: String) -> Boolean?,
    refreshTrigger: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(poi.category, fontSize = 13.sp, color = Color.Gray)
        Text(poi.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D1B2A))

        HorizontalDivider()

        Text(
            "Community Accessibility Votes",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF0052CC)
        )

        Text(
            "Tap ✓ or ✗ to vote based on your experience",
            fontSize = 13.sp,
            color = Color.Gray
        )

        // Feature rows
        poi.features.forEach { feature ->
            val userVote = getUserVote(feature.id)
            val confidenceColor = when (feature.confidenceColor) {
                "green" -> Color(0xFF2E7D32)
                "orange" -> Color(0xFFF57C00)
                "red" -> Color(0xFFCC0000)
                else -> Color.Gray
            }

            // Highlight if relevant to user's profile
            val profile = ProfileStore.profile
            val isRelevant = when (feature.id) {
                "wheelchair" -> profile.wheelchair
                "hearing" -> profile.hearingSensitivity
                "visual" -> profile.visualImpairment
                "neurodivergent" -> profile.autismSensory
                else -> false
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isRelevant) Color(0xFFE8F0FE) else Color(0xFFF8F8F8)
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(feature.emoji, fontSize = 20.sp)
                            Text(
                                feature.label,
                                fontSize = 14.sp,
                                fontWeight = if (isRelevant) FontWeight.Bold else FontWeight.Normal,
                                color = if (isRelevant) Color(0xFF0052CC) else Color(0xFF1A1A1A)
                            )
                        }
                        if (isRelevant) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0052CC)
                            ) {
                                Text(
                                    "Your need",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Vote buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Yes button
                        Button(
                            onClick = { onVote(feature.id, true) },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (userVote == true)
                                    Color(0xFF2E7D32) else Color(0xFFE8F5E9)
                            )
                        ) {
                            Text(
                                "✓ Yes  ${feature.upvotes}",
                                fontSize = 13.sp,
                                color = if (userVote == true) Color.White else Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // No button
                        Button(
                            onClick = { onVote(feature.id, false) },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (userVote == false)
                                    Color(0xFFCC0000) else Color(0xFFFFEBEE)
                            )
                        ) {
                            Text(
                                "✗ No  ${feature.downvotes}",
                                fontSize = 13.sp,
                                color = if (userVote == false) Color.White else Color(0xFFCC0000),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Confidence indicator
                    Text(
                        feature.confidence,
                        fontSize = 12.sp,
                        color = confidenceColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

private fun createPOIMarker(emoji: String): Bitmap {
    val size = 80
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // White circle background
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, Paint().apply {
        color = AndroidColor.WHITE
        isAntiAlias = true
    })

    // Blue border
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, Paint().apply {
        color = AndroidColor.rgb(0, 82, 204)
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    })

    // Emoji text
    val paint = Paint().apply {
        textSize = 36f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(emoji, size / 2f, size / 2f + 13f, paint)

    return bitmap
}

private fun createLocationBitmap(size: Int, innerColor: Int, outerColor: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, Paint().apply {
        color = outerColor; isAntiAlias = true
    })
    canvas.drawCircle(size / 2f, size / 2f, size / 3.5f, Paint().apply {
        color = innerColor; isAntiAlias = true
    })
    canvas.drawCircle(size / 2f, size / 2f, size / 3.5f, Paint().apply {
        color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    })
    return bitmap
}

private fun createArrowBitmap(size: Int, color: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val path = android.graphics.Path().apply {
        moveTo(size / 2f, 0f)
        lineTo(size.toFloat(), size.toFloat())
        lineTo(size / 2f, size * 0.75f)
        lineTo(0f, size.toFloat())
        close()
    }
    canvas.drawPath(path, Paint().apply { this.color = color; isAntiAlias = true })
    canvas.drawPath(path, Paint().apply {
        this.color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    })
    return bitmap
}