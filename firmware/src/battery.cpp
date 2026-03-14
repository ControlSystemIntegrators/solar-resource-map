#include "battery.h"
#include "i2c_bus.h"
#include <Arduino.h>

// IP5306 I2C address (fixed by hardware)
static constexpr uint8_t IP5306_ADDR      = 0x75;

// Registers
static constexpr uint8_t REG_SYS_CTL0     = 0x00;  // charge enable etc.
static constexpr uint8_t REG_CHARGER_CTL0 = 0x20;  // charging state
static constexpr uint8_t REG_BATT_PCT     = 0x78;  // battery LED indicator

static bool initialised = false;

// ── Helpers ───────────────────────────────────────────────────────────────────

static uint8_t readReg(uint8_t reg) {
    secondaryBus.beginTransmission(IP5306_ADDR);
    secondaryBus.write(reg);
    if (secondaryBus.endTransmission(false) != 0) return 0xFF;
    secondaryBus.requestFrom(static_cast<uint8_t>(IP5306_ADDR),
                             static_cast<uint8_t>(1));
    return secondaryBus.available() ? secondaryBus.read() : 0xFF;
}

// ── Public API ────────────────────────────────────────────────────────────────

bool batteryInit() {
    // Probe: attempt a transmission and check for ACK
    secondaryBus.beginTransmission(IP5306_ADDR);
    uint8_t err = secondaryBus.endTransmission();
    if (err != 0) {
        Serial.printf("[Battery] IP5306 not found (err %d)\n", err);
        return false;
    }
    initialised = true;
    Serial.println("[Battery] IP5306 ready");
    return true;
}

BatteryStatus batteryRead() {
    BatteryStatus s = {-1, false, false};
    if (!initialised) return s;

    // REG_BATT_PCT (0x78): bits [4:1] = 4 battery-LED flags
    // Each set bit = one LED = 25%. Count bits to get charge level.
    uint8_t pct = readReg(REG_BATT_PCT);
    if (pct == 0xFF) return s;   // read error
    uint8_t leds = (pct >> 1) & 0x0F;
    s.percent = __builtin_popcount(leds) * 25;

    // REG_CHARGER_CTL0 (0x20): bit 3 = 1 while charging
    uint8_t chg = readReg(REG_CHARGER_CTL0);
    if (chg == 0xFF) return s;
    s.charging = (chg & 0x08) != 0;

    s.valid = true;
    return s;
}
