#pragma once

/**
 * Start the WiFi Access Point and HTTP server.
 *
 * Endpoints:
 *   GET /snapshot  – returns the latest JPEG image (image/jpeg)
 *   GET /imu       – returns IMU Euler angles as JSON
 *   GET /status    – returns device status JSON
 *
 * Call wifiServerBegin() once in setup().
 * No loop handler needed — WebServer runs on background tasks.
 */
void wifiServerBegin();
