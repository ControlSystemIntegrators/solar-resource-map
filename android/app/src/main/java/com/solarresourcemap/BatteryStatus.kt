package com.solarresourcemap

import com.google.gson.annotations.SerializedName

/**
 * Battery status received from the camera's /battery endpoint.
 *
 * @property percent   Approximate charge level (0, 25, 50, 75, or 100).
 *                     -1 if the IP5306 could not be read.
 * @property charging  True while the camera is connected to USB power.
 * @property valid     False if the PMU was not found or the read failed.
 */
data class BatteryStatus(
    @SerializedName("percent")  val percent:  Int,
    @SerializedName("charging") val charging: Boolean,
    @SerializedName("valid")    val valid:    Boolean
)
