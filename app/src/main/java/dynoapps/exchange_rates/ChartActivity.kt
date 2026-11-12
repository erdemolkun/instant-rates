package dynoapps.exchange_rates

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dynoapps.exchange_rates.model.rates.IRate
import dynoapps.exchange_rates.time.TimeIntervalManager
import dynoapps.exchange_rates.ui.compose.setRatesContent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

/** Live line chart of one rate type across all sources. */
class ChartActivity : BaseActivity() {

    private val rateType: Int by lazy { intent.getIntExtra(EXTRA_RATE_TYPE, IRate.USD) }

    private val viewModel: ChartViewModel by viewModels {
        viewModelFactory {
            initializer { ChartViewModel(rateType, getString(R.string.sell), getString(R.string.buy)) }
        }
    }

    private val compositeDisposable = CompositeDisposable()
    private var isRefreshing by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = getString(
            when (rateType) {
                IRate.EUR -> R.string.euro_tl_graph
                IRate.EUR_USD -> R.string.euro_usd_graph
                IRate.ONS -> R.string.ons_dollar_grapsh
                else -> R.string.dollar_tl_graph
            }
        )
        setRatesContent {
            ChartScreen(
                title = title,
                isRefreshing = isRefreshing,
                onRefresh = ::refresh,
                onBack = ::finish,
                onIntervalClick = { TimeIntervalManager.selectInterval(this) },
                onSourcesClick = { SourcesManager.selectSources(this) },
                chart = { modifier -> RateChart(viewModel, contentDescription = title, modifier = modifier) },
            )
        }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    /** Asks every source for new rates; the indicator shows for a second. */
    private fun refresh() {
        ProvidersManager.triggerUpdate()
        isRefreshing = true
        compositeDisposable.add(
            Completable.timer(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe { isRefreshing = false }
        )
    }

    companion object {
        const val EXTRA_RATE_TYPE = "EXTRA_RATE_TYPE"
    }
}
