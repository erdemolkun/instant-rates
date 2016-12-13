package dynoapps.exchange_rates.alarm;

import java.io.Serializable;

/**
 * Created by erdemmac on 13/12/2016.
 */

public class Alarm implements Serializable {
    public Float val;
    public boolean is_above = false;
    public boolean is_enabled = true;
    public int rate_type;
}
