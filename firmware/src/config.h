#pragma once

// ── WiFi Access Point ────────────────────────────────────────────────────────
#define AP_SSID     "SolarMapper"
#define AP_PASSWORD "solarmap123"   // min 8 chars; change before shipping
#define AP_CHANNEL  1
#define AP_HIDDEN   false

// ── HTTP Server ───────────────────────────────────────────────────────────────
#define HTTP_PORT   80

// ── Camera pins (M5Stack Timer Camera) ───────────────────────────────────────
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
// Uses the ESP32's second I2C bus so it doesn't conflict with camera I2C
#define IMU_SDA_PIN  13
#define IMU_SCL_PIN  14
#define BNO055_ADDR  0x28   // ALT: 0x29 if ADR pin is HIGH

// ── Button ────────────────────────────────────────────────────────────────────
#define BUTTON_PIN  37      // M5Stack Timer Camera built-in button
#define BUTTON_ACTIVE_LOW true

// ── Image ─────────────────────────────────────────────────────────────────────
// FRAMESIZE_UXGA (1600×1200) for best quality; reduce if memory-constrained
#define IMAGE_FRAMESIZE FRAMESIZE_UXGA
#define IMAGE_QUALITY   10  // 0 (best) – 63 (worst) JPEG quality
