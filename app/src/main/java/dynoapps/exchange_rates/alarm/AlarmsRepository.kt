package dynoapps.exchange_rates.alarm

import android.content.Context
import dynoapps.exchange_rates.Prefs
import dynoapps.exchange_rates.SourcesManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single

/**
 * Alarms with an in-memory cache in front of [LocalAlarmsDataSource]. The cache is only touched
 * from the main thread.
 */
class AlarmsRepository private constructor(
    private val localAlarmsDataSource: AlarmsDataSource,
) : AlarmsDataSource {

    private var cacheIsDirty = false
    private var cachedAlarms: MutableMap<Long, Alarm>? = null
    private var alarmEnabled: Boolean? = null

    override fun getAlarms(callback: (List<Alarm>) -> Unit) {
        // Respond immediately with the cache if available and not dirty.
        val cached = cachedAlarms
        if (cached != null && !cacheIsDirty) {
            callback(ArrayList(cached.values))
            return
        }
        localAlarmsDataSource.getAlarms { alarms ->
            refreshCache(alarms)
            callback(ArrayList(cache().values))
        }
    }

    override fun saveAlarm(alarm: Alarm, callback: ((Alarm) -> Unit)?) {
        localAlarmsDataSource.saveAlarm(alarm) { saved ->
            cache()[saved.id] = saved
            callback?.invoke(saved)
        }
    }

    override fun deleteAlarm(alarm: Alarm): Single<Alarm> =
        localAlarmsDataSource.deleteAlarm(alarm)
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterSuccess { deleted -> cachedAlarms?.remove(deleted.id) }

    override fun updateAlarm(alarm: Alarm): Single<Alarm> =
        localAlarmsDataSource.updateAlarm(alarm)
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterSuccess { updated -> cache()[updated.id] = updated }

    override fun refreshAlarms() {
        cacheIsDirty = true
    }

    private fun cache(): MutableMap<Long, Alarm> =
        cachedAlarms ?: LinkedHashMap<Long, Alarm>().also { cachedAlarms = it }

    private fun refreshCache(alarms: List<Alarm>) {
        val cache = cache()
        cache.clear()
        alarms.forEach { cache[it.id] = it }
        cacheIsDirty = false
    }

    val isEnabled: Boolean
        get() = alarmEnabled ?: Prefs.isAlarmEnabled().also { alarmEnabled = it }

    fun updateEnabled(enabled: Boolean) {
        alarmEnabled = enabled
        Prefs.saveAlarmEnabled(enabled)
    }

    /** True if alarms are on and any enabled alarm belongs to an enabled source. */
    fun hasAnyActive(): Boolean {
        if (!isEnabled) return false
        val alarms = cachedAlarms?.values ?: return false
        return alarms.any { alarm ->
            alarm.isEnabled && SourcesManager.getCurrencySources()
                .any { it.isEnabled && it.type == alarm.sourceType }
        }
    }

    companion object {
        @Volatile
        private var instance: AlarmsRepository? = null

        fun getInstance(context: Context): AlarmsRepository =
            instance ?: synchronized(this) {
                instance ?: AlarmsRepository(LocalAlarmsDataSource(context)).also { instance = it }
            }
    }
}
