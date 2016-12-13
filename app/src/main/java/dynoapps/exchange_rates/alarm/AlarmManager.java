package dynoapps.exchange_rates.alarm;

import com.google.gson.GsonBuilder;

import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import dynoapps.exchange_rates.Prefs;
import dynoapps.exchange_rates.event.AlarmUpdateEvent;

/**
 * Created by erdemmac on 13/12/2016.
 */

public class AlarmManager {

    public static final int MAX_ALARM_COUNT = 2;

    public static AlarmsHolder alarmsHolder;

    public static boolean addAlarm(Alarm alarm) {
        alarmsHolder = getAlarmsHolder();
        if (alarmsHolder.alarms == null)
            alarmsHolder.alarms = new ArrayList<>();
        if (alarmsHolder.alarms.size() >= MAX_ALARM_COUNT)
            return false;
        alarmsHolder.alarms.add(alarm);
        saveAlarms();
        return true;
    }

    public static AlarmsHolder getAlarmsHolder() {
        if (alarmsHolder == null) {
            String alarm_json = Prefs.getAlarms();
            if (!TextUtils.isEmpty(alarm_json)) {
                try {
                    alarmsHolder = new GsonBuilder().create().fromJson(alarm_json, AlarmsHolder.class);
                } catch (Exception ignored) {
                }
            }
            alarmsHolder = new AlarmsHolder(new ArrayList<Alarm>());
        }
        return alarmsHolder;
    }

    static void saveAlarms() {
        String alarms_json = new GsonBuilder().create().toJson(alarmsHolder);
        Prefs.saveAlarms(alarms_json);
        EventBus.getDefault().post(new AlarmUpdateEvent());
    }

}
