#include <Arduino.h>
#include "config.h"
#include "camera.h"
#include "imu.h"
#include "wifi_server.h"

// ── LED helpers ───────────────────────────────────────────────────────────────

static void ledOn()  { digitalWrite(LED_PIN, HIGH); }
static void ledOff() { digitalWrite(LED_PIN, LOW);  }

/** Brief double-blink to signal a status event (e.g. button press). */
static void ledBlink(int times = 2, uint32_t periodMs = 100) {
    for (int i = 0; i < times; i++) {
        ledOn();  delay(periodMs);
        ledOff(); delay(periodMs);
    }
}

// ── Button debounce ───────────────────────────────────────────────────────────
// The physical button is a secondary indicator only.
// Primary capture is triggered by the Android app via GET /snapshot.

static bool     lastButtonState = HIGH;   // INPUT_PULLUP → idle HIGH
static uint32_t lastDebounceMs  = 0;
static const uint32_t DEBOUNCE_MS = 50;

static bool buttonPressed() {
    bool reading = digitalRead(BUTTON_PIN);
    if (reading != lastButtonState) {
        lastDebounceMs  = millis();
        lastButtonState = reading;
    }
    if ((millis() - lastDebounceMs) > DEBOUNCE_MS && reading == LOW) {
        lastButtonState = HIGH;   // consume press
        return true;
    }
    return false;
}

// ── Setup ─────────────────────────────────────────────────────────────────────

void setup() {
    Serial.begin(115200);
    delay(200);
    Serial.println("\n=== Solar Resource Mapper ===");

    pinMode(LED_PIN,    OUTPUT);
    pinMode(BUTTON_PIN, INPUT_PULLUP);
    ledOff();

    if (!cameraInit()) {
        Serial.println("[FATAL] Camera init failed — halting");
        // Rapid blink to indicate fatal error
        while (true) { ledBlink(5, 50); delay(500); }
    }

    if (!imuInit()) {
        Serial.println("[WARN] IMU not available — headings will be invalid");
        ledBlink(3, 200);   // triple-blink: IMU warning
    }

    wifiServerBegin();

    // Steady ON = ready; app can connect and issue /snapshot at any time
    ledOn();
    Serial.println("[Main] Ready — connect to '" AP_SSID "' and use the app");
}

// ── Loop ──────────────────────────────────────────────────────────────────────

void loop() {
    wifiServerHandleClient();

    // Physical button: blink LED as a "I'm alive" ping.
    // Does not directly trigger capture — that comes from the Android app.
    if (buttonPressed()) {
        Serial.println("[Main] Button pressed — device alive, use app to capture");
        ledBlink(2, 80);
        ledOn();   // restore steady-on state
    }
}
