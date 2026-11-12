package dynoapps.exchange_rates.alarm

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dynoapps.exchange_rates.SourcesManager
import dynoapps.exchange_rates.util.L
import dynoapps.exchange_rates.util.RateUtils
import io.reactivex.rxjava3.schedulers.Schedulers

/** One alarm as the alarms screen shows it. */
data class AlarmItemState(
    val id: Long,
    val isAbove: Boolean,
    val value: String,
    @DrawableRes val rateIcon: Int?,
    val sourceName: String?,
    @ColorInt val sourceColor: Int?,
    val isEnabled: Boolean,
)

/** State of the alarms screen, backed by [AlarmsRepository]. */
class AlarmsViewModel(private val repository: AlarmsRepository) : ViewModel() {

    /** The alarms, sorted by [Alarm.COMPARATOR]. */
    private var alarms: List<Alarm> = emptyList()
        set(value) {
            field = value
            items = value.map(::toItemState)
        }

    var items: List<AlarmItemState> by mutableStateOf(emptyList())
        private set

    /** The master switch: when off, no alarm fires. */
    var alarmsEnabled: Boolean by mutableStateOf(repository.isEnabled)
        private set

    init {
        repository.refreshAlarms()
        repository.getAlarms { loaded -> alarms = loaded.sortedWith(Alarm.COMPARATOR) }
    }

    fun updateAlarmsEnabled(enabled: Boolean) {
        repository.updateEnabled(enabled)
        alarmsEnabled = enabled
    }

    /** Adds an alarm the add-alarm dialog has already saved. */
    fun onAlarmAdded(alarm: Alarm) {
        alarms = (alarms + alarm).sortedWith(Alarm.COMPARATOR)
    }

    fun setAlarmEnabled(id: Long, enabled: Boolean) {
        val alarm = alarms.firstOrNull { it.id == id } ?: return
        alarm.isEnabled = enabled
        alarms = alarms.toList()
        // Not disposed with the screen, so leaving right away doesn't cancel the write.
        repository.updateAlarm(alarm).subscribeOn(Schedulers.io())
            .subscribe({ }, { L.ex(it, "Updating alarm failed") })
    }

    fun deleteAlarm(id: Long) {
        val alarm = alarms.firstOrNull { it.id == id } ?: return
        alarms = alarms - alarm
        repository.deleteAlarm(alarm).subscribeOn(Schedulers.io())
            .subscribe({ }, { L.ex(it, "Deleting alarm failed") })
    }

    private fun toItemState(alarm: Alarm): AlarmItemState {
        val source = SourcesManager.getSource(alarm.sourceType)
        return AlarmItemState(
            id = alarm.id,
            isAbove = alarm.isAbove,
            value = RateUtils.valueToUI(alarm.value, alarm.rateType),
            rateIcon = RateUtils.getRateIcon(alarm.rateType).takeIf { it != -1 },
            sourceName = source?.name,
            sourceColor = source?.color,
            isEnabled = alarm.isEnabled,
        )
    }
}
