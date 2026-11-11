package dynoapps.exchange_rates;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import dynoapps.exchange_rates.alarm.AlarmsRepository;
import dynoapps.exchange_rates.provider.IPollingSource;
import dynoapps.exchange_rates.service.RatePollingService;
import dynoapps.exchange_rates.time.TimeIntervalManager;
import dynoapps.exchange_rates.util.L;

/**
 * Tracks whether any activity of the app is visible and switches rate polling between UI mode and
 * background alarm mode.
 * <p>
 * Going to the background is handled in the {@code onStop} of the last visible activity. That is
 * the visible-to-background transition during which Android 12+ still allows starting a
 * foreground service. It also covers pressing back on the root activity, which on Android 12+
 * moves the task to the back instead of destroying the activity.
 */
class AppVisibilityTracker implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "AppVisibilityTracker";

    private int startedActivities = 0;

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        startedActivities++;
        if (startedActivities == 1) {
            onAppForeground(activity);
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        startedActivities = Math.max(0, startedActivities - 1);
        if (startedActivities == 0 && !activity.isChangingConfigurations()) {
            onAppBackground(activity);
        }
    }

    private void onAppForeground(Context context) {
        context.stopService(new Intent(context, RatePollingService.class));
        TimeIntervalManager.setAlarmMode(false);
        if (ProvidersManager.getInstance().getPollingSources() != null) {
            ProvidersManager.getInstance().startOrStopSources();
        }
    }

    private void onAppBackground(Context context) {
        AlarmsRepository alarmsRepository = App.getInstance().provideAlarmsRepository();
        ProvidersManager providersManager = ProvidersManager.getInstance();
        if (!alarmsRepository.hasAnyActive() || providersManager.getPollingSources() == null) {
            providersManager.stopAll();
            return;
        }

        TimeIntervalManager.setAlarmMode(true);
        for (IPollingSource pollingSource : providersManager.getPollingSources()) {
            pollingSource.stopNonAlarmSources();
        }
        try {
            ContextCompat.startForegroundService(context, new Intent(context, RatePollingService.class));
        } catch (IllegalStateException e) {
            // ForegroundServiceStartNotAllowedException on Android 12+ if the system no longer
            // considers the app as leaving a visible state. Alarms then only fire while open.
            L.ex(e, TAG);
            providersManager.stopAll();
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }
}
