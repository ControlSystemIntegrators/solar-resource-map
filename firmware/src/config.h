#pragma once

// ── WiFi Access Point ────────────────────────────────────────────────────────
#define AP_SSID     "SolarMapper"
#define AP_PASSWORD "solarmap123"   // min 8 chars; change before shipping
#define AP_CHANNEL  1
#define AP_HIDDEN   false

// ── HTTP Server ───────────────────────────────────────────────────────────────
#define HTTP_PORT   80

// ── Camera pins (M5Stack Timer Camera F — U082-F) ────────────────────────────
#define CAM_PIN_PWDN    -1
#define CAM_PIN_RESET   15
#define CAM_PIN_XCLK     27
#define CAM_PIN_SIOD    25   // SDA
#define CAM_PIN_SIOC    23   // SCL
#define CAM_PIN_D7      19
#define CAM_PIN_D6      36
#define CAM_PIN_D5      18
#define CAM_PIN_D4      39
#define CAM_PIN_D3       5
#define CAM_PIN_D2      34
#define CAM_PIN_D1      35
#define CAM_PIN_D0      32
#define CAM_PIN_VSYNC   22
#define CAM_PIN_HREF    26
#define CAM_PIN_PCLK    21

// ── IMU (BNO055) I2C ─────────────────────────────────────────────────────────
// Adafruit BNO055 breakout (RB-Ada-215) wired to the Timer Camera F Grove port.
// Grove provides 5 V power; the Adafruit breakout accepts 3.3–5 V on VIN.
// The Grove I2C pins are on Wire1 (secondary bus) — no conflict with the
// camera's internal I2C bus (Wire0, GPIO 25/23).
#define IMU_SDA_PIN  4       // Grove SDA on Timer Camera F
#define IMU_SCL_PIN  13      // Grove SCL on Timer Camera F
#define BNO055_ADDR  0x28    // ADR pin → GND; use 0x29 if ADR → 3V3

// ── Button ────────────────────────────────────────────────────────────────────
// Secondary input only — primary capture is triggered by the Android app.
#define BUTTON_PIN  37      // Timer Camera F side button (active LOW)

// ── Status LED ────────────────────────────────────────────────────────────────
#define LED_PIN     2       // Timer Camera F onboard LED (active HIGH)
#define BUTTON_ACTIVE_LOW true

// ── Image ─────────────────────────────────────────────────────────────────────
// FRAMESIZE_UXGA (1600×1200) for best quality; reduce if memory-constrained
#define IMAGE_FRAMESIZE FRAMESIZE_UXGA
#define IMAGE_QUALITY   10  // 0 (best) – 63 (worst) JPEG quality
