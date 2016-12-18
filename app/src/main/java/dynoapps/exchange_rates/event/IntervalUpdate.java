package dynoapps.exchange_rates.event;

/**
 * Created by erdemmac on 05/12/2016.
 */

public class IntervalUpdate {
    public IntervalUpdate(boolean is_immediate) {
        this.is_immediate = is_immediate;
    }

    public boolean is_immediate = false;
}
