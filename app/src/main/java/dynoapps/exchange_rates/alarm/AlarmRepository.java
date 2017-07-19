package dynoapps.exchange_rates.alarm;

import java.util.ArrayList;
import java.util.List;

import dynoapps.exchange_rates.App;
import dynoapps.exchange_rates.AppDatabase;
import dynoapps.exchange_rates.AppExecutors;

/**
 * Created by erdemmac on 19/07/2017.
 */

public class AlarmRepository {

    private static AlarmRepository instance;

    public static AlarmRepository getInstance() {
        if (instance == null) instance = new AlarmRepository();
        return instance;
    }

    private AppExecutors appExecutors;

    private AlarmRepository() {
        appExecutors = new AppExecutors();
    }

    public void saveAlarm(final Alarm alarm) {
        appExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final long id = AppDatabase.getInstance(App.context()).alarm().insert(alarm);
                // notify on the main thread
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (id >= 0) {
                            for (AlarmCallback alarmCallback : alarmCallbacks) {
                                alarmCallback.onAdded(alarm);
                            }
                        }
                    }
                });
            }
        });
    }

    public void removeAlarm(final Alarm alarm) {
        appExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final long id = AppDatabase.getInstance(App.context()).alarm().deleteById(alarm.id);
                // notify on the main thread
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (id >= 0) {
                            for (AlarmCallback alarmCallback : alarmCallbacks) {
                                alarmCallback.onRemove();
                            }
                        }
                    }
                });
            }
        });
    }

    public void fetchAlarms() {
        appExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<Alarm> alarms = AppDatabase.getInstance(App.context()).alarm().list();
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        for (AlarmCallback alarmCallback : alarmCallbacks) {
                            alarmCallback.onFetched(alarms);
                        }
                    }
                });
            }
        });
    }

    private List<AlarmCallback> alarmCallbacks = new ArrayList<>();

    public void addCallback(AlarmCallback alarmCallback) {
        if (alarmCallbacks.contains(alarmCallback)) return;
        alarmCallbacks.add(alarmCallback);
    }

    public void remove(AlarmCallback alarmCallback) {
        if (!alarmCallbacks.contains(alarmCallback)) return;
        alarmCallbacks.remove(alarmCallback);
    }

    public interface AlarmCallback {
        void onAdded(Alarm alarm);

        void onRemove();

        void onFetched(List<Alarm> alarms);
    }
}
