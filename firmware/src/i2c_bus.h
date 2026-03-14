#pragma once
#include <Wire.h>

/**
 * Shared secondary I2C bus (Wire1) on GPIO 4 (SDA) / GPIO 13 (SCL).
 *
 * Both the BNO055 IMU and the IP5306 PMU live on this bus:
 *   BNO055  → 0x28
 *   IP5306  → 0x75
 *
 * Call secondaryBusInit() once in setup() before using either device.
 */
extern TwoWire secondaryBus;

void secondaryBusInit();
