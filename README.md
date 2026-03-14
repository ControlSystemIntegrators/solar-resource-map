# Solar Resource Mapper

A portable sky-survey tool that captures a fisheye photograph of the sky and
overlays a solar resource map showing the sun's path for summer solstice,
equinox, and winter solstice — corrected for your GPS location, date/time, and
camera orientation.

## Hardware

| Component | Part |
|-----------|------|
| Camera | M5Stack Timer Camera (OV3660 fisheye, ESP32) |
| IMU | Bosch BNO055 (9-DOF, onboard sensor fusion) |
| Trigger | Built-in button (GPIO 37) |

## How It Works

1. Mount the camera pointing straight up (fisheye lens facing the sky).
2. Connect your Android phone to the **SolarMapper** WiFi network.
3. Press the button on the camera (or tap **Capture** in the app).
4. The app fetches the JPEG image and IMU heading/tilt from the camera, reads
   GPS from the phone, and renders three sun-path arcs on the fisheye image.

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
#define IMU_SDA_PIN  13
#define IMU_SCL_PIN  14
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
