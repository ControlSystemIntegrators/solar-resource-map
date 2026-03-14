# Architecture

## System Overview

```
┌─────────────────────────────────┐        WiFi AP (192.168.4.1)
│   M5Stack Timer Camera          │◄──────────────────────────────┐
│                                 │                               │
│  OV3660 fisheye ──► ESP32       │  GET /snapshot  → JPEG        │
│  BNO055 IMU ───────► ESP32      │  GET /imu       → JSON        │
│  Button ───────────► ESP32      │  GET /status    → JSON        │
└─────────────────────────────────┘                               │
                                                                   │
┌─────────────────────────────────┐                               │
│   Android Phone                 │───────────────────────────────┘
│                                 │
│  GpsManager   → lat/lon         │
│  CameraClient → bitmap + IMU    │
│  SolarCalculator → sun paths    │
│  FisheyeOverlayView → renders   │
└─────────────────────────────────┘
```

## Data Flow

1. User connects phone to **SolarMapper** WiFi AP.
2. User taps **Capture + Overlay** in the Android app.
3. App simultaneously:
   - Fetches JPEG from `/snapshot`
   - Fetches IMU Euler angles from `/imu`
   - Requests GPS fix from the phone's fused location provider
4. `SolarCalculator` computes three solar-path arcs (summer solstice, equinox,
   winter solstice) at 15-minute intervals for the GPS location.
5. `FisheyeOverlayView` renders:
   - Fisheye JPEG as background
   - Altitude grid rings (0°/30°/60°/90°)
   - Three sun-path arcs projected using equidistant fisheye math
   - Solar-noon markers per arc
   - IMU heading rotates the overlay so North = up in the real scene

## Camera HTTP API

| Endpoint    | Method | Response         | Description                     |
|-------------|--------|------------------|---------------------------------|
| `/snapshot` | GET    | `image/jpeg`     | Latest captured frame           |
| `/imu`      | GET    | `application/json` | `{heading, pitch, roll, valid}` |
| `/status`   | GET    | `application/json` | Heap, PSRAM, uptime             |

## Fisheye Projection

The OV3660 with fisheye lens follows an **equidistant** projection:

```
r = f × θ
```

where `θ` is the angle from the optical axis (zenith) and `f` is chosen so that
`r = image_radius` when `θ = 90°` (horizon).

For overlay math we use:

```
r_pixel = R × (90° − altitude) / 90°
x_pixel = cx + r × sin(azimuth)
y_pixel = cy − r × cos(azimuth)    // −Y because screen Y grows downward
```

The IMU heading rotates the canvas before drawing so the overlay's North
direction aligns with the real-world magnetic North (declination correction
to be added in a future iteration).

## IMU Correction

The BNO055 outputs fused Euler angles via `VECTOR_EULER`:
- **x** → heading (0–360°, 0 = North)
- **y** → roll
- **z** → pitch

The Android overlay rotates by `-heading` so North is at the top.
Pitch and roll compensation (tilt correction of the sun path) is a planned
enhancement — the current version assumes the camera is mounted roughly level.
