#include "wifi_server.h"
#include "camera.h"
#include "imu.h"
#include "config.h"
#include <WiFi.h>
#include <WebServer.h>
#include <ArduinoJson.h>
#include <Arduino.h>

static WebServer server(HTTP_PORT);

// ── Helpers ───────────────────────────────────────────────────────────────────

static void setCORSHeaders() {
    server.sendHeader("Access-Control-Allow-Origin", "*");
    server.sendHeader("Cache-Control", "no-cache");
}

// ── Route handlers ────────────────────────────────────────────────────────────

static void handleSnapshot() {
    setCORSHeaders();
    camera_fb_t* fb = captureFrame();
    if (!fb) {
        server.send(503, "text/plain", "Capture failed");
        return;
    }
    server.sendHeader("Content-Disposition", "inline; filename=\"snapshot.jpg\"");
    server.send_P(200, "image/jpeg",
                  reinterpret_cast<const char*>(fb->buf),
                  fb->len);
    esp_camera_fb_return(fb);
}

static void handleImu() {
    setCORSHeaders();
    ImuReading r = imuRead();
    JsonDocument doc;
    doc["heading"] = r.heading;
    doc["pitch"]   = r.pitch;
    doc["roll"]    = r.roll;
    doc["valid"]   = r.valid;
    String body;
    serializeJson(doc, body);
    server.send(200, "application/json", body);
}

static void handleStatus() {
    setCORSHeaders();
    JsonDocument doc;
    doc["device"]  = "SolarMapper";
    doc["heap"]    = ESP.getFreeHeap();
    doc["psram"]   = ESP.getFreePsram();
    doc["uptime"]  = millis();
    String body;
    serializeJson(doc, body);
    server.send(200, "application/json", body);
}

static void handleNotFound() {
    server.send(404, "text/plain", "Not found");
}

// ── Public entry point ────────────────────────────────────────────────────────

void wifiServerBegin() {
    WiFi.softAP(AP_SSID, AP_PASSWORD, AP_CHANNEL, AP_HIDDEN);
    Serial.printf("[WiFi] AP started — SSID: %s  IP: %s\n",
                  AP_SSID,
                  WiFi.softAPIP().toString().c_str());

    server.on("/snapshot", HTTP_GET, handleSnapshot);
    server.on("/imu",      HTTP_GET, handleImu);
    server.on("/status",   HTTP_GET, handleStatus);
    server.onNotFound(handleNotFound);
    server.begin();
    Serial.printf("[HTTP] Server on port %d\n", HTTP_PORT);
}
