# Changelog

All notable changes to this project will be documented in this file.


## 9.0.0 (2026-05-26)

### ⚠ BREAKING CHANGES

* EdXposed is no longer supported (libxposed/api 101 is LSPosed-only); minSdk raised from 21 to 26 (Android 8.0+); package id changed.

### Features

* **i18n:** add Chinese localization for strings ([378ad1a](https://github.com/seiry/Pixelify-Google-Photos/commit/378ad1aae3290d11a2f79e5c051edba6b0874b02))
* migrate from legacy Xposed API to libxposed/api 101 (LSPosed) ([888d758](https://github.com/seiry/Pixelify-Google-Photos/commit/888d75898d012b3146e16851bd3bc9c562c13fba))

### Bug Fixes

* **release:** sync script regex supports both 'versionCode 10' and 'versionCode = 10' ([76a8c33](https://github.com/seiry/Pixelify-Google-Photos/commit/76a8c336922d0f0c3d0f0b83365288b0765800ad))

### Refactor

* replace FilePref with LSPosed remote preferences ([cc44f58](https://github.com/seiry/Pixelify-Google-Photos/commit/cc44f58f00dbc44431f9e7d2c07772505d85395d))

### Documentation

* add Chinese README and link libxposed API docs / LSPosed Telegram ([65b127f](https://github.com/seiry/Pixelify-Google-Photos/commit/65b127f8fd759fa564cf7c2c25acb0074a028a91))
* add commit message instructions for Conventional Commits ([423beeb](https://github.com/seiry/Pixelify-Google-Photos/commit/423beeb429e66f88d068e50f613d2b8bfd30087c))
* add RELEASING.md with version source-of-truth checklist ([71ac210](https://github.com/seiry/Pixelify-Google-Photos/commit/71ac210e935079847b556a650755135dd9e74b75))
* clarify LSPosed 2.0+ is Telegram-only closed-source ([d180ae4](https://github.com/seiry/Pixelify-Google-Photos/commit/d180ae47e236914b94a685710ee0d3db6bc08444))
* drop the specific LSPosed fork recommendation and add tested-on note ([a9813bc](https://github.com/seiry/Pixelify-Google-Photos/commit/a9813bca9c721693c3afd74ceebbabc9ffbe8da0))
* note LSPosed/LSPosed is archived and point at JingMatrix/Vector ([0c1c385](https://github.com/seiry/Pixelify-Google-Photos/commit/0c1c3852ad325bc9ee26f25dac4443163e906cf9))
* **ui:** refresh in-app changelog text for the LSPosed migration ([8ad18d0](https://github.com/seiry/Pixelify-Google-Photos/commit/8ad18d0354a54698341b3c6c21709f50a1f49d26))

### Build

* add libxposed-service dep and App class for service binding ([3a61ade](https://github.com/seiry/Pixelify-Google-Photos/commit/3a61adedf4fef3bbeeddeac72543e2cf53950812))
* upgrade to AGP 9.2.1, Kotlin 2.2.21, Gradle 9.5.1 and drop AsyncTask ([ac6432f](https://github.com/seiry/Pixelify-Google-Photos/commit/ac6432f2fa1879927073c66f4af06187b592a9f2))

### CI

* bump actions to node24-compatible versions and inline apksigner ([5b58c78](https://github.com/seiry/Pixelify-Google-Photos/commit/5b58c781b0e4bb2395018e38e9d1c672fdb66fa6))
* bump BUILD_TOOLS_VERSION to 36.1.0 to match AGP ([c01745c](https://github.com/seiry/Pixelify-Google-Photos/commit/c01745c50bc9affd0f79b609d61b31a58ab689bb))
* **release:** add release-it with conventional-changelog and Android version sync ([ef6d55e](https://github.com/seiry/Pixelify-Google-Photos/commit/ef6d55e9b539d3d459f6d5b9321e9ea792ca49fa))
* **release:** pin changelog URLs to github.com/seiry/Pixelify-Google-Photos ([112426b](https://github.com/seiry/Pixelify-Google-Photos/commit/112426babbfa1888f701f8a6df8f51457e1fc879))
* **release:** trigger on v* tags and derive release tag from git ref ([0354c76](https://github.com/seiry/Pixelify-Google-Photos/commit/0354c76a733c7b045916b63f87c1e33a9d457e18))
