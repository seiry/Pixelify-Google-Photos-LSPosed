package seiry.xposed.pixelifygooglephotos.lsposed

import android.app.Application
import android.util.Log
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import kotlinx.coroutines.CompletableDeferred

/**
 * UI app process Application.
 *
 * Registers with LSPosed's [XposedServiceHelper] so we can talk to the framework
 * (read/write remote preferences, query scope, etc.). The bound [XposedService]
 * is exposed via [awaitService] / [serviceOrNull] for activities to consume.
 *
 * The hooked process (Google Photos) does NOT go through this class — there the
 * module extends [io.github.libxposed.api.XposedModule] and calls
 * `getRemotePreferences("settings")` directly.
 */
class App : Application() {

    companion object {
        private const val TAG = "PixelifyApp"

        private val serviceDeferred = CompletableDeferred<XposedService>()

        /** Null until LSPosed binds the service. */
        val serviceOrNull: XposedService?
            get() = if (serviceDeferred.isCompleted && !serviceDeferred.isCancelled)
                serviceDeferred.getCompleted() else null

        /** Suspends until LSPosed binds the service. */
        suspend fun awaitService(): XposedService = serviceDeferred.await()

        /**
         * Wait for the LSPosed service and attach the remote prefs into [Pref].
         * Returns true on success, false on timeout (LSPosed not installed or
         * module not enabled). Activities should call this from `onCreate`
         * before reading prefs — it short-circuits once Pref is already bound.
         */
        suspend fun bindPrefs(timeoutMs: Long = 3_000): Boolean {
            if (Pref.isBound) return true
            return try {
                kotlinx.coroutines.withTimeout(timeoutMs) {
                    Pref.bind(awaitService().getRemotePreferences(Pref.GROUP))
                }
                true
            } catch (_: Throwable) {
                false
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        XposedServiceHelper.registerListener(object : XposedServiceHelper.OnServiceListener {
            override fun onServiceBind(service: XposedService) {
                Log.i(TAG, "Xposed service bound: ${service.frameworkName} ${service.frameworkVersion}")
                if (!serviceDeferred.isCompleted) serviceDeferred.complete(service)
            }

            override fun onServiceDied(service: XposedService) {
                Log.w(TAG, "Xposed service died")
            }
        })
    }
}
