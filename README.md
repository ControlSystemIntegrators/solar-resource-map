# Solar Resource Mapper

A portable sky-survey tool that captures a fisheye photograph of the sky and
overlays a solar resource map showing the sun's path for summer solstice,
equinox, and winter solstice — corrected for your GPS location, date/time, and
camera orientation.

## Hardware

| Component | Part |
|-----------|------|
| Camera | M5Stack Timer Camera F (U082-F — OV3660 fisheye, ESP32) |
| IMU | Adafruit BNO055 breakout (RB-Ada-215) |
| Trigger | Android app (primary) · onboard button (status ping only) |

## How It Works

The camera mounts on a pole or selfie stick pointed straight up at the sky.
The operator controls everything from the Android phone on the ground.

1. Power on the camera — the LED comes on steady when ready.
2. Connect your Android phone to the **SolarMapper** WiFi AP.
3. Tap **Capture + Overlay** in the app.
4. The app calls `/snapshot` on the camera (fresh capture), reads IMU heading
   and tilt from `/imu`, takes a GPS fix from the phone, then renders three
   sun-path arcs on the fisheye image.

> **Physical button (GPIO 37):** double-blinks the LED as a "device alive"
> confirmation. It does not trigger a capture — use the app for that.

### Overlay Legend

| Colour | Sun Path |
|--------|----------|
| Yellow | Summer solstice |
| Orange | Equinox (March/September) |
| Red | Winter solstice |
| White circle | Horizon (0° altitude) |
| White rings | 30° and 60° altitude |
| Orange dot | Solar noon for each arc |

## Repository Structure

```
solar-resource-map/
├── firmware/           # ESP32 PlatformIO project
│   └── src/
│       ├── config.h        # Pin definitions, AP credentials
│       ├── main.cpp        # Entry point
│       ├── camera.*        # OV3660 capture
│       ├── imu.*           # BNO055 Euler angles
│       └── wifi_server.*   # HTTP AP + endpoints
├── android/            # Native Kotlin Android app
│   └── app/src/main/
│       └── java/com/solarresourcemap/
│           ├── MainActivity.kt
│           ├── CameraClient.kt     # HTTP → camera
│           ├── GpsManager.kt       # Fused location
│           ├── SolarCalculator.kt  # NOAA sun position
│           ├── FisheyeOverlayView.kt # Custom view
│           └── ImuData.kt
└── docs/
    ├── architecture.md
    └── wiring/
        ├── bno055.md
        └── lsm303dlhc.md
```

## Building

### Firmware

1. Install [PlatformIO](https://platformio.org/).
2. Open `firmware/` in VS Code with the PlatformIO extension.
3. Edit `firmware/src/config.h` — set a secure `AP_PASSWORD`.
4. Build and upload: `pio run -t upload`.

### Android App

1. Open `android/` in Android Studio (Ladybug or later).
2. Build → Run on a device running Android 8.0+ (API 26+).

## Configuration

Edit `firmware/src/config.h` before flashing:

```c
#define AP_SSID     "SolarMapper"
#define AP_PASSWORD "your-secure-password"
#define IMU_SDA_PIN  4    // Grove SDA on Timer Camera F
#define IMU_SCL_PIN  13   // Grove SCL on Timer Camera F
```

See `docs/wiring/bno055.md` for sensor wiring details.

## Accuracy Notes

- Solar position: NOAA simplified algorithm, < 0.01° error (1950–2050).
- Heading accuracy depends on BNO055 magnetometer calibration and local
  magnetic declination (declination correction is a planned enhancement).
- Fisheye projection assumes equidistant lens model — verify against your
  specific lens if high geometric accuracy is needed.

## License

MIT — see [LICENSE](LICENSE).
