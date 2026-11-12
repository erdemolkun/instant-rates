package dynoapps.exchange_rates.alarm

import androidx.activity.ComponentActivity
import dynoapps.exchange_rates.App
import dynoapps.exchange_rates.SourcesManager
import dynoapps.exchange_rates.interfaces.ValueType
import dynoapps.exchange_rates.ui.compose.showComposeDialog

/** The "add alarm" dialog. */
object AlarmManager {

    /**
     * Shows the dialog, preselecting [sourceType], [rateType] and [defaultValue] when given.
     * [onSaved] is called with the alarm once it is stored.
     */
    fun addAlarmDialog(
        activity: ComponentActivity,
        sourceType: Int = -1,
        rateType: Int = -1,
        valueType: Int = ValueType.NONE,
        defaultValue: Float? = null,
        onSaved: ((Alarm) -> Unit)? = null,
    ) {
        activity.showComposeDialog { dismiss ->
            AddAlarmDialog(
                sources = SourcesManager.getCurrencySources().filter { it.isEnabled },
                initialSourceType = sourceType,
                initialRateType = rateType,
                initialValue = defaultValue?.let(::alarmInputText).orEmpty(),
                onAdd = { value, isAbove, selectedSourceType, selectedRateType ->
                    val alarm = Alarm().apply {
                        this.value = value
                        this.isAbove = isAbove
                        this.sourceType = selectedSourceType
                        this.rateType = selectedRateType
                        value_type = valueType
                    }
                    App.getInstance().provideAlarmsRepository().saveAlarm(alarm, onSaved)
                    dismiss()
                },
                onDismiss = dismiss,
            )
        }
    }
}
