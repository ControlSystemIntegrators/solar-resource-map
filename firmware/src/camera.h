#pragma once
#include <esp_camera.h>
#include <stdbool.h>

/**
 * Initialise the OV3660 camera.
 * Must be called once in setup() before captureFrame().
 * @return true on success.
 */
bool cameraInit();

/**
 * Capture a single JPEG frame and return the camera frame buffer.
 * Caller MUST call esp_camera_fb_return(fb) after use.
 * @return pointer to frame buffer, or nullptr on failure.
 */
camera_fb_t* captureFrame();
