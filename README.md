# Pixelify-Google-Photos (libxposed/LSPosed API 101 fork)

[![Build and Release](https://github.com/seiry/Pixelify-Google-Photos/actions/workflows/release.yaml/badge.svg)](https://github.com/seiry/Pixelify-Google-Photos/actions/workflows/release.yaml)

**中文版**: [README.zh-CN.md](README.zh-CN.md)

This is a fork of [BaltiApps/Pixelify-Google-Photos](https://github.com/BaltiApps/Pixelify-Google-Photos), migrated from the legacy `de.robv.android.xposed` API to the modern [libxposed/api](https://github.com/libxposed/api) (LSPosed API 101).

The module makes any Android device look like a Google Pixel to the Google Photos app so it unlocks Pixel-exclusive features (unlimited original-quality backup, Magic Eraser, etc.).

## What changed in this fork

- Switched from `de.robv.android.xposed:api:82` to `io.github.libxposed:api:101.0.0` and `io.github.libxposed:service:101.0.0`
- New entry point: a single `ModuleMain` extending `XposedModule`, declared via `META-INF/xposed/java_init.list` (the old `assets/xposed_init` and `xposed*` manifest meta-data have been removed)
- Hooks now use the OkHttp-style interceptor chain (`hook(method).intercept { chain -> ... }`) instead of `XposedHelpers.findAndHookMethod` / `XC_MethodHook`
- Settings moved off `/sdcard/pixelify-pref.json` + root onto LSPosed remote preferences — **the module no longer requires root on the hooked Google Photos process**
- Package name changed to `seiry.xposed.pixelifygooglephotos.lsposed` so this fork installs side-by-side with the upstream APK
- Toolchain: AGP 9.2.1 / Gradle 9.5.1 / Kotlin 2.2.21 / JDK 21, `compileSdk 36`, `minSdk 26`, `targetSdk 35`

## Requirements

- Android **8.0+** (libxposed/api 101 requires minSdk 26; the previous fork supported 5.0+)
- **LSPosed 2.0+** — earlier LSPosed releases ship the legacy API only and won't load this module. EdXposed is no longer supported either (libxposed/api 101 is LSPosed-only).
- Magisk / KernelSU (or any other Zygisk host that LSPosed can ride on).

> **Heads up on LSPosed forks.** The original [LSPosed/LSPosed](https://github.com/LSPosed/LSPosed) repository is **archived** as of 2024-01 — last commit `df74d83`, no longer maintained. The actively maintained continuation lives at **[JingMatrix/Vector](https://github.com/JingMatrix/Vector)** (formerly `JingMatrix/LSPosed`), which is what ships LSPosed 2.0+ with libxposed/api 101 support. Install from there.

Useful links:

- [JingMatrix/Vector](https://github.com/JingMatrix/Vector) — the actively maintained LSPosed framework
- [LSPosed Telegram group](https://t.me/LSPosed) — support / discussion
- [libxposed/api Javadoc](https://libxposed.github.io/api/) — API used inside the hooked process (`ModuleMain`)
- [libxposed/service Javadoc](https://libxposed.github.io/service/) — API used in the UI app for remote preferences, scope queries, etc.

## Install

1. Install Magisk + LSPosed 2.0 or newer — get it from the actively maintained [JingMatrix/Vector](https://github.com/JingMatrix/Vector) (the original [LSPosed/LSPosed](https://github.com/LSPosed/LSPosed) repo is archived).
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

Settings live in LSPosed's remote preferences (group `settings`), managed by the framework — no manual files, no root required. The UI app reads/writes via `XposedService.getRemotePreferences("settings")`; the hooked Google Photos process reads via `XposedModule.getRemotePreferences("settings")`.

⚠ This is a breaking change from the upstream `/sdcard/pixelify-pref.json` scheme — settings from the original BaltiApps APK are **not** carried over.

## Building locally

```bash
# Requires JDK 21 + Android SDK with platform-36 and build-tools 36.1.0
export JAVA_HOME=/path/to/jdk-21
export ANDROID_HOME=/path/to/Android/sdk
./gradlew assembleDebug      # auto-signed debug APK at app/build/outputs/apk/debug/
./gradlew assembleRelease    # unsigned release APK; sign with apksigner or via the workflow
```

For release workflow and version bumping, see [RELEASING.md](RELEASING.md).

## Disclaimer

The user takes sole responsibility for any damage that might arise from using this module, including device damage, data loss, and legal matters. This project was made as a learning initiative and the developer cannot be held liable for use of it.

## Credits

- Upstream: [BaltiApps/Pixelify-Google-Photos](https://github.com/BaltiApps/Pixelify-Google-Photos)
- Modern Xposed API: [libxposed/api](https://github.com/libxposed/api), [libxposed/service](https://github.com/libxposed/service)
- LSPosed framework: [JingMatrix/Vector](https://github.com/JingMatrix/Vector) (active fork; the original [LSPosed/LSPosed](https://github.com/LSPosed/LSPosed) is archived)
