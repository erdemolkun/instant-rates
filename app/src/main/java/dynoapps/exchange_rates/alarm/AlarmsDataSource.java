package dynoapps.exchange_rates.alarm;


import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Single;

/**
 * Created by erdemmac on 19/07/2017.
 */

public interface AlarmsDataSource {

    void getAlarms(AlarmsLoadCallback alarmsLoadCallback);

    void saveAlarm(@NonNull Alarm alarm, AlarmUpdateInsertCallback alarmUpdateInsertCallback);

    Single<Alarm> deleteAlarm(@NonNull Alarm alarm);

    Single<Alarm> updateAlarm(@NonNull Alarm alarm);

    void refreshAlarms();

    interface AlarmsLoadCallback {

        void onAlarmsLoaded(List<Alarm> alarms);

        // TODO implement error case
    }

    interface AlarmUpdateInsertCallback {

        void onAlarmUpdate(Alarm alarm);
    }
}
