# DAEMON HOUR — Android Hackathon Workspace

Shared repository for a three-person Android hackathon team.

> **Important:** The organizers will assign the problem statement during the event. Do not treat any previously discussed prototype idea as the final requirement.

## Current status

- Repository scaffold and collaboration rules only.
- Final problem statement: **not assigned yet**.
- Planned stack: **Kotlin, Android Studio, Jetpack Compose**, subject to the assigned problem.
- Cost: **₹0 additional spend**. Prefer local/device APIs; no paid API keys or services.
- Demo: Android emulator or USB-connected Android device; APK if requested.

## Branches

| Branch | Owner | Scope |
| --- | --- | --- |
| `main` | Entire team | Stable integrated and demo-ready app |
| `feature/ui` | Member 1 / integration lead | Screens, navigation, design system, integration |
| `feature/core` | Member 2 | Problem-specific Android functionality |
| `feature/insights` | Member 3 | Logic, insights, tests, demo validation |

These are initial responsibilities; adjust after problem statement allocation.

## Team workflow

1. Accept GitHub collaborator invitations and clone the repository.
2. Work on your branch only; pull from `main` before starting new work.
3. Agree on the app package, data models, interfaces and file ownership before parallel implementation.
4. Commit small, runnable changes; push your branch.
5. Open a pull request **into `main`** with a short description, testing evidence, and screenshots for UI changes.
6. Integrator reviews and merges, resolves conflicts, then tests the app end-to-end.
7. Never push directly to `main` once coding begins. Do not force-push shared branches.

See [CONTRIBUTING.md](CONTRIBUTING.md), [TEAM_PLAN.md](TEAM_PLAN.md) and [DEMO_CHECKLIST.md](DEMO_CHECKLIST.md).

## When the assigned problem statement arrives

Capture the **exact wording** in [TEAM_PLAN.md](TEAM_PLAN.md). Identify the user problem, required features, uniqueness, demo steps and platform constraints before generating code. Keep the MVP small and functional; add differentiators only after a working vertical slice.

## Run the application

_Not available yet. Add the Android Studio project and verified run instructions after the problem statement is assigned._

## Security

Do not commit tokens, passwords, local.properties, keystores, personal data, APK binaries, or machine-specific build outputs.