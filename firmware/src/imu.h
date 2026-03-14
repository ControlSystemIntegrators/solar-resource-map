#pragma once
#include <stdbool.h>

struct ImuReading {
    float heading;   // magnetic heading, degrees (0–360, 0 = North)
    float pitch;     // degrees, positive = front up
    float roll;      // degrees, positive = right side up
    bool  valid;     // false if sensor not initialised or read failed
};

/**
 * Initialise the BNO055 over the secondary I2C bus.
 * @return true on success.
 */
bool imuInit();

/**
 * Read Euler angles from the BNO055 onboard fusion output.
 */
ImuReading imuRead();
