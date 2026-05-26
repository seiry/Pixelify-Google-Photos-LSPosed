package seiry.xposed.pixelifygooglephotos.lsposed

import android.content.SharedPreferences

/**
 * Process-aware preferences singleton, backed by LSPosed remote preferences.
 *
 * Both the UI app (this activity-bearing process) and the hooked Google Photos
 * process attach a different [SharedPreferences] instance to the same backing
 * store via LSPosed:
 *
 *  - **UI**: `XposedService.getRemotePreferences("settings")`, attached after
 *    the LSPosed service binder arrives — see [App.awaitService].
 *  - **Hook** (`ModuleMain`): `XposedModule.getRemotePreferences("settings")`,
 *    attached from `onPackageReady` (always available there).
 *
 * Reads before [bind] return the supplied default. That covers the
 * "LSPosed not installed / module not enabled" case — the rest of the UI
 * already handles defaults gracefully.
 */
object Pref {

    /** Group name used in the underlying LSPosed remote preferences. */
    const val GROUP = "settings"

    @Volatile private var backing: SharedPreferences? = null

    /** Attach the [SharedPreferences] returned by LSPosed for this process. */
    fun bind(prefs: SharedPreferences) {
        backing = prefs
    }

    /** True after [bind] has been called. */
    val isBound: Boolean get() = backing != null

    fun getString(key: String, defaultValue: String? = null): String? =
        backing?.getString(key, defaultValue) ?: defaultValue

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean =
        backing?.getBoolean(key, defaultValue) ?: defaultValue

    fun getInt(key: String, defaultValue: Int = 0): Int =
        backing?.getInt(key, defaultValue) ?: defaultValue

    fun getStringSet(key: String, defaultValue: Set<String>? = null): Set<String>? =
        backing?.getStringSet(key, defaultValue) ?: defaultValue

    fun putString(key: String, value: String?) {
        backing?.edit()?.putString(key, value)?.apply()
    }

    fun putBoolean(key: String, value: Boolean) {
        backing?.edit()?.putBoolean(key, value)?.apply()
    }

    fun putInt(key: String, value: Int) {
        backing?.edit()?.putInt(key, value)?.apply()
    }

    fun putStringSet(key: String, values: Set<String>?) {
        backing?.edit()?.putStringSet(key, values)?.apply()
    }
}
