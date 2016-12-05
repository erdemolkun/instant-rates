package dynoapps.exchange_rates.time;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import dynoapps.exchange_rates.App;
import dynoapps.exchange_rates.Prefs;

/**
 * Created by erdemmac on 03/12/2016.
 */

public class TimeIntervalManager {

    private static int selected_interval_index = getPrefIndex();
    private static long prefInterval = Prefs.getInterval(App.context());

    private static int getPrefIndex() {
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

    public static int getSelectedIndex() {
        return selected_interval_index;
    }


    public static void setSelectedIndex(int index) {
        if (getDefaultIntervals().size() > index) {
            selected_interval_index = index;
        } else {
            selected_interval_index = getDefaultIntervals().size();
        }
        Prefs.saveInterval(App.context(), getIntervalInMiliseconds());
    }

    public static long getIntervalInMiliseconds() {
        if (selected_interval_index < 0) {
            if (prefInterval < 0) {
                return getDefaultIntervals().get(0).to(TimeUnit.MILLISECONDS);
            } else {
                return prefInterval;
            }
        }
        return getDefaultIntervals().get(selected_interval_index).to(TimeUnit.MILLISECONDS);
    }


    private static ArrayList<TimeInterval> intervals;

    public static ArrayList<TimeInterval> getDefaultIntervals() {
        if (intervals == null || intervals.size() <= 0) {
            intervals = new ArrayList<>();
            intervals.add(new TimeInterval(3, TimeUnit.SECONDS));
            intervals.add(new TimeInterval(5, TimeUnit.SECONDS));
            intervals.add(new TimeInterval(10, TimeUnit.SECONDS));
            intervals.add(new TimeInterval(30, TimeUnit.SECONDS));
            intervals.add(new TimeInterval(1, TimeUnit.MINUTES));
            intervals.add(new TimeInterval(3, TimeUnit.MINUTES));
            intervals.add(new TimeInterval(5, TimeUnit.MINUTES));
            intervals.add(new TimeInterval(10, TimeUnit.MINUTES));
            intervals.add(new TimeInterval(15, TimeUnit.MINUTES));
        }
        return intervals;
    }

    public static class TimeInterval {
        public TimeInterval(int value, TimeUnit timeUnit) {
            this.value = value;
            this.timeUnit = timeUnit;
        }

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }

        public long to(TimeUnit unit) {
            return unit.convert(value, timeUnit);
        }

        @Override
        public String toString() {
            // // TODO: 03/12/2016 localization
            if (timeUnit == TimeUnit.SECONDS) {
                return value + " " + "sn";
            } else if (timeUnit == TimeUnit.MINUTES) {
                return value + " " + "dk";
            } else {
                return value + "";
            }
        }

        private TimeUnit timeUnit;
        private int value;

    }
}