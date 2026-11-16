# Demonstration and release checklist

## Before coding

- [ ] Official problem statement copied exactly into TEAM_PLAN.md.
- [ ] Scope and unique value explained in one sentence.
- [ ] Android Studio / SDK / emulator or USB device working on at least one laptop.
- [ ] Collaborators accept GitHub invitations and can read/write their assigned branches.
- [ ] Agree on package name, shared interfaces and file ownership.

## MVP verification

- [ ] Android project opens and Gradle sync succeeds.
- [ ] App builds without errors.
- [ ] App launches on Android emulator or USB device.
- [ ] Core user journey works without crashes.
- [ ] Required permissions are requested correctly, with sensible fallbacks.
- [ ] No mocked output is presented as live data.
- [ ] Offline behavior and missing-permission behavior checked where applicable.
- [ ] Feature branches merged into main via reviewed PRs.
- [ ] Fresh clone of main builds/runs on the demonstration laptop.

## Judge demo

- [ ] Show the problem and who benefits.
- [ ] Perform the primary workflow live.
- [ ] Demonstrate the differentiator clearly, not only describe it.
- [ ] Show a useful, actionable outcome.
- [ ] Describe Kotlin/Compose, Android APIs, and local storage truthfully.
- [ ] State why the solution differs from comparable apps without claiming to be the first.
- [ ] Have offline screenshots or a clearly labeled demo scenario if sensors/API readings are unreliable.
- [ ] Keep the USB cable, phone, emulator, charger, and backup build ready.
- [ ] Do not claim completion until the live run passes.

## After demonstration

- [ ] Record final commit hash.
- [ ] Keep main stable and archive demo screenshots if organizer rules permit.
