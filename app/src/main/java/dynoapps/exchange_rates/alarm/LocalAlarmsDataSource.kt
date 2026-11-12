package dynoapps.exchange_rates.alarm

import android.content.Context
import dynoapps.exchange_rates.AppDatabase
import kotlin.coroutines.cancellation.CancellationException
import timber.log.Timber

/** Alarms in the Room database. Room runs the suspend queries on its own threads. */
class LocalAlarmsDataSource(context: Context) : AlarmsDataSource {

    private val alarmDao: AlarmDao = AppDatabase.getInstance(context.applicationContext).alarm()

    override suspend fun getAlarms(): List<Alarm> =
        try {
            alarmDao.list()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Loading alarms failed")
            emptyList()
        }

    override suspend fun saveAlarm(alarm: Alarm): Alarm {
        alarm.id = alarmDao.insert(alarm)
        return alarm
    }

    override suspend fun deleteAlarm(alarm: Alarm): Alarm {
        alarmDao.deleteById(alarm.id)
        return alarm
    }

    override suspend fun updateAlarm(alarm: Alarm): Alarm {
        alarmDao.update(alarm)
        return alarm
    }

    override fun refreshAlarms() {
        // Not required: AlarmsRepository decides when to reload from this data source.
    }

    private companion object {
        const val TAG = "LocalAlarmsDataSource"
    }
}
