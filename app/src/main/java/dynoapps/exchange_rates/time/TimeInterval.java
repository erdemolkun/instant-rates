package dynoapps.exchange_rates.time;

import java.util.concurrent.TimeUnit;

import dynoapps.exchange_rates.App;
import dynoapps.exchange_rates.R;

/**
 * Created by erdemmac on 14/12/2016.
 */

public class TimeInterval {
    private TimeUnit timeUnit;
    private int value;

    TimeInterval(int value, TimeUnit timeUnit) {
        this.value = value;
        this.timeUnit = timeUnit;
    }

    public long to(TimeUnit unit) {
        return unit.convert(value, timeUnit);
    }

    @Override
    public String toString() {
        if (timeUnit == TimeUnit.SECONDS) {
            return App.context().getResources().getQuantityString(R.plurals.sec_short, value, value);
        } else if (timeUnit == TimeUnit.MINUTES) {
            return App.context().getResources().getQuantityString(R.plurals.min_short, value, value);
        } else {
            return value + "";
        }
    }

}