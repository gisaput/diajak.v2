package com.example.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

data class DeviceLocationResult(
    val latitude: Double,
    val longitude: Double,
    val nearestCityName: String,
    val fromGps: Boolean
)

class LocationService(private val context: Context) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context.applicationContext)

    fun hasLocationPermission(): Boolean {
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return coarseGranted || fineGranted
    }

    @SuppressLint("MissingPermission")
    suspend fun detectCurrentLocation(): DeviceLocationResult? {
        if (!hasLocationPermission()) return null
        return try {
            val cancellationTokenSource = CancellationTokenSource()
            val freshLocation = withTimeoutOrNull(4_500L) {
                try {
                    fusedClient.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        cancellationTokenSource.token
                    ).await()
                } catch (_: Exception) {
                    null
                }
            }
            if (freshLocation == null) {
                cancellationTokenSource.cancel()
            }
            val location = freshLocation ?: withTimeoutOrNull(2_500L) {
                try {
                    fusedClient.lastLocation.await()
                } catch (_: Exception) {
                    null
                }
            }
            if (location != null) {
                val nearest = DiajakCatalog.findNearestCity(location.latitude, location.longitude)
                DeviceLocationResult(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    nearestCityName = nearest.name,
                    fromGps = true
                )
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
