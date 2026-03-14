package com.solarresourcemap

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.solarresourcemap.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val client = CameraClient()
    private lateinit var gpsManager: GpsManager

    // ── Permission launcher ───────────────────────────────────────────────────

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            onPermissionsGranted()
        } else {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_LONG).show()
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gpsManager = GpsManager(this)

        binding.btnCapture.setOnClickListener { onCaptureClicked() }

        checkPermissions()
        startBatteryPolling()
    }

    // ── Permission handling ───────────────────────────────────────────────────

    private fun checkPermissions() {
        val needed = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val missing = needed.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) onPermissionsGranted() else permissionLauncher.launch(needed)
    }

    private fun onPermissionsGranted() {
        binding.btnCapture.isEnabled = true
    }

    // ── Battery polling ───────────────────────────────────────────────────────

    private fun startBatteryPolling() {
        lifecycleScope.launch {
            while (isActive) {
                val result = client.fetchBattery()
                result.onSuccess { batt -> updateBatteryUi(batt) }
                // Poll every 30 seconds; don't show errors — camera may not be connected yet
                delay(30_000L)
            }
        }
    }

    private fun updateBatteryUi(batt: BatteryStatus) {
        if (!batt.valid) {
            binding.batteryText.text = "Battery: unavailable"
            binding.batteryText.setTextColor(Color.parseColor("#AAAAAA"))
            return
        }
        val chargingLabel = if (batt.charging) " ⚡" else ""
        binding.batteryText.text = "Battery: ${batt.percent}%$chargingLabel"
        binding.batteryText.setTextColor(
            when {
                batt.charging      -> Color.parseColor("#4CAF50")  // green while charging
                batt.percent <= 25 -> Color.parseColor("#F44336")  // red — low
                batt.percent <= 50 -> Color.parseColor("#FF9800")  // orange — moderate
                else               -> Color.parseColor("#4CAF50")  // green — good
            }
        )
        if (batt.percent <= 25 && !batt.charging) {
            Toast.makeText(this, "Camera battery low (${batt.percent}%)", Toast.LENGTH_LONG).show()
        }
    }

    // ── Capture flow ──────────────────────────────────────────────────────────

    private fun onCaptureClicked() {
        binding.btnCapture.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE
        binding.statusText.text = "Connecting to camera…"

        lifecycleScope.launch {
            // 1. Fetch image + IMU from camera
            val captureResult = client.fetchCapture()
            if (captureResult.isFailure) {
                showError("Camera error: ${captureResult.exceptionOrNull()?.message}")
                return@launch
            }
            val (bitmap, imu) = captureResult.getOrThrow()

            // 2. Get phone GPS
            binding.statusText.text = "Getting GPS location…"
            val location = runCatching { gpsManager.getLocation() }.getOrElse {
                showError("GPS error: ${it.message}")
                return@launch
            }

            // 3. Calculate solar paths (summer solstice, equinox, winter solstice)
            binding.statusText.text = "Computing solar paths…"
            val now = Calendar.getInstance()
            val paths = listOf(
                summerSolsticeCalendar(now),
                equinoxCalendar(now),
                winterSolsticeCalendar(now)
            ).map { cal ->
                SolarCalculator.dailySunPath(location.latitude, location.longitude, cal)
            }

            // 4. Update the overlay view
            with(binding.fisheyeOverlay) {
                fisheyeBitmap = bitmap
                solarPaths    = paths
                headingDeg    = imu.heading
                pitchDeg      = imu.pitch
                rollDeg       = imu.roll
            }

            binding.statusText.text =
                "Lat: %.4f  Lon: %.4f  Heading: %.1f°".format(
                    location.latitude, location.longitude, imu.heading
                )
            binding.progressBar.visibility = View.GONE
            binding.btnCapture.isEnabled   = true
        }
    }

    private fun showError(msg: String?) {
        runOnUiThread {
            binding.progressBar.visibility = View.GONE
            binding.btnCapture.isEnabled   = true
            binding.statusText.text        = "Error: $msg"
            Toast.makeText(this, msg ?: "Unknown error", Toast.LENGTH_LONG).show()
        }
    }

    // ── Calendar helpers ──────────────────────────────────────────────────────

    private fun summerSolsticeCalendar(ref: Calendar) = (ref.clone() as Calendar).apply {
        set(Calendar.MONTH, Calendar.JUNE); set(Calendar.DAY_OF_MONTH, 21)
    }

    private fun equinoxCalendar(ref: Calendar) = (ref.clone() as Calendar).apply {
        set(Calendar.MONTH, Calendar.MARCH); set(Calendar.DAY_OF_MONTH, 21)
    }

    private fun winterSolsticeCalendar(ref: Calendar) = (ref.clone() as Calendar).apply {
        set(Calendar.MONTH, Calendar.DECEMBER); set(Calendar.DAY_OF_MONTH, 21)
    }
}
