package dynoapps.exchange_rates

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import dynoapps.exchange_rates.service.RatePollingService
import dynoapps.exchange_rates.time.TimeIntervalManager
import timber.log.Timber

/**
 * Tracks whether any activity of the app is visible and switches rate polling between UI mode and
 * background alarm mode.
 *
 * Going to the background is handled in the `onStop` of the last visible activity. That is the
 * visible-to-background transition during which Android 12+ still allows starting a foreground
 * service. It also covers pressing back on the root activity, which on Android 12+ moves the task
 * to the back instead of destroying the activity.
 */
internal class AppVisibilityTracker : Application.ActivityLifecycleCallbacks {

    private var startedActivities = 0

    override fun onActivityStarted(activity: Activity) {
        startedActivities++
        if (startedActivities == 1) onAppForeground(activity)
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivities = maxOf(0, startedActivities - 1)
        if (startedActivities == 0 && !activity.isChangingConfigurations) onAppBackground(activity)
    }

    private fun onAppForeground(context: Context) {
        context.stopService(Intent(context, RatePollingService::class.java))
        TimeIntervalManager.setAlarmMode(false)
        if (ProvidersManager.pollingSources != null) {
            ProvidersManager.startOrStopSources()
        }
    }

    private fun onAppBackground(context: Context) {
        val pollingSources = ProvidersManager.pollingSources
        if (pollingSources == null || !App.getInstance().provideAlarmsRepository().hasAnyActive()) {
            ProvidersManager.stopAll()
            return
        }

        TimeIntervalManager.setAlarmMode(true)
        pollingSources.forEach { it.stopNonAlarmSources() }
        try {
            ContextCompat.startForegroundService(context, Intent(context, RatePollingService::class.java))
        } catch (e: IllegalStateException) {
            // ForegroundServiceStartNotAllowedException on Android 12+ if the system no longer
            // considers the app as leaving a visible state. Alarms then only fire while open.
            Timber.tag(TAG).e(e, "Starting the polling service failed")
            ProvidersManager.stopAll()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit

    private companion object {
        const val TAG = "AppVisibilityTracker"
    }
}
