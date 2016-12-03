package dynoapps.exchange_rates.time;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by erdemmac on 03/12/2016.
 */

public class TimeIntervalManager {

    private static int selected_interval_index = -1;

    public static int getSelectedIndex() {
        return selected_interval_index;
    }

    private static final TimeInterval DEFAULT_INTERVAL = getDefaultIntervals().get(0);

    public static void setSelectedIndex(int index) {
        if (getDefaultIntervals().size() > index) {
            selected_interval_index = index;
        } else {
            selected_interval_index = getDefaultIntervals().size();
        }
    }

    public static int getIntervalInMiliseconds() {
        // // TODO: 03/12/2016  add Persistent
        if (selected_interval_index < 0) {
            return (int) DEFAULT_INTERVAL.to(TimeUnit.MILLISECONDS);
        }
        return (int) getDefaultIntervals().get(selected_interval_index).to(TimeUnit.MILLISECONDS);
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
