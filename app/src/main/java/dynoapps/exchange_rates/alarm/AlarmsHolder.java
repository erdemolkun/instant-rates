package dynoapps.exchange_rates.alarm;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by erdemmac on 13/12/2016.
 */

public class AlarmsHolder implements Serializable {
    public ArrayList<Alarm> alarms;

    AlarmsHolder(ArrayList<Alarm> alarms) {
        this.alarms = alarms;
    }
}
