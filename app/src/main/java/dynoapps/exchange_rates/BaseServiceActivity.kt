package dynoapps.exchange_rates

import dynoapps.exchange_rates.time.TimeIntervalManager

/** Base for screens that show live rates: polls at the UI interval while resumed. */
abstract class BaseServiceActivity : BaseActivity() {

    override fun onResume() {
        super.onResume()
        TimeIntervalManager.setAlarmMode(false)
    }
}
