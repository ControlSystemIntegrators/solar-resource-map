#include "i2c_bus.h"
#include "config.h"
#include <Arduino.h>

TwoWire secondaryBus = TwoWire(1);   // hardware I2C peripheral 1

void secondaryBusInit() {
    secondaryBus.begin(IMU_SDA_PIN, IMU_SCL_PIN);
    Serial.printf("[I2C] Secondary bus ready — SDA:%d SCL:%d\n",
                  IMU_SDA_PIN, IMU_SCL_PIN);
}
