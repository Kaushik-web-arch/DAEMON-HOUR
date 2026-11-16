# Contribution guide

## Getting started (Windows PowerShell or VS Code terminal)

```powershell
git clone https://github.com/Kaushik-web-arch/DAEMON-HOUR.git
cd DAEMON-HOUR
git fetch origin
git switch feature/ui
```

Replace `feature/ui` with your assigned branch (`feature/core` or `feature/insights`).

To update your branch after `main` changes:

```powershell
git fetch origin
git switch feature/ui
git merge origin/main
```

Resolve any conflicts before continuing.

## Commit and share

```powershell
git status
git add .
git commit -m "feat: describe the working change"
git push -u origin feature/ui
```

Use your own branch name. Open a pull request from your feature branch to `main` on GitHub.

## Pull request requirements

- State what changed and why it is needed by the **allotted problem statement**.
- Explain how it was tested (Android emulator, USB device, or automated tests).
- Link relevant issues; add screenshots for visual work.
- Confirm no local paths, secrets, mock features disguised as real, or unrelated files.
- Wait for a review before merging into `main`.

## Working agreements

- Only the integration lead merges PRs to `main` after a basic run check.
- Prefer different files/packages per teammate; define shared interfaces before coding.
- Do not introduce dependencies or architecture changes silently.
- Use truthful labels for demo/sample data. Never claim sensor or system information is real when simulated.
- Prefer a working core flow over optional features.
- Never commit `local.properties`, API keys, keystores, or build directories.
- Do not modify another teammate's branch without coordination.

## New task allocation

The feature ownership in TEAM_PLAN.md is provisional. Reassign after the official problem statement arrives.