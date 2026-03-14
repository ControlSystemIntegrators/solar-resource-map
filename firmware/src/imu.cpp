#include "imu.h"
#include "i2c_bus.h"
#include "config.h"
#include <Adafruit_Sensor.h>
#include <Adafruit_BNO055.h>
#include <Arduino.h>

// BNO055 on the shared secondary I2C bus (GPIO 4/13)
static Adafruit_BNO055 bno(55, BNO055_ADDR, &secondaryBus);
static bool            initialised = false;

bool imuInit() {
    // secondaryBus is initialised by secondaryBusInit() in main setup()
    if (!bno.begin()) {
        Serial.println("[IMU] BNO055 not found — check wiring & address");
        return false;
    }

    bno.setExtCrystalUse(true);   // better accuracy with external crystal
    delay(100);
    initialised = true;
    Serial.println("[IMU] BNO055 ready");
    return true;
}

ImuReading imuRead() {
    ImuReading r = {0, 0, 0, false};
    if (!initialised) return r;

    sensors_event_t event;
    bno.getEvent(&event, Adafruit_BNO055::VECTOR_EULER);

    // BNO055 Euler output: x = heading, y = roll, z = pitch
    r.heading = event.orientation.x;
    r.roll    = event.orientation.y;
    r.pitch   = event.orientation.z;
    r.valid   = true;
    return r;
}
