package com.solarresourcemap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * HTTP client that communicates with the SolarMapper camera access point.
 *
 * Base URL defaults to the ESP32 AP gateway address (192.168.4.1).
 */
class CameraClient(baseUrl: String = "http://192.168.4.1") {

    private val base = baseUrl.trimEnd('/')
    private val gson = Gson()

    private val http = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /** Fetch the latest JPEG snapshot from the camera. */
    suspend fun fetchSnapshot(): Result<Bitmap> = withContext(Dispatchers.IO) {
        runCatching {
            val response = http.newCall(Request.Builder().url("$base/snapshot").build()).execute()
            check(response.isSuccessful) { "Snapshot HTTP ${response.code}" }
            val bytes = response.body!!.bytes()
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: error("Failed to decode JPEG")
        }
    }

    /** Fetch IMU Euler angles from the camera. */
    suspend fun fetchImu(): Result<ImuData> = withContext(Dispatchers.IO) {
        runCatching {
            val response = http.newCall(Request.Builder().url("$base/imu").build()).execute()
            check(response.isSuccessful) { "IMU HTTP ${response.code}" }
            gson.fromJson(response.body!!.string(), ImuData::class.java)
        }
    }

    /**
     * Fetch snapshot and IMU data together.
     * Returns a pair only if both succeed.
     */
    suspend fun fetchCapture(): Result<Pair<Bitmap, ImuData>> {
        val bmp = fetchSnapshot().getOrElse { return Result.failure(it) }
        val imu = fetchImu().getOrElse { return Result.failure(it) }
        return Result.success(Pair(bmp, imu))
    }
}
