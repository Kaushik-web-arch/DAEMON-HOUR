# Ghost Drain Hunter · PS 14

## PRD and FRD

Ghost Drain Hunter tells a user how quickly their phone lost battery while the screen was off. Tap Start, unplug, turn the screen off, then turn it back on. The app records the battery percentage and time at both transitions, shows percentage points lost per hour, and displays “Something is draining your phone in your pocket” above the selected rate. The default is 5 points/hour. A session at exactly the threshold does not trigger the flag.

Keep the latest 30 sessions on-device as the bonus. The differentiator identifies short sessions whose hourly estimate is sensitive to a one-point battery reading change. Charging sessions and battery increases are shown as excluded from drain calculations. Monitoring starts from the visible app, runs with a foreground service notification and ends on Stop. The app uses no backend or external API.

## Architecture and ownership

One app module, package `com.daemonhour.ghostdrain`. The service receives screen broadcasts, samples the system battery, and sends completed sessions to the local repository. The ViewModel exposes typed UI state and receives events. Kaushik owns the application shell, including the screen entry point. Adithya's screen takes state and a callback. Prithvi's calculation consumes only the shared model. Keep the shared contracts on one reviewed baseline.

All Kotlin paths below are relative to `app/src/`.

| Owner / temporary branch | Exclusive files | Deliverable and acceptance |
| --- | --- | --- |
| Kaushik / `ghost-drain/integration` | `main/java/com/daemonhour/ghostdrain/contract/DrainContracts.kt`, `MainActivity.kt`, `DrainViewModel.kt`, `DrainRepository.kt`, `BatteryMonitorService.kt`, `BaselineScreen.kt` (same package directory), `main/AndroidManifest.xml`, `main/res/drawable/ic_monitor.xml`, `main/res/values/styles.xml`, all Gradle and wrapper files, this brief | Start/stop monitoring, accurate screen transition capture, durable history, app wiring, review and integration. Verify same-commit build, restart, charging and notification behavior. |
| Prithvi / `ghost-drain/domain` | `main/java/com/daemonhour/ghostdrain/domain/DrainInsights.kt`, `test/java/com/daemonhour/ghostdrain/domain/DrainInsightsTest.kt` | `object DrainInsights : DrainAnalyzer`. Use elapsed time, exclude charging and invalid durations or increases, compare strictly above threshold, label samples under 15 minutes as short. Cover 2 points in 30 minutes, above threshold, equality, zero loss, charging, increase and invalid duration. |
| Adithya / `ghost-drain/ui` | `main/java/com/daemonhour/ghostdrain/ui/DashboardScreen.kt`, `main/java/com/daemonhour/ghostdrain/ui/DrainComponents.kt`, `androidTest/java/com/daemonhour/ghostdrain/ui/DashboardScreenTest.kt` | `@Composable fun DashboardScreen(state: DrainUiState, onEvent: (DrainEvent) -> Unit)`. Show monitoring state, start/stop, threshold options, latest reading, both timestamps and percentages, short-sample message, history and errors. Test CTA callbacks and high-drain rendering. Primary emulator verification. |

Prithvi and Adithya work from the same contract baseline. Implement only the listed files. Ask Kaushik to review contract changes before editing. Adithya can render previews and test fake states without waiting for the service. Prithvi can unit-test the analyzer without waiting for Compose. Prithvi is the backup build/demo operator.

## Implementation and integration

The official organizer repo is the eventual submission destination. This temporary workspace is the team's working source until it arrives. Use separate branches only if contributor access is verified. For contributions without write access, the contributor can use a local branch and send a `git format-patch` file privately to Kaushik for review and application.

Build the minimum tracking flow first. Review each PR or patch for ownership and imports. Integrate the domain implementation first, then wire in the Compose screen in `MainActivity.kt`. Preserve the official repository's files when porting the verified source and follow its submission branch rules.

On Windows, open the repository root in Android Studio and use its configured Gradle JDK (17 or compatible). From PowerShell:

```powershell
git rev-parse HEAD
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

For the UI branch with a booted emulator, run `.\gradlew.bat :app:connectedDebugAndroidTest`. The debug APK is `app/build/outputs/apk/debug/app-debug.apk`. Run it in Android Studio or install with `adb install -r app/build/outputs/apk/debug/app-debug.apk`. Record the SHA, build log and test outcome independently. Preserve the last passing APK and SHA. If an update regresses, build the last passing SHA from a separate checkout or revert the reviewed regression with a new commit.

## Acceptance and handoff

On the emulator, disconnect virtual AC power. Start tracking, turn the screen off, lower virtual capacity, then turn the screen on. Check both samples, a positive rate, and the high-drain message. Label the battery change as simulated during the demo. Repeat with unchanged battery, then a charging session. Stop monitoring and reopen the app to verify history survives. A short power-button test on a physical phone should also record a real session even if the battery percentage stays unchanged.

Return changed paths, branch, commit SHA, exact test commands/results and blockers. Use verified status for observed builds and emulator results. The team shares the eventual verified SHA and corresponding APK for backup demonstration.
