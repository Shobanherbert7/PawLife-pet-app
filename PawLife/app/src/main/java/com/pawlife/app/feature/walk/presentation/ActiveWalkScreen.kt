package com.pawlife.app.feature.walk.presentation

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.pawlife.app.feature.walk.WalkTrackingService

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ActiveWalkScreen(
    petId: Long,
    viewModel: ActiveWalkViewModel = hiltViewModel(),
    onWalkFinished: () -> Unit
) {
    val context = LocalContext.current
    val isTracking by viewModel.isTracking.collectAsStateWithLifecycle()
    val routePoints by viewModel.routePoints.collectAsStateWithLifecycle()
    val distance by viewModel.distanceMeters.collectAsStateWithLifecycle()
    val durationSeconds by viewModel.durationSeconds.collectAsStateWithLifecycle()
    val paceMinPerKm by viewModel.paceMinPerKm.collectAsStateWithLifecycle()

    val isBleConnected by viewModel.isBleConnected.collectAsStateWithLifecycle()
    val heartRateBpm by viewModel.heartRateBpm.collectAsStateWithLifecycle()
    val collarSteps by viewModel.collarSteps.collectAsStateWithLifecycle()
    val batteryLevel by viewModel.batteryLevel.collectAsStateWithLifecycle()

    val permissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        permissions.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
    }

    val locationPermissionState = rememberMultiplePermissionsState(permissions)

    LaunchedEffect(Unit) {
        if (!locationPermissionState.allPermissionsGranted) {
            locationPermissionState.launchMultiplePermissionRequest()
        }
    }

    val latLngList = routePoints.map { LatLng(it.latitude, it.longitude) }
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(latLngList.lastOrNull()) {
        latLngList.lastOrNull()?.let { lastLoc ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(lastLoc, 17f)
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (locationPermissionState.allPermissionsGranted) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true)
                ) {
                    // Geofence Safe Zone Perimeter (Home)
                    Circle(
                        center = LatLng(13.0827, 80.2707),
                        radius = 150.0,
                        strokeColor = androidx.compose.ui.graphics.Color(0xFF10B981),
                        strokeWidth = 4f,
                        fillColor = androidx.compose.ui.graphics.Color(0x2210B981)
                    )

                    if (latLngList.isNotEmpty()) {
                        Polyline(
                            points = latLngList,
                            color = MaterialTheme.colorScheme.primary,
                            width = 12f
                        )
                    }
                }

                // Top Collar Telemetry HUD
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    color = com.pawlife.app.presentation.theme.CardDark.copy(alpha = 0.94f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.1f)),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = "BLE",
                                tint = if (isBleConnected) com.pawlife.app.presentation.theme.Primary else com.pawlife.app.presentation.theme.SlateSubtle,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isBleConnected) "Collar Live" else "Collar Ready",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = if (isBleConnected) com.pawlife.app.presentation.theme.Primary else com.pawlife.app.presentation.theme.SlateSubtle
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "BPM",
                                tint = androidx.compose.ui.graphics.Color(0xFFFF5252),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$heartRateBpm BPM",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                                contentDescription = "Steps",
                                tint = com.pawlife.app.presentation.theme.Secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$collarSteps",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = Icons.Default.BatteryFull,
                                contentDescription = "Battery",
                                tint = com.pawlife.app.presentation.theme.Success,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$batteryLevel%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    color = com.pawlife.app.presentation.theme.CardDark,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WalkStatItem("Duration", formatDuration(durationSeconds))
                            WalkStatItem("Distance", "${"%.2f".format(distance / 1000f)} km")
                            WalkStatItem("Pace", "${"%.1f".format(paceMinPerKm)} /km")
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        if (!isTracking) {
                            Button(
                                onClick = {
                                    val intent = Intent(context, WalkTrackingService::class.java).apply {
                                        action = WalkTrackingService.ACTION_START
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        context.startForegroundService(intent)
                                    } else {
                                        context.startService(intent)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.pawlife.app.presentation.theme.Primary, contentColor = androidx.compose.ui.graphics.Color.Black)
                            ) {
                                Text("Start Walk", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    val intent = Intent(context, WalkTrackingService::class.java).apply {
                                        action = WalkTrackingService.ACTION_STOP
                                    }
                                    context.startService(intent)
                                    viewModel.saveWalk(petId)
                                    onWalkFinished()
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.pawlife.app.presentation.theme.Warning, contentColor = androidx.compose.ui.graphics.Color.Black)
                            ) {
                                Text("Stop & Save Walk", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Location permission is required to track walks.",
                    color = com.pawlife.app.presentation.theme.SlateLight,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun WalkStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = com.pawlife.app.presentation.theme.SlateSubtle)
        Text(text = value, style = MaterialTheme.typography.titleLarge, color = androidx.compose.ui.graphics.Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
}
