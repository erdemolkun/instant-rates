package dynoapps.exchange_rates.alarm;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dynoapps.exchange_rates.Prefs;
import dynoapps.exchange_rates.SourcesManager;
import dynoapps.exchange_rates.data.CurrencySource;

/**
 * Created by erdemmac on 19/07/2017.
 */

public class AlarmsRepository implements AlarmsDataSource {

    private static AlarmsRepository INSTANCE = null;

    private AlarmsDataSource localAlarmsDataSource;


    private Boolean alarmEnabled = null;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    boolean mCacheIsDirty = false;

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    Map<Long, Alarm> mCachedAlarms;


    public static AlarmsRepository getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AlarmsRepository(new LocalAlarmsDataSource(context));
        }
        return INSTANCE;
    }

    private AlarmsRepository(AlarmsDataSource localAlarmsDataSource) {
        this.localAlarmsDataSource = localAlarmsDataSource;
    }

    @Override
    public void getAlarms(final AlarmsLoadCallback callback) {
        // Respond immediately with cache if available and not dirty
        if (mCachedAlarms != null && !mCacheIsDirty) {
            callback.onAlarmsLoaded(new ArrayList<>(mCachedAlarms.values()));
            return;
        }
        localAlarmsDataSource.getAlarms(new AlarmsLoadCallback() {
            @Override
            public void onAlarmsLoaded(List<Alarm> alarms) {
                refreshCache(alarms);
                callback.onAlarmsLoaded(new ArrayList<>(mCachedAlarms.values()));
            }
        });
    }

    @Override
    public void saveAlarm(@NonNull Alarm alarm, final AlarmUpdateInsertCallback alarmUpdateInsertCallback) {
        localAlarmsDataSource.saveAlarm(alarm, new AlarmUpdateInsertCallback() {
            @Override
            public void onAlarmUpdate(final Alarm alarm) {
                // Do in memory cache update to keep the app UI up to date
                if (mCachedAlarms == null) {
                    mCachedAlarms = new LinkedHashMap<>();
                }
                mCachedAlarms.put(alarm.id, alarm);
                if (alarmUpdateInsertCallback != null) {
                    alarmUpdateInsertCallback.onAlarmUpdate(alarm);
                }
            }
        });
    }

    @Override
    public void deleteAlarm(@NonNull Alarm alarm, final AlarmUpdateInsertCallback alarmUpdateInsertCallback) {
        localAlarmsDataSource.deleteAlarm(alarm, new AlarmUpdateInsertCallback() {
            @Override
            public void onAlarmUpdate(Alarm alarm) {
                mCachedAlarms.remove(alarm.id);
                if (alarmUpdateInsertCallback != null) {
                    alarmUpdateInsertCallback.onAlarmUpdate(alarm);
                }
            }
        });
    }

    @Override
    public void updateAlarm(@NonNull Alarm alarm, final AlarmUpdateInsertCallback alarmUpdateInsertCallback) {
        localAlarmsDataSource.updateAlarm(alarm, new AlarmUpdateInsertCallback() {
            @Override
            public void onAlarmUpdate(Alarm alarm) {
                // Do in memory cache update to keep the app UI up to date
                if (mCachedAlarms == null) {
                    mCachedAlarms = new LinkedHashMap<>();
                }
                mCachedAlarms.put(alarm.id, alarm);
                if (alarmUpdateInsertCallback != null) {
                    alarmUpdateInsertCallback.onAlarmUpdate(alarm);
                }
            }
        });
    }

    @Override
    public void refreshAlarms() {
        mCacheIsDirty = true;
    }

    private void refreshCache(List<Alarm> alarms) {
        if (mCachedAlarms == null) {
            mCachedAlarms = new LinkedHashMap<>();
        }
        mCachedAlarms.clear();
        for (Alarm alarm : alarms) {
            mCachedAlarms.put(alarm.id, alarm);
        }
        mCacheIsDirty = false;
    }

    public boolean isEnabled() {
        if (alarmEnabled == null) alarmEnabled = Prefs.isAlarmEnabled();
        return alarmEnabled;
    }

    public void updateEnabled(boolean alarmEnabled) {
        this.alarmEnabled = alarmEnabled;
        Prefs.saveAlarmEnabled(alarmEnabled);
    }

    public boolean hasAnyActive() {
        if (mCachedAlarms == null) return false;
        if (!isEnabled()) return false;
        if (mCachedAlarms.size() < 1) return false; // TODO
        for (Alarm alarm : mCachedAlarms.values()) {
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
}
