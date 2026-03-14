# LSM303DLHC Wiring (Alternative IMU)

> **Note:** The BNO055 is the recommended IMU for this project because it
> provides onboard sensor fusion. The LSM303DLHC is a raw accelerometer +
> magnetometer combo that requires tilt-compensated heading math in firmware.
> This document is retained for reference if the BNO055 is unavailable.

## Pin Connections

| LSM303DLHC Pin | ESP32 Pin | Notes                          |
|----------------|-----------|--------------------------------|
| VIN            | 3V3       |                                |
| GND            | GND       |                                |
| SDA            | GPIO 13   | Secondary I2C bus              |
| SCL            | GPIO 14   | Secondary I2C bus              |

## I2C Addresses

The LSM303DLHC exposes two separate I2C devices:

| Device        | Address |
|---------------|---------|
| Accelerometer | 0x19    |
| Magnetometer  | 0x1E    |

## Software Notes

If using the LSM303DLHC instead of BNO055, you must implement
tilt-compensated heading in firmware:

```cpp
// Simplified tilt-compensated heading
float Bx = mag.x * cos(pitch) + mag.z * sin(pitch);
float By = mag.x * sin(roll) * sin(pitch)
         + mag.y * cos(roll)
         - mag.z * sin(roll) * cos(pitch);
float heading = atan2(-By, Bx) * 180.0 / PI;
if (heading < 0) heading += 360.0;
```

Update `imu.cpp` to use `Adafruit_LSM303_Accel_Unified` and
`Adafruit_LSM303DLH_Mag_Unified` from the Adafruit LSM303DLHC library.
