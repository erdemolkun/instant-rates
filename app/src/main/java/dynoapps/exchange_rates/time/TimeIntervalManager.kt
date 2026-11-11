package dynoapps.exchange_rates.time

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import dynoapps.exchange_rates.App
import dynoapps.exchange_rates.Prefs
import dynoapps.exchange_rates.R
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit

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

    /**
     * Emits when the polling interval should be re-applied. The value is true when providers
     * should fetch immediately.
     */
    val intervalUpdates: PublishSubject<Boolean> = PublishSubject.create()

    fun setAlarmMode(enabled: Boolean) {
        isAlarmMode = enabled
        updateIntervalsToUIMode()
    }

    fun updateIntervalsToUIMode() {
        intervalUpdates.onNext(true)
    }

    fun getSelectionStr(): String = intervals[getSelectedIndex()].toString()

    fun selectInterval(activity: Activity) {
        var userSelectedIndex = getSelectedIndex()
        val names = intervals.map { it.toString() }.toTypedArray()
        val dialog = AlertDialog.Builder(activity, R.style.AppTheme_Alert)
            .setSingleChoiceItems(names, userSelectedIndex) { _, which -> userSelectedIndex = which }
            .setCancelable(true)
            .setTitle(R.string.select_time_interval)
            .setPositiveButton(R.string.apply) { _, _ ->
                if (userSelectedIndex != getSelectedIndex()) {
                    updateUserIntervalSelection(userSelectedIndex)
                    intervalUpdates.onNext(false)
                }
            }
            .setNegativeButton(R.string.dismiss, null)
            .create()
        dialog.window?.setWindowAnimations(R.style.DialogAnimationFade)
        dialog.show()
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
