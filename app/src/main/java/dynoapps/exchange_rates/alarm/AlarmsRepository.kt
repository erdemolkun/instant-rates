package dynoapps.exchange_rates.alarm

import android.content.Context
import dynoapps.exchange_rates.Prefs
import dynoapps.exchange_rates.SourcesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Alarms with an in-memory cache in front of [LocalAlarmsDataSource]. The cache is only touched
 * from the main thread: every method switches to it, so they can be called from any thread.
 */
class AlarmsRepository private constructor(
    private val localAlarmsDataSource: AlarmsDataSource,
) : AlarmsDataSource {

    private var cacheIsDirty = false
    private var cachedAlarms: MutableMap<Long, Alarm>? = null
    private var alarmEnabled: Boolean? = null

    // Main.immediate doesn't suspend when already on the main thread, so a cache hit returns
    // without waiting for the next main looper message.
    override suspend fun getAlarms(): List<Alarm> = withContext(Dispatchers.Main.immediate) {
        val cached = cachedAlarms
        if (cached == null || cacheIsDirty) refreshCache(localAlarmsDataSource.getAlarms())
        ArrayList(cache().values)
    }

    override suspend fun saveAlarm(alarm: Alarm): Alarm = withContext(Dispatchers.Main.immediate) {
        localAlarmsDataSource.saveAlarm(alarm).also { saved -> cache()[saved.id] = saved }
    }

    override suspend fun deleteAlarm(alarm: Alarm): Alarm = withContext(Dispatchers.Main.immediate) {
        localAlarmsDataSource.deleteAlarm(alarm).also { deleted -> cachedAlarms?.remove(deleted.id) }
    }

    override suspend fun updateAlarm(alarm: Alarm): Alarm = withContext(Dispatchers.Main.immediate) {
        localAlarmsDataSource.updateAlarm(alarm).also { updated -> cache()[updated.id] = updated }
    }

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
