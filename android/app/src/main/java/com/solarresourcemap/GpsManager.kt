package com.solarresourcemap

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Thin wrapper around FusedLocationProviderClient.
 * Requires ACCESS_FINE_LOCATION permission to be granted before calling [getLastLocation].
 */
class GpsManager(context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Returns the best available location. Requests a fresh fix if the last known
     * location is null or stale.
     *
     * Caller must ensure location permission is granted.
     */
    @SuppressLint("MissingPermission")
    suspend fun getLocation(): Location = suspendCancellableCoroutine { cont ->
        fusedClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null   // CancellationToken — null = no cancellation linked here
        ).addOnSuccessListener { location ->
            if (location != null) {
                cont.resume(location)
            } else {
                cont.resumeWithException(IllegalStateException("Location unavailable"))
            }
        }.addOnFailureListener { ex ->
            cont.resumeWithException(ex)
        }
    }
}
