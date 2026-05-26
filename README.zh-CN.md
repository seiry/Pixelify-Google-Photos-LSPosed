# Pixelify-Google-Photos（libxposed/LSPosed API 101 分支）

[![Build and Release](https://github.com/seiry/Pixelify-Google-Photos/actions/workflows/release.yaml/badge.svg)](https://github.com/seiry/Pixelify-Google-Photos/actions/workflows/release.yaml)

**English**: [README.md](README.md)

本项目 fork 自 [BaltiApps/Pixelify-Google-Photos](https://github.com/BaltiApps/Pixelify-Google-Photos)，已经从老的 `de.robv.android.xposed` API 迁移到现代的 [libxposed/api](https://github.com/libxposed/api)（LSPosed API 101）。

模块的作用是让 Google Photos 误以为当前设备是 Google Pixel，从而解锁 Pixel 独占功能（原画质无限备份、魔法擦除等）。

## 本 fork 的改动

- 依赖切到 `io.github.libxposed:api:101.0.0` 和 `io.github.libxposed:service:101.0.0`，不再用 `de.robv.android.xposed:api:82`
- 入口类合并成一个 `ModuleMain`，继承 `XposedModule`，通过 `META-INF/xposed/java_init.list` 声明（删掉了老的 `assets/xposed_init` 和 manifest 里的 `xposed*` meta-data）
- Hook 使用 OkHttp 风格的 interceptor 链（`hook(method).intercept { chain -> ... }`），代替老的 `XposedHelpers.findAndHookMethod` / `XC_MethodHook`
- 偏好不再走 `/sdcard/pixelify-pref.json` + root，而是用 LSPosed remote preferences —— **被 hook 的 Google Photos 进程不再需要 root**
- 包名改为 `seiry.xposed.pixelifygooglephotos.lsposed`，可以跟上游 APK 共存
- 工具链：AGP 9.2.1 / Gradle 9.5.1 / Kotlin 2.2.21 / JDK 21，`compileSdk 36`、`minSdk 26`、`targetSdk 35`

## 环境要求

- Android **8.0 或更高**（libxposed/api 101 要求 minSdk 26，上游 fork 之前支持到 5.0）
- **LSPosed 2.0 或更高** —— 更早的 LSPosed 只提供老 API，加载不了这个模块。EdXposed 同样不再支持（libxposed/api 101 只在 LSPosed 上跑）
- Magisk / KernelSU（或其他能承载 LSPosed 的 Zygisk 宿主）

> **关于 LSPosed 的说明。** 原版 [LSPosed/LSPosed](https://github.com/LSPosed/LSPosed) 仓库已于 2024-01 **被 archived**（最后 commit `df74d83`），不再发版。LSPosed 2.0+ 目前**只通过 [LSPosed Telegram 群](https://t.me/LSPosed)以闭源构建产物的形式分发** —— 没有公开源码、没有 GitHub release。是否使用请自行权衡。

目前**只在 Android 16 + LSPosed 2** 上验证过。理论上 Android 8 及以上（minSdk 是 26）都应该能跑，但没实际测过。

相关链接：

- [LSPosed Telegram 群](https://t.me/LSPosed) —— 答疑 / 讨论
- [libxposed/api Javadoc](https://libxposed.github.io/api/) —— 被 hook 进程里 `ModuleMain` 用的那套 API
- [libxposed/service Javadoc](https://libxposed.github.io/service/) —— UI app 端用来访问 remote preferences、scope 查询等的 API

## 安装步骤

1. 装 Magisk + LSPosed 2.0 或更新版。2.0+ 的构建产物目前只在 [LSPosed Telegram 群](https://t.me/LSPosed)中分发（闭源）；GitHub 上的 [LSPosed/LSPosed](https://github.com/LSPosed/LSPosed) 仓库已 archived，里面只剩老版 1.x 的源码
2. 从 [Releases](https://github.com/seiry/Pixelify-Google-Photos/releases) 页面下载 APK 并安装
3. 打开 LSPosed，启用本模块 —— 作用域已通过 `META-INF/xposed/module.prop` 的 `staticScope=true` 和 `META-INF/xposed/scope.list` 自动锁定为 `com.google.android.apps.photos`
4. 强制停止或重启 Google Photos（启动器 app 里有按钮）。首次使用可能需要清除 Google Photos 数据

## 工作原理

模块依旧 hook `android.app.ApplicationPackageManager` 上的 `hasSystemFeature()`，当 Google Photos 查询 Pixel 独占特性时返回 `true`。还会改写 `android.os.Build` 和 `android.os.Build.VERSION` 的静态字段（BRAND、MODEL、FINGERPRINT、RELEASE、SDK_INT 等），让 Google Photos 以为运行在真的 Pixel 上。

特性列表和 build prop 的数据来源：
- [Dot OS pixel_2016_exclusive.xml](https://github.com/DotOS/android_vendor_dot/blob/55f1c26bb6dbb1175d96cf538ae113618caf7d06/prebuilt/common/etc/pixel_2016_exclusive.xml)
- [PixelFeatureDrops Magisk 模块](https://github.com/ayush5harma/PixelFeatureDrops/tree/master/system/etc/sysconfig)
- [Pixel-Props 仓库组](https://github.com/orgs/Pixel-Props/repositories)

## 偏好存储

设置存在 LSPosed 的 remote preferences 里（group 名 `settings`），由框架管理 —— 不需要手动管理文件、不需要 root。UI app 通过 `XposedService.getRemotePreferences("settings")` 读写；被 hook 的 Google Photos 进程通过 `XposedModule.getRemotePreferences("settings")` 只读访问。

⚠ 这是对上游 `/sdcard/pixelify-pref.json` 方案的破坏性改动 —— 从原版 BaltiApps APK 升级过来的用户**不会**自动迁移之前的设置。

## 本地构建

```bash
# 需要 JDK 21 + 包含 platform-36 和 build-tools 36.1.0 的 Android SDK
export JAVA_HOME=/path/to/jdk-21
export ANDROID_HOME=/path/to/Android/sdk
./gradlew assembleDebug      # 自动签名的 debug APK 在 app/build/outputs/apk/debug/
./gradlew assembleRelease    # 未签名的 release APK；用 apksigner 或者通过 workflow 签名
```

发版流程和版本号管理见 [RELEASING.md](RELEASING.md)。

## 免责声明

使用本模块所导致的一切问题（设备损坏、数据丢失、法律纠纷等）由使用者本人承担。本项目仅作为学习目的开发，开发者对其使用结果不负任何责任。

## 致谢

- 上游：[BaltiApps/Pixelify-Google-Photos](https://github.com/BaltiApps/Pixelify-Google-Photos)
- 现代 Xposed API：[libxposed/api](https://github.com/libxposed/api)、[libxposed/service](https://github.com/libxposed/service)
- LSPosed 框架：原 [LSPosed/LSPosed](https://github.com/LSPosed/LSPosed)（已 archived；2.0+ 构建只在 [LSPosed Telegram 群](https://t.me/LSPosed) 中分发）
