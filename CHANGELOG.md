# Changelog

All notable changes to this project will be documented in this file.


## [8.0.2](https://github.com/seiry/Pixelify-Google-Photos/compare/v8.0.1...v8.0.2) (2026-05-26)

### Documentation

* add commit message instructions for Conventional Commits ([423beeb](https://github.com/seiry/Pixelify-Google-Photos/commit/423beeb429e66f88d068e50f613d2b8bfd30087c))

### CI

* **release:** pin changelog URLs to github.com/seiry/Pixelify-Google-Photos ([112426b](https://github.com/seiry/Pixelify-Google-Photos/commit/112426babbfa1888f701f8a6df8f51457e1fc879))

## 8.0.1 (2026-05-26)

## 8.0.0 — Initial fork release

* Migrated from `de.robv.android.xposed:api:82` to `io.github.libxposed:api:101.0.0` (modern LSPosed API)
* Combined the old `DeviceSpoofer` + `FeatureSpoofer` into a single `ModuleMain` extending `XposedModule`
* Toolchain bumped: AGP 8.10.1, Gradle 8.13, Kotlin 2.0.21, JDK 21, compileSdk 36, minSdk 26
* Module declaration moved from `assets/xposed_init` + manifest meta-data to `META-INF/xposed/{java_init.list, module.prop, scope.list}`
* Package renamed to `seiry.xposed.pixelifygooglephotos.lsposed` to coexist with the upstream APK
* EdXposed no longer supported (libxposed/api 101 is LSPosed-only)
* Minimum Android version raised from 5.0 → 8.0
