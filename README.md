# Acer_Remote

A minimal Android remote for Acer Android TV / Google TV sets, working over
IR (phone's built-in IR blaster) or Wi-Fi.

Controls: Back, Home, Google voice, Volume down / Mute / Volume up, and a large
touch pad (swipe to move, tap to select).

## Status

- [x] App skeleton and minimal UI (actions are only logged for now)
- [ ] IR sending
- [ ] Wi-Fi pairing and control
- [ ] Release APK

## Build

Open the folder in Android Studio, or run `./gradlew assembleDebug`
(needs the Android SDK; set `sdk.dir` in `local.properties` or `ANDROID_HOME`).
The APK lands in `app/build/outputs/apk/debug/`.
