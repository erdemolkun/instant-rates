package dynoapps.exchange_rates

import dynoapps.exchange_rates.time.TimeIntervalManager
import io.reactivex.rxjava3.disposables.CompositeDisposable

/** Base for screens that show live rates: polls at the UI interval while resumed. */
abstract class BaseServiceActivity : BaseActivity() {

    protected val compositeDisposable = CompositeDisposable()

    override fun onResume() {
        super.onResume()
        TimeIntervalManager.setAlarmMode(false)
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}
