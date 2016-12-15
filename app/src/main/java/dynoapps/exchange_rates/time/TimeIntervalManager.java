package dynoapps.exchange_rates.time;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import dynoapps.exchange_rates.App;
import dynoapps.exchange_rates.Prefs;
import dynoapps.exchange_rates.R;
import dynoapps.exchange_rates.event.IntervalUpdate;

/**
 * Created by erdemmac on 03/12/2016.
 */

public final class TimeIntervalManager {

    private static final int DEFAULT_INTERVAL_INDEX = 2;
    private static final int INTERVAL_SERVICE_INDEX = 4;

    private static Integer selected_interval_index_user = getSelectedIndexViaPrefs();
    private static long pref_interval_milis = Prefs.getInterval(App.context());
    private static ArrayList<TimeInterval> intervals;

    private static boolean isUIMode = false;

    private static boolean isUIMode() {
        return isUIMode;
    }

    public static void changeMode(boolean isUI) {
        TimeIntervalManager.isUIMode = isUI;
    }

    private static ArrayList<TimeInterval> getDefaultIntervals() {
        if (intervals == null || intervals.size() == 0) {
            intervals = new ArrayList<>();
            intervals.add(new TimeInterval(3, TimeUnit.SECONDS));
            intervals.add(new TimeInterval(5, TimeUnit.SECONDS));
            intervals.add(new TimeInterval(10, TimeUnit.SECONDS));
            intervals.add(new TimeInterval(20, TimeUnit.SECONDS));
            intervals.add(new TimeInterval(30, TimeUnit.SECONDS));
            intervals.add(new TimeInterval(1, TimeUnit.MINUTES));
            intervals.add(new TimeInterval(2, TimeUnit.MINUTES));
            intervals.add(new TimeInterval(3, TimeUnit.MINUTES));
        }
        return intervals;
    }

    private static int user_selected_item_index = -1;

    public static String getSelectionStr() {
        return getDefaultIntervals().get(getSelectedIndex()).toString();
    }

    public static void selectInterval(final Activity activity) {

        final ArrayList<TimeInterval> timeIntervals = TimeIntervalManager.getDefaultIntervals();
        user_selected_item_index = TimeIntervalManager.getSelectedIndex();
        String[] time_values = new String[timeIntervals.size()];
        for (int i = 0; i < time_values.length; i++) {
            time_values[i] = timeIntervals.get(i).toString();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setSingleChoiceItems(time_values, user_selected_item_index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                user_selected_item_index = i;
            }
        });

        builder.setCancelable(true);
        builder.setTitle(R.string.select_time_interval);
        builder.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (user_selected_item_index != TimeIntervalManager.getSelectedIndex()) {
                    TimeIntervalManager.updateUserInvertalSelection(user_selected_item_index);
                    EventBus.getDefault().post(new IntervalUpdate());
                }
            }
        });

        builder.setNegativeButton(R.string.dismiss, null);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setWindowAnimations(R.style.DialogAnimationFade);
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
        if (isUIMode()) {
            if (selected_interval_index_user < 0) {
                return DEFAULT_INTERVAL_INDEX;
            }
            return selected_interval_index_user;
        } else {
            return selected_interval_index_user > INTERVAL_SERVICE_INDEX ? selected_interval_index_user : INTERVAL_SERVICE_INDEX;
        }
    }


    public static void updateUserInvertalSelection(int index) {
        if (getDefaultIntervals().size() > index) {
            selected_interval_index_user = index;
        } else {
            selected_interval_index_user = getDefaultIntervals().size();
        }
        Prefs.saveInterval(App.context(), getDefaultIntervals().get(selected_interval_index_user).to(TimeUnit.MILLISECONDS));
    }

    /**
     * Returns time interval for polling in  {@link TimeUnit#MILLISECONDS} milisecons
     */
    public static long getPollingInterval() {
        if (isUIMode() && selected_interval_index_user < 0) {
            if (pref_interval_milis < 0) {
                return getDefaultIntervals().get(DEFAULT_INTERVAL_INDEX).to(TimeUnit.MILLISECONDS);
            } else {
                return pref_interval_milis;
            }
        } else {
            return getDefaultIntervals().get(getSelectedIndex()).to(TimeUnit.MILLISECONDS);
        }
    }
}
