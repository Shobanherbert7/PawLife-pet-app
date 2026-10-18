package com.pawlife.app.feature.walk.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawlife.app.core.data.db.entity.ActivityLogEntity
import com.pawlife.app.core.domain.repository.ActivityRepository
import com.pawlife.app.feature.walk.WalkTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActiveWalkViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    val routePoints = WalkTracker.routePoints.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val isTracking = WalkTracker.isTracking.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val isBleConnected = com.pawlife.app.data.service.BleWearableManager.isConnected
    val heartRateBpm = com.pawlife.app.data.service.BleWearableManager.heartRateBpm
    val collarSteps = com.pawlife.app.data.service.BleWearableManager.stepCount
    val batteryLevel = com.pawlife.app.data.service.BleWearableManager.batteryLevel

    val distanceMeters = routePoints.map { points ->
        var totalDistance = 0f
        if (points.size > 1) {
            for (i in 0 until points.size - 1) {
                totalDistance += points[i].distanceTo(points[i + 1])
            }
        }
        totalDistance
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val durationSeconds = combine(WalkTracker.isTracking, WalkTracker.startTime, tickerFlow()) { isTracking, startTime, tick ->
        if (isTracking && startTime != null) {
            (System.currentTimeMillis() - startTime) / 1000L
        } else {
            0L
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val paceMinPerKm = combine(distanceMeters, durationSeconds) { dist, dur ->
        if (dist > 0) {
            val distKm = dist / 1000f
            val durMin = dur / 60f
            durMin / distKm
        } else {
            0f
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    private fun tickerFlow() = flow {
        while (true) {
            emit(Unit)
            delay(1000L)
        }
    }

    fun saveWalk(petId: Long) {
        viewModelScope.launch {
            val dist = distanceMeters.value
            val pointsList = routePoints.value.map { "[${it.longitude}, ${it.latitude}]" }
            val geoJson = """{"type":"LineString","coordinates":[${pointsList.joinToString(",")}]}"""

            val log = ActivityLogEntity(
                petId = petId,
                type = "walk",
                startTime = WalkTracker.startTime.value ?: System.currentTimeMillis(),
                endTime = System.currentTimeMillis(),
                durationMinutes = (durationSeconds.value / 60).toInt(),
                distanceMeters = dist,
                caloriesBurned = (dist * 0.05).toInt(), // Roughly 50 cal per km
                routeGeoJson = if (pointsList.size > 1) geoJson else null,
                mapSnapshotUri = null,
                notes = "Completed a walk of ${"%.1f".format(dist)} meters"
            )
            activityRepository.addActivityLog(log)
        }
    }
}
