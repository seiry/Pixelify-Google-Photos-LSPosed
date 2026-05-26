# Commit message instructions

Generated commit messages **must** follow [Conventional Commits 1.0](https://www.conventionalcommits.org/). These messages feed `@release-it/conventional-changelog`, which auto-populates `CHANGELOG.md` and the GitHub Release body — sloppy messages mean sloppy release notes.

## Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

- **`<type>`**: see table below. Required.
- **`<scope>`**: optional. Use one of `module`, `ui`, `build`, `ci`, `release`, `docs`, or omit. No new scopes — keep the list small.
- **`<subject>`**: imperative mood ("add" not "added"/"adds"), lower-case start, no trailing period, ≤ 72 chars.
- **`<body>`**: optional, wrap at 80 chars. Use for **why**, not **what** (the diff already shows what).
- **`<footer>`**: optional. Use `BREAKING CHANGE: <description>` for breaking changes, or `Refs: #123` to link issues.

A single-line message is fine if the change is small.

## Type table

| Type | When | Section in CHANGELOG.md | Triggers |
|---|---|---|---|
| `feat` | New user-visible feature or capability | **Features** | minor bump |
| `fix` | Bug fix | **Bug Fixes** | patch bump |
| `perf` | Performance improvement, no behavior change | **Performance** | patch bump |
| `refactor` | Internal restructuring, no behavior change | **Refactor** | — |
| `docs` | Documentation only (README, RELEASING.md, KDoc, comments) | **Documentation** | — |
| `build` | Build system, Gradle, dependencies | **Build** | — |
| `ci` | CI config (`.github/workflows/*`, release tooling) | **CI** | — |
| `chore` | Misc maintenance not user-visible | hidden | — |
| `test` | Tests only | hidden | — |
| `style` | Formatting / whitespace | hidden | — |
| `revert` | Revert a previous commit | hidden | — |

## Breaking changes

Two equivalent ways:
- Append `!` to the type: `feat!: drop EdXposed support`
- Add a footer: `BREAKING CHANGE: drop EdXposed support`

Either bumps the major version.

## Examples (good)

```
feat(module): spoof PIXEL_2025_EXPERIENCE for Photos
fix: hasSystemFeature crash on Android 14
perf(module): cache feature flag lookup
refactor(ui): collapse DeviceSpoofer and FeatureSpoofer into ModuleMain
docs: explain libxposed/api 101 migration in README
build: bump Kotlin to 2.0.21
ci(release): tag-triggered releases extract notes from CHANGELOG
chore: bump pnpm-lock
feat!: rename package to seiry.xposed.pixelifygooglephotos.lsposed

BREAKING CHANGE: APKs from the old package id will not update in place
```

## Examples (bad — do not produce)

```
update files                       # no type, vague
WIP                                # no type, useless to changelog
Fix the thing                      # capitalized, no specifics
feat: Added new feature.           # past tense + period + capitalized
feat: stuff (#42)                  # don't pad subject with PR refs; use footer
chore: feat add pixel 9            # type confusion
```

## Notes for the model

- **Never** invent prefix variants like `feature:`, `bugfix:`, `update:` — only the types in the table are valid.
- **Never** prefix with emoji / gitmoji.
- If the diff spans multiple categories, pick the dominant one and mention the others briefly in the body. Do not chain types like `feat+fix:`.
- For pure version-bump commits made by `release-it`, the message is fixed to `chore: release vX.Y.Z` — don't try to expand it.
- Prefer one focused commit per change. If asked to summarize a sprawling diff, split it conceptually in the body rather than cramming everything into the subject.
