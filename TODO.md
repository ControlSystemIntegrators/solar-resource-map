# TODO

Items to address when hardware is in hand and development resumes.

## Firmware

- [ ] Confirm GPIO 4 / GPIO 13 Grove I2C pin assignment against Timer Camera F
      schematic when hardware arrives (see `docs/wiring/bno055.md`)
- [ ] Implement BNO055 calibration offset storage to flash so the sensor
      does not require re-calibration on every boot
      (`Adafruit_BNO055::getSensorOffsets` / `setSensorOffsets`)
- [ ] Expose a `/calibration` endpoint to return current BNO055 calibration
      status (sys/gyro/accel/mag) so the Android app can warn the user
      when calibration is poor
- [x] Refactor `main.cpp` — expose `wifiServerHandleClient()` cleanly;
      removed `extern WebServer` hack
- [ ] Test OV3660 fisheye vflip/hmirror settings with physical hardware;
      adjust `camera.cpp` if image is mirrored or inverted

## Android App

- [ ] Add `gradle/libs.versions.toml` version catalog (Android Studio will
      flag missing dependency declarations when the project is first opened)
- [ ] Apply magnetic declination correction to heading so the overlay aligns
      to true North rather than magnetic North
      (NOAA WMM or World Magnetic Model API)
- [ ] Add tilt correction to the fisheye overlay using BNO055 pitch and roll
      (currently the overlay assumes the camera is mounted level)
- [ ] Add a "Save image" action to export the annotated fisheye to the
      phone's gallery
- [ ] Show BNO055 calibration quality indicator in the UI (sourced from
      `/calibration` endpoint above)
- [ ] Handle the case where the phone switches off WiFi to maintain mobile
      data — guide the user to disable "Switch to mobile data" in Android
      WiFi settings

## Documentation

- [ ] Add a physical assembly / enclosure notes document
- [ ] Document the lens calibration procedure to determine the actual
      fisheye projection model (equidistant vs. equisolid) for the
      specific OV3660 + lens combination used
- [ ] Add wiring diagram image(s) to `docs/wiring/`
