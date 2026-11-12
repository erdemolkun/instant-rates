package dynoapps.exchange_rates.alarm

interface AlarmsDataSource {

    /** All alarms; empty if they can't be loaded. */
    suspend fun getAlarms(): List<Alarm>

    /** Stores [alarm] and returns it with its new ID. */
    suspend fun saveAlarm(alarm: Alarm): Alarm

    suspend fun deleteAlarm(alarm: Alarm): Alarm

    suspend fun updateAlarm(alarm: Alarm): Alarm

    fun refreshAlarms()
}
