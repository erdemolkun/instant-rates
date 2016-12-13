package dynoapps.exchange_rates.alarm;

import java.io.Serializable;

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
     * {@link dynoapps.exchange_rates.data.CurrencySource.Type}
     */
    public int source_type;
}
