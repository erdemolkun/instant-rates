package dynoapps.exchange_rates.alarm;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import dynoapps.exchange_rates.AppDatabase;
import dynoapps.exchange_rates.AppExecutors;

/**
 * Created by erdemmac on 19/07/2017.
 */

public class LocalAlarmsDataSource implements AlarmsDataSource {

    private AppExecutors appExecutors;

    private AlarmDao alarmDao;

    public LocalAlarmsDataSource(@NonNull Context context) {
        appExecutors = new AppExecutors();
        alarmDao = AppDatabase.getInstance(context.getApplicationContext()).alarm();
    }

    @Override
    public void getAlarms(final AlarmsLoadCallback alarmsLoadCallback) {
        appExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<Alarm> alarms = alarmDao.list();
                if (alarmsLoadCallback != null) {
                    appExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            alarmsLoadCallback.onAlarmsLoaded(alarms);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void saveAlarm(@NonNull final Alarm alarm, final AlarmUpdateInsertCallback alarmUpdateInsertCallback) {
        appExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                alarm.id = alarmDao.insert(alarm);
                alarmUpdateInsertCallback.onAlarmUpdate(alarm);
            }
        });
    }

    @Override
    public void deleteAlarm(@NonNull final Alarm alarm, final AlarmUpdateInsertCallback alarmUpdateInsertCallback) {
        appExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                alarmDao.deleteById(alarm.id);
                alarmUpdateInsertCallback.onAlarmUpdate(alarm);
            }
        });
    }

    @Override
    public void updateAlarm(@NonNull final Alarm alarm, final AlarmUpdateInsertCallback alarmUpdateInsertCallback) {
        appExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                alarmDao.update(alarm);
                alarmUpdateInsertCallback.onAlarmUpdate(alarm);
            }
        });
    }

    @Override
    public void refreshAlarms() {
        // Not required because the {@link AlarmsRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }
}
