# Releasing

Reference for cutting a new release. Optimized for an AI agent to follow without further context.

## TL;DR — one-shot flow

```bash
pnpm install                                  # only once per clone
pnpm release                                  # interactive: pick patch/minor/major
# or non-interactive:
pnpm release:patch    # 8.0.0 → 8.0.1
pnpm release:minor    # 8.0.0 → 8.1.0
pnpm release:major    # 8.0.0 → 9.0.0
```

`release-it` will:
1. Decide the new version (interactive prompt or from the script name)
2. Generate `CHANGELOG.md` from conventional commits since the last tag (via `@release-it/conventional-changelog`)
3. Run `scripts/sync-android-version.mjs` to propagate the version into `app/build.gradle`, `update_info.json`, and `strings.xml`
4. Commit everything (`chore: release vX.Y.Z`), tag (`vX.Y.Z`), push to `main` + push the tag
5. The pushed `v*` tag triggers `.github/workflows/release.yaml`, which builds + signs the APK and creates a GitHub Release whose body is the latest section of `CHANGELOG.md`

To preview without changing anything:

```bash
pnpm release:dry
```

## Conventional Commits — required for auto-changelog

The auto-generated section in `CHANGELOG.md` is only as good as your commit messages. Use the [Conventional Commits](https://www.conventionalcommits.org/) prefix on every commit:

| Prefix | Shows up in CHANGELOG.md under | Triggers version bump |
|---|---|---|
| `feat: ...` | **Features** | minor |
| `fix: ...` | **Bug Fixes** | patch |
| `perf: ...` | **Performance** | patch |
| `refactor: ...` | Refactor | none |
| `docs: ...` | Documentation | none |
| `build: ...` | Build | none |
| `ci: ...` | CI | none |
| `chore: ...` / `test:` / `style:` | _hidden_ | none |
| `feat!: ...` or footer `BREAKING CHANGE:` | **⚠ BREAKING CHANGES** | major |

If you don't use these prefixes you'll still get a release, but the changelog section will be empty.

## Version sources of truth (must all agree)

A release bumps **four** places. If they drift, the changelog dialog stops firing or the in-app update check breaks.

| # | File | Field | Example | Notes |
|---|---|---|---|---|
| 1 | `app/build.gradle` | `versionCode` | `9` | Integer, must increase monotonically. Read by Android (`pm install -r`) and by `BuildConfig.VERSION_CODE`. |
| 2 | `app/build.gradle` | `versionName` | `"8.1"` | Free-form string. Surfaced in Settings → Apps and in the CI release tag (when triggered manually). |
| 3 | `update_info.json` | `latest_version_code` | `9` | Must equal #1. The app fetches this from `raw.githubusercontent.com/seiry/Pixelify-Google-Photos/main/update_info.json` on launch; if remote > local `BuildConfig.VERSION_CODE`, the "update available" link appears. |
| 4 | `app/src/main/res/values/strings.xml` | `version_head` + `version_desc` | `"Version 8.1"` + bullet list | Powers the in-app changelog dialog. Dialog fires once per user when `BuildConfig.VERSION_CODE > PREF_LAST_VERSION` saved in `/sdcard/pixelify-pref.json`. |

### Changelog dialog mechanics

`ActivityMain.kt` runs this on every launch:

```kotlin
val thisVersion = BuildConfig.VERSION_CODE
if (getInt(PREF_LAST_VERSION, 0) < thisVersion) {
    showChangeLog()                  // reads version_head / version_desc
    putInt(PREF_LAST_VERSION, thisVersion)
}
```

So:
- Bumping `versionCode` is what *triggers* the dialog for existing users.
- `version_head` / `version_desc` are what *content* the dialog shows. If you forget to update them, users see the previous release's notes.
- `version_desc` uses `\n` for line breaks and the upstream convention is to prefix each bullet with `#`.

## Pre-flight

```bash
# Working tree must be clean and on main
git status
git pull --ff-only
```

## Step-by-step

Replace `8` / `8.1` with your new values. Use semver-ish for `versionName`.

```bash
NEW_CODE=9
NEW_NAME="8.1"
```

1. Bump versions:
   - `app/build.gradle`: `versionCode <NEW_CODE>`, `versionName "<NEW_NAME>"`
   - `update_info.json`: `"latest_version_code": <NEW_CODE>`
   - `app/src/main/res/values/strings.xml`:
     - `<string name="version_head">Version <NEW_NAME></string>`
     - `<string name="version_desc">` — replace body with this release's bullet list (use `\n` after each line; `#` prefix per bullet matches the upstream style)

2. Sanity check that nothing else still references the old version:
   ```bash
   grep -rn "versionCode\|versionName\|latest_version_code\|version_head" \
     app/build.gradle update_info.json app/src/main/res/values/strings.xml
   ```

3. Build locally to catch silly mistakes (XML escape errors etc.):
   ```bash
   export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
   export ANDROID_HOME=$HOME/Library/Android/sdk
   export PATH=$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH
   ./gradlew assembleRelease
   ```

4. Commit and push:
   ```bash
   git add app/build.gradle update_info.json app/src/main/res/values/strings.xml
   git commit -m "release v${NEW_NAME}"
   git push
   ```

5. Tag → triggers CI:
   ```bash
   git tag "v${NEW_NAME}"
   git push origin "v${NEW_NAME}"
   ```

6. Watch CI:
   ```bash
   export GH_REPO=seiry/Pixelify-Google-Photos       # if not exported elsewhere
   gh run watch
   ```

7. Verify release:
   ```bash
   gh release view "v${NEW_NAME}"
   gh release download "v${NEW_NAME}" -p '*.apk' -D /tmp
   adb install -r /tmp/Pixelify-Google-Photos-v${NEW_NAME}.apk
   ```

## CI surface

- Workflow file: `.github/workflows/release.yaml`
- Triggers: push tag matching `v*`, or `workflow_dispatch`
- Required repo secrets (already configured): `KEY64`, `ALIAS`, `KEY` (see "Secrets" below)
- Output: a GitHub Release with tag = pushed git tag (e.g. `v8.1`), one APK asset named `Pixelify-Google-Photos-<tag>.apk`
- When triggered via `workflow_dispatch` (no tag), the release is tagged `manual-<versionName>-<shortSha>` instead

## Recovering from a bad release

```bash
# Delete the GitHub Release and remote tag
gh release delete "v${NEW_NAME}" --yes
git push origin ":refs/tags/v${NEW_NAME}"

# Delete the local tag
git tag -d "v${NEW_NAME}"

# Fix the issue, then re-tag and push
```

Note: deleting a release / tag is fine. **Re-using the same tag after users have downloaded the bad APK is not** — the in-app update check compares `versionCode`, not tags, so bump `versionCode` again before re-releasing.

## Secrets

Configured once in GitHub repo settings. Do **not** rotate without backing up the old keystore — APKs signed with a different key cannot upgrade installs of the old key.

| Secret | Source |
|---|---|
| `KEY64` | `base64 -i ~/.keys/pixelify-release.jks \| tr -d '\n'` (one line, no trailing newline) |
| `ALIAS` | Key alias used at `keytool -genkey` time (this project uses `pixelify`) |
| `KEY` | Keystore password (also used as key password, since they were set equal) |

Re-publishing them (e.g. on a new machine):

```bash
export GH_REPO=seiry/Pixelify-Google-Photos
base64 -i ~/.keys/pixelify-release.jks | tr -d '\n' | gh secret set KEY64
gh secret set ALIAS --body "pixelify"
printf 'Keystore password: '; read -s KS_PASS; echo
printf '%s' "$KS_PASS" | gh secret set KEY
unset KS_PASS
gh secret list
```

## Module API metadata (rarely changes)

The new libxposed manifest lives in `app/src/main/resources/META-INF/xposed/`:

- `module.prop` — `minApiVersion=101`, `targetApiVersion=101`, `staticScope=true`. Bump `targetApiVersion` (and add `targetApiVersion=102` etc.) only when adopting new libxposed features.
- `java_init.list` — fully-qualified entry class, currently `seiry.xposed.pixelifygooglephotos.lsposed.ModuleMain`. If the entry class is renamed/moved, this file **must** be updated or LSPosed won't load the module.
- `scope.list` — packages the module is statically scoped to; currently `com.google.android.apps.photos`.

## Common pitfalls

- **Forgetting to update `update_info.json`.** The CI release will succeed and the APK works, but the in-app update banner won't appear for existing users until you do.
- **`version_desc` with raw line breaks instead of `\n`** — Android XML treats whitespace literally; use `\n` to force line breaks inside `<string>` blocks.
- **Tag named without `v` prefix.** Workflow trigger is `tags: [ 'v*' ]`, so `8.1` won't fire CI but `v8.1` will.
- **Tag already exists locally but not remote** (or vice versa). If `git push origin v8.1` fails, check `git tag -l` and `git ls-remote --tags origin`.
- **`r0adkll/sign-android-release` looking for the wrong build-tools.** The workflow pins `BUILD_TOOLS_VERSION: "36.1.0"`; if you bump AGP to a version requiring newer build-tools, bump this env too.
- **Keystore password contained shell-special characters and was set via `--body`** instead of stdin pipe → CI fails at the sign step. Always pipe via `printf '%s'`.
