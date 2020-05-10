package dynoapps.exchange_rates.time;

import android.app.Activity;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AlertDialog;
import dynoapps.exchange_rates.App;
import dynoapps.exchange_rates.Prefs;
import dynoapps.exchange_rates.R;
import dynoapps.exchange_rates.util.CollectionUtils;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by erdemmac on 03/12/2016.
 */

public final class TimeIntervalManager {

    private static final int DEFAULT_INTERVAL_INDEX = 2;
    private static final int INTERVAL_SERVICE_INDEX = 4;
    private static final long PREF_INTERVAL_MILIS = Prefs.getInterval(App.context());
    private static ArrayList<TimeInterval> intervals;
    private static Integer selected_interval_index_user = getSelectedIndexViaPrefs();
    private static boolean isAlarmMode = false;
    private static int user_selected_item_index = -1;

    private static final PublishSubject<Boolean> intervalUpdates = PublishSubject.create();

    private static boolean isAlarmMode() {
        return isAlarmMode;
    }

    public static void setAlarmMode(boolean enabled) {
        TimeIntervalManager.isAlarmMode = enabled;
    }

    public static void updateIntervalsToUIMode() {
        intervalUpdates.onNext(true);
    }

    private static ArrayList<TimeInterval> getDefaultIntervals() {
        if (CollectionUtils.isNullOrEmpty(intervals)) {
            intervals = new ArrayList<>();
            intervals.add(new TimeInterval(3, TimeUnit.SECONDS));
            intervals.add(new TimeInterval(5, TimeUnit.SECONDS));
            intervals.add(new TimeInterval(10, TimeUnit.SECONDS));
            intervals.add(new TimeInterval(15, TimeUnit.SECONDS));
            intervals.add(new TimeInterval(20, TimeUnit.SECONDS));
            intervals.add(new TimeInterval(30, TimeUnit.SECONDS));
            intervals.add(new TimeInterval(1, TimeUnit.MINUTES));
        }
        return intervals;
    }

    public static PublishSubject<Boolean> getIntervalUpdates() {
        return intervalUpdates;
    }

    public static String getSelectionStr() {
        return getDefaultIntervals().get(getSelectedIndex()).toString();
    }

    public static void selectInterval(final Activity activity) {

        final ArrayList<TimeInterval> timeIntervals = TimeIntervalManager.getDefaultIntervals();
        user_selected_item_index = TimeIntervalManager.getSelectedIndex();
        String[] time_values = new String[CollectionUtils.size(timeIntervals)];
        for (int i = 0; i < time_values.length; i++) {
            time_values[i] = timeIntervals.get(i).toString();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppTheme_Alert);

        builder.setSingleChoiceItems(time_values, user_selected_item_index, (dialogInterface, i) -> user_selected_item_index = i);

        builder.setCancelable(true);
        builder.setTitle(R.string.select_time_interval);
        builder.setPositiveButton(R.string.apply, (dialog, which) -> {
            if (user_selected_item_index != TimeIntervalManager.getSelectedIndex()) {
                TimeIntervalManager.updateUserInvertalSelection(user_selected_item_index);
                intervalUpdates.onNext(false);
            }
        });

        builder.setNegativeButton(R.string.dismiss, null);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setWindowAnimations(R.style.DialogAnimationFade);
        }
        dialog.show();
    }

    private static int getSelectedIndexViaPrefs() {
        long saved = Prefs.getInterval(App.context());
        if (saved < 0)
            return -1;
        int index = 0;
        for (TimeInterval timeInterval : getDefaultIntervals()) {
            if (timeInterval.to(TimeUnit.MILLISECONDS) == saved) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private static int getSelectedIndex() {
        if (!isAlarmMode()) {
            if (selected_interval_index_user < 0) {
                return DEFAULT_INTERVAL_INDEX;
            }
            return selected_interval_index_user;
        } else {
            return selected_interval_index_user > INTERVAL_SERVICE_INDEX ? selected_interval_index_user : INTERVAL_SERVICE_INDEX;
        }
    }


    public static void updateUserInvertalSelection(int index) {
        selected_interval_index_user = Math.min(CollectionUtils.size(getDefaultIntervals()), index);
        Prefs.saveInterval(App.context(), getDefaultIntervals().get(selected_interval_index_user).to(TimeUnit.MILLISECONDS));
    }

    /**
     * Returns time interval for polling in  {@link TimeUnit#MILLISECONDS} milliseconds
     */
    public static long getPollingInterval() {
        if (!isAlarmMode() && selected_interval_index_user < 0) {
            if (PREF_INTERVAL_MILIS < 0) {
                return getDefaultIntervals().get(DEFAULT_INTERVAL_INDEX).to(TimeUnit.MILLISECONDS);
            } else {
                return PREF_INTERVAL_MILIS;
            }
        } else {
            return getDefaultIntervals().get(getSelectedIndex()).to(TimeUnit.MILLISECONDS);
        }
    }
}
