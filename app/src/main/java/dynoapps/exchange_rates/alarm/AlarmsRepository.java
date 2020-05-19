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
import io.reactivex.Single;

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
    Map<Long, Alarm> cachedAlarms;
    private final AlarmsDataSource localAlarmsDataSource;
    private Boolean alarmEnabled = null;


    private AlarmsRepository(AlarmsDataSource localAlarmsDataSource) {
        this.localAlarmsDataSource = localAlarmsDataSource;
    }

    public static AlarmsRepository getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (AlarmsRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AlarmsRepository(new LocalAlarmsDataSource(context));
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void getAlarms(final AlarmsLoadCallback callback) {
        // Respond immediately with cache if available and not dirty
        if (cachedAlarms != null && !mCacheIsDirty) {
            callback.onAlarmsLoaded(new ArrayList<>(cachedAlarms.values()));
            return;
        }
        localAlarmsDataSource.getAlarms(alarms -> {
            refreshCache(alarms);
            callback.onAlarmsLoaded(new ArrayList<>(cachedAlarms.values()));
        });
    }

    @Override
    public void saveAlarm(@NonNull Alarm alarm, final AlarmUpdateInsertCallback alarmUpdateInsertCallback) {
        localAlarmsDataSource.saveAlarm(alarm, alarm1 -> {
            // Do in memory cache update to keep the app UI up to date
            if (cachedAlarms == null) {
                cachedAlarms = new LinkedHashMap<>();
            }
            cachedAlarms.put(alarm1.id, alarm1);
            if (alarmUpdateInsertCallback != null) {
                alarmUpdateInsertCallback.onAlarmUpdate(alarm1);
            }
        });
    }

    @Override
    public Single<Alarm> deleteAlarm(@NonNull Alarm alarm) {
        return localAlarmsDataSource.deleteAlarm(alarm).doAfterSuccess(alarm1 -> {
            cachedAlarms.remove(alarm1.id);
        });
    }

    @Override
    public Single<Alarm> updateAlarm(@NonNull Alarm alarm) {

        return localAlarmsDataSource.updateAlarm(alarm).doAfterSuccess(updatedAlarm -> {
            // Do in memory cache update to keep the app UI up to date
            if (cachedAlarms == null) {
                cachedAlarms = new LinkedHashMap<>();
            }
            cachedAlarms.put(updatedAlarm.id, updatedAlarm);
        });
    }

    @Override
    public void refreshAlarms() {
        mCacheIsDirty = true;
    }

    private void refreshCache(List<Alarm> alarms) {
        if (cachedAlarms == null) {
            cachedAlarms = new LinkedHashMap<>();
        }
        cachedAlarms.clear();
        for (Alarm alarm : alarms) {
            cachedAlarms.put(alarm.id, alarm);
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

        if (!isEnabled()) return false;
        if (cachedAlarms == null || cachedAlarms.size() < 1) return false;
        for (Alarm alarm : cachedAlarms.values()) {
            if (alarm.isEnabled) {
                for (CurrencySource source : SourcesManager.getCurrencySources()) {
                    if (source.isEnabled() && source.getType() == alarm.sourceType) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
