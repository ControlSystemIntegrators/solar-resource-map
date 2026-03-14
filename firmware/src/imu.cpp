#include "imu.h"
#include "config.h"
#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_BNO055.h>
#include <Arduino.h>

// Use the ESP32 second I2C bus (Wire1) to avoid conflict with camera I2C
static TwoWire        imuWire = TwoWire(1);
static Adafruit_BNO055 bno(55, BNO055_ADDR, &imuWire);
static bool           initialised = false;

bool imuInit() {
    imuWire.begin(IMU_SDA_PIN, IMU_SCL_PIN);

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
