#pragma once
#include <stdint.h>

struct BatteryStatus {
    /**
     * Approximate charge level in percent.
     * The IP5306 reports in 4 steps so values will be 0, 25, 50, 75, or 100.
     * Returns -1 if the PMU could not be read.
     */
    int     percent;
    bool    charging;   // true while connected to USB power
    bool    valid;      // false if IP5306 not found or read failed
};

/**
 * Initialise contact with the IP5306 PMU over the shared secondary I2C bus.
 * secondaryBusInit() must be called before batteryInit().
 * @return true if the IP5306 responded.
 */
bool batteryInit();

/** Read the current battery status from the IP5306. */
BatteryStatus batteryRead();
