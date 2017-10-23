package dynoapps.exchange_rates.alarm;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by erdemmac on 19/07/2017.
 */

public interface AlarmsDataSource {

    void getAlarms(AlarmsLoadCallback alarmsLoadCallback);

    void saveAlarm(@NonNull Alarm alarm, AlarmUpdateInsertCallback alarmUpdateInsertCallback);

    void deleteAlarm(@NonNull Alarm alarm, AlarmUpdateInsertCallback alarmUpdateInsertCallback);

    void updateAlarm(@NonNull Alarm alarm, AlarmUpdateInsertCallback alarmUpdateInsertCallback);

    void refreshAlarms();

    interface AlarmsLoadCallback {

        void onAlarmsLoaded(List<Alarm> alarms);

        // TODO implement error case
    }

    interface AlarmUpdateInsertCallback {

        void onAlarmUpdate(Alarm alarm);
    }
}
