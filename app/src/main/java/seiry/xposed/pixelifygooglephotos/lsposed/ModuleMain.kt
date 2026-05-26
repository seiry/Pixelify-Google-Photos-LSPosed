package seiry.xposed.pixelifygooglephotos.lsposed

import android.os.Build
import android.util.Log
import seiry.xposed.pixelifygooglephotos.lsposed.Constants.PACKAGE_NAME_GOOGLE_PHOTOS
import seiry.xposed.pixelifygooglephotos.lsposed.Constants.PREF_DEVICE_TO_SPOOF
import seiry.xposed.pixelifygooglephotos.lsposed.Constants.PREF_ENABLE_VERBOSE_LOGS
import seiry.xposed.pixelifygooglephotos.lsposed.Constants.PREF_OVERRIDE_ROM_FEATURE_LEVELS
import seiry.xposed.pixelifygooglephotos.lsposed.Constants.PREF_SPOOF_ANDROID_VERSION_FOLLOW_DEVICE
import seiry.xposed.pixelifygooglephotos.lsposed.Constants.PREF_SPOOF_ANDROID_VERSION_MANUAL
import seiry.xposed.pixelifygooglephotos.lsposed.Constants.PREF_SPOOF_FEATURES_LIST
import seiry.xposed.pixelifygooglephotos.lsposed.Constants.PREF_STRICTLY_CHECK_GOOGLE_PHOTOS
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

/**
 * Entry point for the modern libxposed API (LSPosed >= API 101).
 *
 * Combines the old DeviceSpoofer + FeatureSpoofer responsibilities:
 *  - rewrites static fields of [android.os.Build] / [Build.VERSION]
 *  - intercepts [android.app.ApplicationPackageManager#hasSystemFeature]
 */
class ModuleMain : XposedModule() {

    companion object {
        private const val TAG = "PixelifyGooglePhotos"
        private const val CLASS_APPLICATION_PACKAGE_MANAGER =
            "android.app.ApplicationPackageManager"
    }

    private val pref by lazy { FilePref }

    private val verboseLog: Boolean by lazy {
        pref.getBoolean(PREF_ENABLE_VERBOSE_LOGS, false)
    }

    private val finalDeviceToSpoof: DeviceProps.DeviceEntries? by lazy {
        val deviceName = pref.getString(PREF_DEVICE_TO_SPOOF, DeviceProps.defaultDeviceName)
        info("Device spoof: $deviceName")
        DeviceProps.getDeviceProps(deviceName)
    }

    private val androidVersionToSpoof: DeviceProps.AndroidVersion? by lazy {
        if (pref.getBoolean(PREF_SPOOF_ANDROID_VERSION_FOLLOW_DEVICE, false)) {
            finalDeviceToSpoof?.androidVersion
        } else {
            pref.getString(PREF_SPOOF_ANDROID_VERSION_MANUAL, null)
                ?.let { DeviceProps.getAndroidVersionFromLabel(it) }
        }
    }

    private val finalFeaturesToSpoof: List<String> by lazy {
        val defaultFeatures = DeviceProps.defaultFeatures
        val defaultNames = defaultFeatures.map { it.displayName }.toSet()
        val flags = pref.getStringSet(PREF_SPOOF_FEATURES_LIST, defaultNames)?.let { set ->
            val eligible: List<DeviceProps.Features> = when {
                set.isEmpty() -> {
                    info("Feature flags init: EMPTY SET")
                    emptyList()
                }
                set == defaultNames -> {
                    info("Feature flags init: DEFAULT SET")
                    defaultFeatures
                }
                else -> DeviceProps.allFeatures.filter { it.displayName in set }
            }
            val out = ArrayList<String>()
            eligible.forEach { out.addAll(it.featureFlags) }
            out
        } ?: emptyList()
        info("Pass TRUE for feature flags: $flags")
        flags
    }

    private val overrideCustomROMLevels by lazy {
        pref.getBoolean(PREF_OVERRIDE_ROM_FEATURE_LEVELS, true)
    }

    private val featuresNotToSpoof: List<String> by lazy {
        val all = ArrayList<String>()
        DeviceProps.allFeatures.forEach { all.addAll(it.featureFlags) }
        all.filter { it !in finalFeaturesToSpoof }.also {
            info("Pass FALSE for feature flags: $it")
        }
    }

    private fun info(msg: String) = log(Log.INFO, TAG, msg)
    private fun warn(msg: String, t: Throwable? = null) {
        if (t == null) log(Log.WARN, TAG, msg) else log(Log.WARN, TAG, msg, t)
    }
    private fun verbose(msg: String) { if (verboseLog) info(msg) }

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        info("onModuleLoaded: ${param.processName}")
    }

    override fun onPackageReady(param: PackageReadyParam) {
        if (pref.getBoolean(PREF_STRICTLY_CHECK_GOOGLE_PHOTOS, true) &&
            param.packageName != PACKAGE_NAME_GOOGLE_PHOTOS) {
            return
        }
        info("onPackageReady: ${param.packageName}")
        spoofDeviceFields()
        spoofVersionFields()
        hookHasSystemFeature(param)
    }

    private fun spoofDeviceFields() {
        val props = finalDeviceToSpoof?.props ?: return
        if (props.isEmpty()) return
        val buildClass = Build::class.java
        props.forEach { (key, value) ->
            try {
                setStaticField(buildClass, key, value)
                verbose("DEVICE PROPS: $key - $value")
            } catch (t: Throwable) {
                warn("Failed to set Build.$key", t)
            }
        }
    }

    private fun spoofVersionFields() {
        val map = androidVersionToSpoof?.getAsMap() ?: return
        val versionClass = Build.VERSION::class.java
        map.forEach { (key, value) ->
            try {
                setStaticField(versionClass, key, value)
                verbose("VERSION SPOOF: $key - $value")
            } catch (t: Throwable) {
                warn("Failed to set Build.VERSION.$key", t)
            }
        }
    }

    private fun setStaticField(clazz: Class<*>, name: String, value: Any?) {
        val field = clazz.getDeclaredField(name)
        field.isAccessible = true
        field.set(null, value)
    }

    private fun hookHasSystemFeature(param: PackageReadyParam) {
        val classLoader = param.classLoader
        val clazz = try {
            Class.forName(CLASS_APPLICATION_PACKAGE_MANAGER, false, classLoader)
        } catch (t: Throwable) {
            warn("Failed to find $CLASS_APPLICATION_PACKAGE_MANAGER", t)
            return
        }

        val hooker = XposedInterface.Hooker { chain ->
            val args = chain.args
            val feature = args.firstOrNull() as? String
            when {
                feature == null -> chain.proceed()
                feature in finalFeaturesToSpoof -> {
                    verbose("TRUE - feature: $feature")
                    true
                }
                overrideCustomROMLevels && feature in featuresNotToSpoof -> {
                    verbose("FALSE - feature: $feature")
                    false
                }
                else -> {
                    verbose("NO_CHANGE - feature: $feature")
                    chain.proceed()
                }
            }
        }

        try {
            val m1 = clazz.getDeclaredMethod("hasSystemFeature", String::class.java)
            hook(m1).intercept(hooker)
        } catch (t: Throwable) {
            warn("Failed to hook hasSystemFeature(String)", t)
        }
        try {
            val m2 = clazz.getDeclaredMethod(
                "hasSystemFeature",
                String::class.java,
                Int::class.javaPrimitiveType
            )
            hook(m2).intercept(hooker)
        } catch (t: Throwable) {
            warn("Failed to hook hasSystemFeature(String, int)", t)
        }
    }
}
