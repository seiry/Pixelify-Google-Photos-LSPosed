# Changelog

All notable changes to this project will be documented in this file.

## 8.0.0 — Initial fork release

* Migrated from `de.robv.android.xposed:api:82` to `io.github.libxposed:api:101.0.0` (modern LSPosed API)
* Combined the old `DeviceSpoofer` + `FeatureSpoofer` into a single `ModuleMain` extending `XposedModule`
* Toolchain bumped: AGP 8.10.1, Gradle 8.13, Kotlin 2.0.21, JDK 21, compileSdk 36, minSdk 26
* Module declaration moved from `assets/xposed_init` + manifest meta-data to `META-INF/xposed/{java_init.list, module.prop, scope.list}`
* Package renamed to `seiry.xposed.pixelifygooglephotos.lsposed` to coexist with the upstream APK
* EdXposed no longer supported (libxposed/api 101 is LSPosed-only)
* Minimum Android version raised from 5.0 → 8.0
