package dynoapps.exchange_rates.alarm;

import android.content.Context;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import dynoapps.exchange_rates.Prefs;
import dynoapps.exchange_rates.SourcesManager;
import dynoapps.exchange_rates.data.CurrencySource;

/**
 * Created by erdemmac on 19/07/2017.
 */

public class AlarmsRepository implements AlarmsDataSource {

    private static AlarmsRepository INSTANCE = null;
    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    boolean mCacheIsDirty = false;
    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    Map<Long, Alarm> mCachedAlarms;
    private AlarmsDataSource localAlarmsDataSource;
    private Boolean alarmEnabled = null;


    private AlarmsRepository(AlarmsDataSource localAlarmsDataSource) {
        this.localAlarmsDataSource = localAlarmsDataSource;
    }

    public static AlarmsRepository getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AlarmsRepository(new LocalAlarmsDataSource(context));
        }
        return INSTANCE;
    }

    @Override
    public void getAlarms(final AlarmsLoadCallback callback) {
        // Respond immediately with cache if available and not dirty
        if (mCachedAlarms != null && !mCacheIsDirty) {
            callback.onAlarmsLoaded(new ArrayList<>(mCachedAlarms.values()));
            return;
        }
        localAlarmsDataSource.getAlarms(alarms -> {
            refreshCache(alarms);
            callback.onAlarmsLoaded(new ArrayList<>(mCachedAlarms.values()));
        });
    }

    @Override
    public void saveAlarm(@NonNull Alarm alarm, final AlarmUpdateInsertCallback alarmUpdateInsertCallback) {
        localAlarmsDataSource.saveAlarm(alarm, alarm1 -> {
            // Do in memory cache update to keep the app UI up to date
            if (mCachedAlarms == null) {
                mCachedAlarms = new LinkedHashMap<>();
            }
            mCachedAlarms.put(alarm1.id, alarm1);
            if (alarmUpdateInsertCallback != null) {
                alarmUpdateInsertCallback.onAlarmUpdate(alarm1);
            }
        });
    }

    @Override
    public void deleteAlarm(@NonNull Alarm alarm, final AlarmUpdateInsertCallback alarmUpdateInsertCallback) {
        localAlarmsDataSource.deleteAlarm(alarm, alarm1 -> {
            mCachedAlarms.remove(alarm1.id);
            if (alarmUpdateInsertCallback != null) {
                alarmUpdateInsertCallback.onAlarmUpdate(alarm1);
            }
        });
    }

    @Override
    public void updateAlarm(@NonNull Alarm alarm, final AlarmUpdateInsertCallback alarmUpdateInsertCallback) {
        localAlarmsDataSource.updateAlarm(alarm, alarm1 -> {
            // Do in memory cache update to keep the app UI up to date
            if (mCachedAlarms == null) {
                mCachedAlarms = new LinkedHashMap<>();
            }
            mCachedAlarms.put(alarm1.id, alarm1);
            if (alarmUpdateInsertCallback != null) {
                alarmUpdateInsertCallback.onAlarmUpdate(alarm1);
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
