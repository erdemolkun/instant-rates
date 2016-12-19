package dynoapps.exchange_rates.event;

/**
 * Created by erdemmac on 13/12/2016.
 */

public class AlarmUpdateEvent {
    public boolean is_added;
    public boolean is_update;

    public AlarmUpdateEvent(boolean is_added, boolean is_update) {
        this.is_added = is_added;
        this.is_update = is_update;
    }
}
