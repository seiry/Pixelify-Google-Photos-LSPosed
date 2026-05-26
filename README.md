# Pixelify-Google-Photos (libxposed/LSPosed API 101 fork)

[![Build and Release](https://github.com/seiry/Pixelify-Google-Photos/actions/workflows/release.yaml/badge.svg)](https://github.com/seiry/Pixelify-Google-Photos/actions/workflows/release.yaml)

This is a fork of [BaltiApps/Pixelify-Google-Photos](https://github.com/BaltiApps/Pixelify-Google-Photos), migrated from the legacy `de.robv.android.xposed` API to the modern [libxposed/api](https://github.com/libxposed/api) (LSPosed API 101).

The module makes any Android device look like a Google Pixel to the Google Photos app so it unlocks Pixel-exclusive features (unlimited original-quality backup, Magic Eraser, etc.).

## What changed in this fork

- Switched from `de.robv.android.xposed:api:82` to `io.github.libxposed:api:101.0.0`
- New entry point: a single `ModuleMain` extending `XposedModule`, declared via `META-INF/xposed/java_init.list` (the old `assets/xposed_init` and `xposed*` manifest meta-data have been removed)
- Hooks now use the OkHttp-style interceptor chain (`hook(method).intercept { chain -> ... }`) instead of `XposedHelpers.findAndHookMethod` / `XC_MethodHook`
- Package name changed to `seiry.xposed.pixelifygooglephotos.lsposed` so this fork installs side-by-side with the upstream APK
- Toolchain: AGP 8.10.1 / Gradle 8.13 / Kotlin 2.0.21 / JDK 21, `compileSdk 36`, `minSdk 26`, `targetSdk 35`

## Requirements

- Android **8.0+** (libxposed/api 101 requires minSdk 26; the previous fork supported 5.0+)
- **LSPosed only** — EdXposed is no longer supported because it does not implement libxposed/api 101. Upstream's EdXposed compatibility was dropped together with the legacy API.
- Magisk / KernelSU (or any other Zygisk host that LSPosed can ride on)

## Install

1. Install Magisk + [LSPosed (Zygisk)](https://github.com/LSPosed/LSPosed).
2. Install the APK from the [Releases](https://github.com/seiry/Pixelify-Google-Photos/releases) page.
3. Open LSPosed, enable the module — scope is automatically `com.google.android.apps.photos` because the module declares `staticScope=true` in `META-INF/xposed/module.prop` and lists Google Photos in `META-INF/xposed/scope.list`.
4. Force-stop or restart Google Photos (the launcher app provides a button for this). You may also need to clear Google Photos data the first time.

## How does this module work?

It still hooks `hasSystemFeature()` on `android.app.ApplicationPackageManager`. When Google Photos queries for Pixel-only features, the module returns `true`. The module can also rewrite static fields on `android.os.Build` and `android.os.Build.VERSION` (BRAND, MODEL, FINGERPRINT, RELEASE, SDK_INT, …) so Google Photos thinks it is running on a real Pixel.

Sources for the feature lists and build props:
- [Dot OS pixel_2016_exclusive.xml](https://github.com/DotOS/android_vendor_dot/blob/55f1c26bb6dbb1175d96cf538ae113618caf7d06/prebuilt/common/etc/pixel_2016_exclusive.xml)
- [PixelFeatureDrops Magisk module](https://github.com/ayush5harma/PixelFeatureDrops/tree/master/system/etc/sysconfig)
- [Pixel-Props repos](https://github.com/orgs/Pixel-Props/repositories)

## Preferences

Settings are persisted to `/sdcard/pixelify-pref.json` (root is requested on first read/write). This is the upstream's workaround for `XSharedPreferences` being broken on newer Android — kept in this fork so config from the original app carries over.

## Building locally

```bash
# Requires JDK 21 + Android SDK with platform-36 and build-tools 36.1.0
export JAVA_HOME=/path/to/jdk-21
export ANDROID_HOME=/path/to/Android/sdk
./gradlew assembleDebug      # auto-signed debug APK at app/build/outputs/apk/debug/
./gradlew assembleRelease    # unsigned release APK; sign with apksigner or via the workflow
```

## Disclaimer

The user takes sole responsibility for any damage that might arise from using this module, including device damage, data loss, and legal matters. This project was made as a learning initiative and the developer cannot be held liable for use of it.

## Credits

- Upstream: [BaltiApps/Pixelify-Google-Photos](https://github.com/BaltiApps/Pixelify-Google-Photos)
- Modern Xposed API: [libxposed/api](https://github.com/libxposed/api), [LSPosed](https://github.com/LSPosed/LSPosed)
