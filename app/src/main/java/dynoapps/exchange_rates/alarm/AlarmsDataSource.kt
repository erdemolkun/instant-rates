package dynoapps.exchange_rates.alarm

import io.reactivex.rxjava3.core.Single

interface AlarmsDataSource {

    fun getAlarms(callback: (List<Alarm>) -> Unit)

    fun saveAlarm(alarm: Alarm, callback: ((Alarm) -> Unit)?)

    fun deleteAlarm(alarm: Alarm): Single<Alarm>

    fun updateAlarm(alarm: Alarm): Single<Alarm>

    fun refreshAlarms()
}
