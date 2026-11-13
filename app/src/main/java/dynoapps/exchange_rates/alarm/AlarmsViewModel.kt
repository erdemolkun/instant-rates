package dynoapps.exchange_rates.alarm

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dynoapps.exchange_rates.AppScope
import dynoapps.exchange_rates.SourcesManager
import dynoapps.exchange_rates.util.RateUtils
import kotlinx.coroutines.launch

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
        viewModelScope.launch { alarms = repository.getAlarms().sortedWith(Alarm.COMPARATOR) }
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
        // In the app scope, so leaving the screen right away doesn't cancel the write.
        AppScope.launch(TAG, "Updating alarm failed") { repository.updateAlarm(alarm) }
    }

    fun deleteAlarm(id: Long) {
        val alarm = alarms.firstOrNull { it.id == id } ?: return
        alarms = alarms - alarm
        AppScope.launch(TAG, "Deleting alarm failed") { repository.deleteAlarm(alarm) }
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

    private companion object {
        const val TAG = "AlarmsViewModel"
    }
}
