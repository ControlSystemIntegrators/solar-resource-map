package com.solarresourcemap

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

/**
 * Solar position calculator based on the NOAA simplified algorithm.
 * Accuracy: < 0.01° for dates between 1950–2050.
 *
 * All angular inputs/outputs are in degrees.
 */
object SolarCalculator {

    data class SolarPosition(
        /** Altitude above horizon (degrees). Negative = below horizon. */
        val altitude: Double,
        /** True azimuth from North, clockwise (degrees, 0–360). */
        val azimuth: Double
    )

    /**
     * Calculate solar position for a given location and time.
     *
     * @param latDeg   Observer latitude  (degrees, N positive)
     * @param lonDeg   Observer longitude (degrees, E positive)
     * @param cal      Calendar instance representing the local date/time
     */
    fun calculate(latDeg: Double, lonDeg: Double, cal: Calendar): SolarPosition {
        val jd = toJulianDay(cal)
        val n  = jd - 2451545.0   // days from J2000.0

        // Mean longitude and mean anomaly (degrees)
        val L = (280.46 + 0.9856474 * n) mod 360.0
        val g = toRad((357.528 + 0.9856003 * n) mod 360.0)

        // Ecliptic longitude
        val lambdaDeg = L + 1.915 * sin(g) + 0.020 * sin(2 * g)
        val lambda    = toRad(lambdaDeg)

        // Obliquity of ecliptic
        val epsilon = toRad(23.439 - 0.0000004 * n)

        // Right ascension and declination
        val sinDec = sin(epsilon) * sin(lambda)
        val decRad = asin(sinDec)

        val raNumerator   = cos(lambda)
        val raDenominator = cos(epsilon) * sin(lambda)
        var raHours = toDeg(atan2(raNumerator, -raDenominator)) / 15.0
        if (raHours < 0) raHours += 24.0

        // Greenwich Mean Sidereal Time (hours)
        val gmst = (6.697375 + 0.0657098242 * n + utHours(cal)) mod 24.0

        // Local Hour Angle (degrees)
        val lst = (gmst + lonDeg / 15.0) mod 24.0
        var ha  = (lst - raHours) * 15.0
        if (ha < -180) ha += 360.0
        if (ha >  180) ha -= 360.0
        val haRad = toRad(ha)

        val latRad = toRad(latDeg)

        // Altitude
        val sinAlt = sin(latRad) * sin(decRad) + cos(latRad) * cos(decRad) * cos(haRad)
        val altRad  = asin(sinAlt)

        // Azimuth (from North, clockwise)
        val cosAz = (sin(decRad) - sin(altRad) * sin(latRad)) / (cos(altRad) * cos(latRad))
        var az = toDeg(acos(cosAz.coerceIn(-1.0, 1.0)))
        if (ha > 0) az = 360.0 - az

        return SolarPosition(altitude = toDeg(altRad), azimuth = az)
    }

    /**
     * Generate a full sun-path for a given day at [intervalMinutes] resolution.
     * Useful for rendering the annual sun-path arcs on the fisheye overlay.
     */
    fun dailySunPath(
        latDeg: Double,
        lonDeg: Double,
        cal: Calendar,
        intervalMinutes: Int = 15
    ): List<SolarPosition> {
        val day = cal.clone() as Calendar
        day.set(Calendar.HOUR_OF_DAY, 0)
        day.set(Calendar.MINUTE, 0)
        day.set(Calendar.SECOND, 0)
        val result = mutableListOf<SolarPosition>()
        repeat(24 * 60 / intervalMinutes) { i ->
            day.add(Calendar.MINUTE, intervalMinutes)
            result.add(calculate(latDeg, lonDeg, day))
        }
        return result
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun toRad(deg: Double) = deg * PI / 180.0
    private fun toDeg(rad: Double) = rad * 180.0 / PI
    private infix fun Double.mod(m: Double) = ((this % m) + m) % m

    private fun utHours(cal: Calendar): Double {
        val utc = cal.clone() as Calendar
        utc.timeZone = TimeZone.getTimeZone("UTC")
        return utc.get(Calendar.HOUR_OF_DAY) +
               utc.get(Calendar.MINUTE) / 60.0 +
               utc.get(Calendar.SECOND) / 3600.0
    }

    /** Julian Day Number from a Calendar instance. */
    private fun toJulianDay(cal: Calendar): Double {
        val utc = cal.clone() as Calendar
        utc.timeZone = TimeZone.getTimeZone("UTC")
        var y  = utc.get(Calendar.YEAR)
        var m  = utc.get(Calendar.MONTH) + 1
        val d  = utc.get(Calendar.DAY_OF_MONTH)
        val hr = utc.get(Calendar.HOUR_OF_DAY) +
                 utc.get(Calendar.MINUTE) / 60.0 +
                 utc.get(Calendar.SECOND) / 3600.0
        if (m <= 2) { y--; m += 12 }
        val a = y / 100
        val b = 2 - a + a / 4
        return floor(365.25 * (y + 4716)) +
               floor(30.6001 * (m + 1)) +
               d + hr / 24.0 + b - 1524.5
    }
}
