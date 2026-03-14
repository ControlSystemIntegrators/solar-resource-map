package com.solarresourcemap

import com.google.gson.annotations.SerializedName

/**
 * IMU reading received from the camera's /imu endpoint.
 *
 * @property heading  Magnetic heading in degrees (0–360, 0 = North).
 * @property pitch    Pitch in degrees (positive = front tilted up).
 * @property roll     Roll in degrees (positive = right side up).
 * @property valid    False if the BNO055 returned an invalid reading.
 */
data class ImuData(
    @SerializedName("heading") val heading: Float,
    @SerializedName("pitch")   val pitch:   Float,
    @SerializedName("roll")    val roll:    Float,
    @SerializedName("valid")   val valid:   Boolean
)
