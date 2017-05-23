package dynoapps.exchange_rates.alarm;

import java.io.Serializable;
import java.util.Comparator;

import dynoapps.exchange_rates.interfaces.ValueType;

/**
 * Created by erdemmac on 13/12/2016.
 */

public class Alarm implements Serializable {
    public Float val;
    public boolean is_above = false;
    public boolean is_enabled = true;

    /**
     * {@link dynoapps.exchange_rates.model.rates.IRate}
     */
    public int rate_type;

    /**
     * {@link dynoapps.exchange_rates.data.CurrencyType}
     */
    public int source_type;

    /**
     * {@link dynoapps.exchange_rates.interfaces.ValueType}
     */
    public int value_type = ValueType.NONE;

    public static int getPushId(Alarm alarm) {
        return alarm.rate_type * 100 + alarm.source_type;
    }

    private static int compare(int x, int y) {
        return x < y ? -1
                : x > y ? 1
                : 0;
    }

    private static int compare(float x, float y) {
        return x < y ? -1
                : x > y ? 1
                : 0;
    }

    public static Comparator<Alarm> COMPARATOR = new Comparator<Alarm>() {

        @Override
        public int compare(Alarm first, Alarm second) {

            int i = Alarm.compare(first.source_type, second.source_type);
            if (i != 0) return i;

            i = Alarm.compare(first.rate_type, second.rate_type);
            if (i != 0) return i;

            i = Alarm.compare(first.val, second.val);
            return i;
            
        }
    };
}
