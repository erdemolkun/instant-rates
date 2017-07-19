package dynoapps.exchange_rates.alarm;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import dynoapps.exchange_rates.App;
import dynoapps.exchange_rates.AppDatabase;
import dynoapps.exchange_rates.AppExecutors;
import dynoapps.exchange_rates.Prefs;
import dynoapps.exchange_rates.SourcesManager;
import dynoapps.exchange_rates.data.CurrencySource;
import dynoapps.exchange_rates.event.AlarmUpdateEvent;
import dynoapps.exchange_rates.util.CollectionUtils;

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
    private List<Alarm> cachedAlarms;

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
                            if (!cachedAlarms.contains(alarm)) {
                                cachedAlarms.add(alarm);
                            }
                            for (AlarmCallback alarmCallback : alarmCallbacks) {
                                alarmCallback.onAdded(alarm);
                            }
                            EventBus.getDefault().post(new AlarmUpdateEvent(alarm, true, false));
                        }
                    }
                });
            }
        });
    }

    public void updateAlarm(final Alarm alarm) {
        appExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final long id = AppDatabase.getInstance(App.context()).alarm().update(alarm);
                // notify on the main thread
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (!cachedAlarms.contains(alarm)) {
                            cachedAlarms.add(alarm);
                        }
                        if (id >= 0) {
                            for (AlarmCallback alarmCallback : alarmCallbacks) {
                                alarmCallback.onAdded(alarm); //TODO
                            }
                        }
                        EventBus.getDefault().post(new AlarmUpdateEvent(alarm, true, false));
                    }
                });
            }
        });
    }

    public List<Alarm> getCachedAlarms() {
        return cachedAlarms;
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
                        if (cachedAlarms.contains(alarm)) {
                            cachedAlarms.remove(alarm);
                        }
                        if (id >= 0) {
                            for (AlarmCallback alarmCallback : alarmCallbacks) {
                                alarmCallback.onRemove();
                            }
                        }
                        EventBus.getDefault().post(new AlarmUpdateEvent(null, false, false));
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
                cachedAlarms = alarms;
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

    private Boolean alarmEnabled = null;

    public boolean hasAnyActive() {
        if (!isEnabled()) return false;
        if (CollectionUtils.isNullOrEmpty(cachedAlarms)) return false; // TODO
        for (Alarm alarm : cachedAlarms) {
            if (alarm.is_enabled) {
                for (CurrencySource source : SourcesManager.getCurrencySources()) {
                    if (source.isEnabled() && source.getType() == alarm.source_type) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isEnabled() {
        if (alarmEnabled == null) alarmEnabled = Prefs.isAlarmEnabled();
        return alarmEnabled;
    }

    public void updateEnabled(boolean alarmEnabled) {
        this.alarmEnabled = alarmEnabled;
        Prefs.saveAlarmEnabled(alarmEnabled);
    }

    public interface AlarmCallback {
        void onAdded(Alarm alarm);

        void onRemove();

        void onFetched(List<Alarm> alarms);
    }
}
