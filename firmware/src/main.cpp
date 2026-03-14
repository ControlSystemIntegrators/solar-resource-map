#include <Arduino.h>
#include "config.h"
#include "camera.h"
#include "imu.h"
#include "wifi_server.h"

// ── Button debounce state ─────────────────────────────────────────────────────
static bool     lastButtonState = !BUTTON_ACTIVE_LOW;
static uint32_t lastDebounceMs  = 0;
static const uint32_t DEBOUNCE_MS = 50;

static bool buttonPressed() {
    bool reading = (digitalRead(BUTTON_PIN) == LOW) == BUTTON_ACTIVE_LOW;
    if (reading != lastButtonState) {
        lastDebounceMs = millis();
        lastButtonState = reading;
    }
    if ((millis() - lastDebounceMs) > DEBOUNCE_MS && reading) {
        lastButtonState = !reading;  // consume press
        return true;
    }
    return false;
}

// ── Setup ─────────────────────────────────────────────────────────────────────
void setup() {
    Serial.begin(115200);
    delay(200);
    Serial.println("\n=== Solar Resource Mapper ===");

    pinMode(BUTTON_PIN, INPUT_PULLUP);

    if (!cameraInit()) {
        Serial.println("[FATAL] Camera init failed — halting");
        while (true) delay(1000);
    }

    if (!imuInit()) {
        Serial.println("[WARN] IMU not available — headings will be invalid");
    }

    wifiServerBegin();
    Serial.println("[Main] Ready — waiting for connections");
}

// ── Loop ──────────────────────────────────────────────────────────────────────
void loop() {
    // HTTP server polling is handled here
    // (WebServer is not async on plain Arduino framework)
    extern WebServer server;  // declared in wifi_server.cpp — or refactor to expose handleClient()

    if (buttonPressed()) {
        // Button press: blink confirmation; image will be served on next /snapshot request
        Serial.println("[Main] Button pressed — snapshot ready on /snapshot");
    }

    delay(1);
}
