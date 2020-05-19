package dynoapps.exchange_rates.alarm;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import dynoapps.exchange_rates.AppDatabase;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by erdemmac on 19/07/2017.
 */

public class LocalAlarmsDataSource implements AlarmsDataSource {

    private final AlarmDao alarmDao;

    public LocalAlarmsDataSource(@NonNull Context context) {
        alarmDao = AppDatabase.getInstance(context.getApplicationContext()).alarm();
    }

    @SuppressLint("CheckResult")
    @Override
    public void getAlarms(final AlarmsLoadCallback alarmsLoadCallback) {
        alarmDao.list().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((alarms, throwable) -> {
            if (alarmsLoadCallback != null) {
                alarmsLoadCallback.onAlarmsLoaded(alarms);
            }
        });
    }

    @SuppressLint("CheckResult")
    @Override
    public void saveAlarm(@NonNull final Alarm alarm, final AlarmUpdateInsertCallback alarmUpdateInsertCallback) {
        alarmDao.insert(alarm).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe((aLong, throwable) -> {
                    alarm.id = aLong;
                    alarmUpdateInsertCallback.onAlarmUpdate(alarm);
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void deleteAlarm(@NonNull final Alarm alarm, final AlarmUpdateInsertCallback alarmUpdateInsertCallback) {
        alarmDao.deleteById(alarm.id).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe((integer, throwable) -> {
                    alarmUpdateInsertCallback.onAlarmUpdate(alarm);
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void updateAlarm(@NonNull final Alarm alarm, final AlarmUpdateInsertCallback alarmUpdateInsertCallback) {
        alarmDao.update(alarm).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe((integer, throwable) -> {
                    alarmUpdateInsertCallback.onAlarmUpdate(alarm);
                });
    }

    @Override
    public void refreshAlarms() {
        // Not required because the {@link AlarmsRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }
}
