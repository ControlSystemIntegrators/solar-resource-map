#pragma once

/**
 * Start the WiFi Access Point and HTTP server.
 *
 * Endpoints:
 *   GET /snapshot  – captures and returns a fresh JPEG (image/jpeg)
 *   GET /imu       – returns BNO055 Euler angles as JSON
 *   GET /status    – returns device status JSON
 *
 * Primary capture flow: Android app calls GET /snapshot.
 * The physical button on the device is a secondary indicator only.
 *
 * Call wifiServerBegin() once in setup(), then call
 * wifiServerHandleClient() on every loop() iteration.
 */
void wifiServerBegin();

/** Must be called on every loop() iteration to service HTTP requests. */
void wifiServerHandleClient();
