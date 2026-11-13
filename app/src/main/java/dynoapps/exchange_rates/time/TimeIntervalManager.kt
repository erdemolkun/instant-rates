package dynoapps.exchange_rates.time

import androidx.activity.ComponentActivity
import dynoapps.exchange_rates.App
import dynoapps.exchange_rates.Prefs
import dynoapps.exchange_rates.R
import dynoapps.exchange_rates.ui.compose.SingleChoiceDialog
import dynoapps.exchange_rates.ui.compose.showComposeDialog
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * The polling interval: the one the user picked while the UI is visible, and a slower one (at
 * least [INTERVAL_SERVICE_INDEX]) in alarm mode while the app is in the background.
 */
object TimeIntervalManager {
    private const val DEFAULT_INTERVAL_INDEX = 2
    private const val INTERVAL_SERVICE_INDEX = 4

    private val intervals = listOf(
        TimeInterval(3, TimeUnit.SECONDS),
        TimeInterval(5, TimeUnit.SECONDS),
        TimeInterval(10, TimeUnit.SECONDS),
        TimeInterval(15, TimeUnit.SECONDS),
        TimeInterval(20, TimeUnit.SECONDS),
        TimeInterval(30, TimeUnit.SECONDS),
        TimeInterval(1, TimeUnit.MINUTES),
    )

    private val prefIntervalMillis: Long = Prefs.getInterval(App.context())

    private var selectedIntervalIndexUser: Int = selectedIndexFromPrefs()

    private var isAlarmMode = false

    // Like the RxJava PublishSubject it replaces: no replay, and nothing is kept without collectors.
    private val _intervalUpdates = MutableSharedFlow<Boolean>(extraBufferCapacity = 8)

    /**
     * Emits when the polling interval should be re-applied. The value is true when providers
     * should fetch immediately.
     */
    val intervalUpdates: SharedFlow<Boolean> = _intervalUpdates.asSharedFlow()

    fun setAlarmMode(enabled: Boolean) {
        isAlarmMode = enabled
        updateIntervalsToUIMode()
    }

    fun updateIntervalsToUIMode() {
        _intervalUpdates.tryEmit(true)
    }

    fun getSelectionStr(): String = intervals[getSelectedIndex()].toString()

    fun selectInterval(activity: ComponentActivity) {
        activity.showComposeDialog { dismiss ->
            SingleChoiceDialog(
                title = activity.getString(R.string.select_time_interval),
                items = intervals.map { it.toString() },
                initialSelection = getSelectedIndex(),
                confirmText = activity.getString(R.string.apply),
                dismissText = activity.getString(R.string.dismiss),
                onConfirm = { selected ->
                    if (selected != getSelectedIndex()) {
                        updateUserIntervalSelection(selected)
                        _intervalUpdates.tryEmit(false)
                    }
                    dismiss()
                },
                onDismiss = dismiss,
            )
        }
    }

    private fun selectedIndexFromPrefs(): Int {
        val saved = Prefs.getInterval(App.context())
        if (saved < 0) return -1
        return intervals.indexOfFirst { it.to(TimeUnit.MILLISECONDS) == saved }
    }

    private fun getSelectedIndex(): Int = if (!isAlarmMode) {
        if (selectedIntervalIndexUser < 0) DEFAULT_INTERVAL_INDEX else selectedIntervalIndexUser
    } else {
        maxOf(selectedIntervalIndexUser, INTERVAL_SERVICE_INDEX)
    }

    private fun updateUserIntervalSelection(index: Int) {
        selectedIntervalIndexUser = index.coerceIn(0, intervals.lastIndex)
        Prefs.saveInterval(App.context(), intervals[selectedIntervalIndexUser].to(TimeUnit.MILLISECONDS))
    }

    fun getPollingInterval(): Long =
        if (!isAlarmMode && selectedIntervalIndexUser < 0) {
            if (prefIntervalMillis < 0) {
                intervals[DEFAULT_INTERVAL_INDEX].to(TimeUnit.MILLISECONDS)
            } else {
                prefIntervalMillis
            }
        } else {
            intervals[getSelectedIndex()].to(TimeUnit.MILLISECONDS)
        }
}
