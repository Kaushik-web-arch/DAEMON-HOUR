# Ghost Drain Hunter

**An Android app that measures battery loss while your phone screen is off.**

Ghost Drain Hunter records the battery level when the screen turns off and when it turns on again. It calculates the battery percentage points lost per hour, compares the result with a threshold the user chooses, and keeps a local history of sessions.

## What it does

- Records battery level and timestamps at screen-off and screen-on.
- Calculates the screen-off drain rate in percentage points per hour.
- Shows a high-drain alert when the selected threshold is exceeded.
- Lets the user adjust the alert threshold.
- Keeps a history of previous monitoring sessions on the device.
- Handles charging and invalid readings separately.

A short session can produce a large hourly estimate. For example, losing one percentage point in a few seconds is mathematically extrapolated to an hour, so longer sessions give a more representative rate. The app shows the actual readings and duration alongside the estimate.

## How it works

1. Start monitoring in the app.
2. The app records the battery level and time when the screen turns off.
3. When the screen turns on, the app records the new battery level and time.
4. It calculates the loss rate, checks the chosen threshold, and saves the session locally.

## Built with

- Kotlin
- Native Android APIs for screen events and battery information
- Jetpack Compose and Material 3
- Android SharedPreferences for local session history stored as JSON
- JUnit for domain logic tests

No external API, account, or hosted backend is required. Monitoring data stays on the device.

## Run it

Open the project in Android Studio and allow Gradle sync to finish. Select an Android emulator or connected device, then run the app configuration.

To build and run the unit tests on Windows:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

The debug APK is generated at:

```
app/build/outputs/apk/debug/app-debug.apk
```

## Project status

The integrated Android application source is on the [ghost-drain/team-integrated branch](https://github.com/Kaushik-web-arch/DAEMON-HOUR/tree/ghost-drain/team-integrated). That branch contains the monitoring flow, drain analysis, dashboard, session history, and tests.

The app was built and unit-tested with Gradle and demonstrated on an Android emulator. The hourly rate is an estimate based on the duration and battery change measured in each session.

## Team

- **Kaushik** — Android integration, monitoring and local data handling
- **Prithvi** — drain calculation logic and unit tests
- **Adithya** — Compose dashboard and emulator validation
