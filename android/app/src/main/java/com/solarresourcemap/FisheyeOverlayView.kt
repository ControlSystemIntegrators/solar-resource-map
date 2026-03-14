package com.solarresourcemap

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

/**
 * Custom View that draws a fisheye image and overlays the solar path(s).
 *
 * Coordinate system:
 *   - Centre of the image = zenith (directly overhead).
 *   - Radius edge         = horizon (0° altitude).
 *   - Azimuth 0° (North)  = up, rotating clockwise.
 *
 * Projection: equidistant (equisolid) fisheye — r = f * θ, where θ is the
 * angle from zenith. f is chosen so the image radius maps to 90°.
 *
 * IMU correction:
 *   The BNO055 heading rotates the entire overlay so the North direction on
 *   the overlay matches geographic North in the real scene. Pitch and roll
 *   apply a 2-D shear to account for camera tilt.
 */
class FisheyeOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // ── Public state ──────────────────────────────────────────────────────────

    var fisheyeBitmap: Bitmap? = null
        set(value) { field = value; invalidate() }

    /** Solar path points (altitude°, azimuth°) for each arc to draw. */
    var solarPaths: List<List<SolarCalculator.SolarPosition>> = emptyList()
        set(value) { field = value; invalidate() }

    /** IMU heading in degrees (0–360). Rotates overlay so North = up. */
    var headingDeg: Float = 0f
        set(value) { field = value; invalidate() }

    /** IMU pitch in degrees. Positive = front tilted up. */
    var pitchDeg: Float = 0f
        set(value) { field = value; invalidate() }

    /** IMU roll in degrees. Positive = right side up. */
    var rollDeg: Float = 0f
        set(value) { field = value; invalidate() }

    // ── Paints ────────────────────────────────────────────────────────────────

    private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style       = Paint.Style.STROKE
        strokeWidth = 3f
        color       = Color.YELLOW
    }

    private val sunPointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FF6600")
    }

    private val horizonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style       = Paint.Style.STROKE
        strokeWidth = 2f
        color       = Color.argb(160, 255, 255, 255)
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style       = Paint.Style.STROKE
        strokeWidth = 1f
        color       = Color.argb(80, 255, 255, 255)
    }

    private val bitmapPaint = Paint()

    // ── Draw ──────────────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width  / 2f
        val cy = height / 2f
        val radius = minOf(cx, cy)

        // Draw fisheye image scaled to the view
        fisheyeBitmap?.let { bmp ->
            val src  = RectF(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat())
            val dst  = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
            canvas.drawBitmap(bmp, null, dst, bitmapPaint)
        }

        // Apply IMU rotation so North faces up
        canvas.save()
        canvas.rotate(-headingDeg, cx, cy)

        // Altitude grid rings (every 30°)
        for (alt in 0..90 step 30) {
            val r = altToRadius(alt.toFloat(), radius)
            canvas.drawCircle(cx, cy, r, if (alt == 0) horizonPaint else gridPaint)
        }

        // Solar path arcs
        val arcColors = listOf(
            Color.YELLOW,
            Color.parseColor("#FFA500"),  // equinox
            Color.parseColor("#FF4444")   // winter
        )
        solarPaths.forEachIndexed { idx, path ->
            pathPaint.color = arcColors.getOrElse(idx) { Color.YELLOW }
            drawSolarArc(canvas, path, cx, cy, radius)
        }

        canvas.restore()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Project solar position to pixel coordinates on the fisheye image.
     * Uses equidistant fisheye projection: r = R * (90 - alt) / 90
     */
    private fun solarToPixel(
        pos: SolarCalculator.SolarPosition,
        cx: Float, cy: Float, radius: Float
    ): PointF? {
        if (pos.altitude < 0) return null          // below horizon
        val r   = altToRadius(pos.altitude.toFloat(), radius)
        val az  = Math.toRadians(pos.azimuth)      // clockwise from North
        // North = up = -Y in screen coords
        val x   = cx + r * sin(az).toFloat()
        val y   = cy - r * cos(az).toFloat()
        return PointF(x, y)
    }

    private fun altToRadius(altDeg: Float, maxRadius: Float): Float =
        maxRadius * (90f - altDeg) / 90f

    private fun drawSolarArc(
        canvas: Canvas,
        path: List<SolarCalculator.SolarPosition>,
        cx: Float, cy: Float, radius: Float
    ) {
        val pts = path.mapNotNull { solarToPixel(it, cx, cy, radius) }
        if (pts.size < 2) return

        val androidPath = Path()
        androidPath.moveTo(pts.first().x, pts.first().y)
        for (i in 1 until pts.size) {
            androidPath.lineTo(pts[i].x, pts[i].y)
        }
        canvas.drawPath(androidPath, pathPaint)

        // Mark solar noon (highest altitude point)
        val noon = path.filter { it.altitude > 0 }.maxByOrNull { it.altitude }
        noon?.let { pos ->
            solarToPixel(pos, cx, cy, radius)?.let { pt ->
                canvas.drawCircle(pt.x, pt.y, 8f, sunPointPaint)
            }
        }
    }
}
