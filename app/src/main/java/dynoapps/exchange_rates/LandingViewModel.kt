package dynoapps.exchange_rates

import androidx.annotation.DrawableRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dynoapps.exchange_rates.data.RatesHolder
import dynoapps.exchange_rates.interfaces.ValueType
import dynoapps.exchange_rates.model.rates.AvgRate
import dynoapps.exchange_rates.model.rates.BaseRate
import dynoapps.exchange_rates.model.rates.BuySellRate
import dynoapps.exchange_rates.model.rates.IRate
import dynoapps.exchange_rates.time.TimeIntervalManager
import dynoapps.exchange_rates.util.RateUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

/** One card on the main screen: a source's buy, sell or average value for a rate type. */
data class RateCardState(
    val sourceType: Int,
    val valueType: Int,
    val title: String,
    /** The formatted value, or empty before the source's first result. */
    val value: String,
)

/** One row on the main screen. No cards means no enabled source provides [rateType]. */
data class RateRowState(
    @IRate.RateDef val rateType: Int,
    @DrawableRes val icon: Int,
    /** Whether the icon is drawn smaller, as the dollar and euro icons are. */
    val insetIcon: Boolean,
    val cards: List<RateCardState>,
)

/**
 * State of the main screen: a row per rate type with a card per enabled source that provides it.
 * Starts the rate providers and keeps them in step with the source selection.
 */
class LandingViewModel : ViewModel() {

    private val disposables = CompositeDisposable()

    /** The latest rates per source type. */
    private val latestRates = HashMap<Int, List<BaseRate>>()

    var rows: List<RateRowState> by mutableStateOf(emptyList())
        private set

    /** The polling interval, such as "10 secs". */
    var intervalSelection: String by mutableStateOf(TimeIntervalManager.getSelectionStr())
        private set

    init {
        // Load saved alarms now: going to the background only starts the polling service if the
        // repository already knows there is an active alarm.
        App.getInstance().provideAlarmsRepository().getAlarms { }

        ProvidersManager.startSources()
        disposables.add(ProvidersManager.registerIntervalUpdates())

        // Show the cached rates until the next fetch arrives.
        for (source in SourcesManager.getCurrencySources()) {
            RatesHolder.getLatestEvent(source.type)?.let { latestRates[it.sourceType] = it.rates }
        }
        rows = buildRows()

        disposables.add(
            SourcesManager.sourceUpdates.observeOn(AndroidSchedulers.mainThread()).subscribe {
                ProvidersManager.startOrStopSources()
                rows = buildRows()
            }
        )
        disposables.add(
            TimeIntervalManager.intervalUpdates.observeOn(AndroidSchedulers.mainThread()).subscribe {
                intervalSelection = TimeIntervalManager.getSelectionStr()
            }
        )
        disposables.add(
            ProvidersManager.ratesEventPublishSubject.observeOn(AndroidSchedulers.mainThread()).subscribe { event ->
                latestRates[event.sourceType] = event.rates
                rows = buildRows()
            }
        )
    }

    override fun onCleared() {
        disposables.dispose()
    }

    private fun buildRows(): List<RateRowState> = ROWS.map { (rateType, icon, insetIcon) ->
        val cards = SourcesManager.getCurrencySources()
            .filter { it.isEnabled && it.isRateSupported(rateType) }
            .flatMap { source ->
                val valueTypes = if (SourcesManager.isAvgType(source.type)) {
                    listOf(ValueType.AVG)
                } else {
                    listOf(ValueType.BUY, ValueType.SELL)
                }
                valueTypes.map { valueType -> card(source.type, valueType, rateType) }
            }
        RateRowState(rateType, icon, insetIcon, cards)
    }

    private fun card(sourceType: Int, valueType: Int, rateType: Int): RateCardState {
        val rate = RateUtils.getRate(latestRates[sourceType], rateType)
        val value: Float? = when {
            rate is BuySellRate && valueType == ValueType.SELL -> rate.valueSellReal
            rate is BuySellRate && valueType == ValueType.BUY -> rate.valueBuyReal
            rate is AvgRate -> rate.val_real_avg
            else -> null
        }
        return RateCardState(
            sourceType = sourceType,
            valueType = valueType,
            title = SourcesManager.getSourceName(sourceType, valueType),
            value = if (rate != null && value != null) rate.getFormatted(value) else "",
        )
    }

    private companion object {
        /** The rows in screen order: rate type, icon, and whether the icon is inset. */
        val ROWS = listOf(
            Triple(IRate.USD, R.drawable.ic_dollar, true),
            Triple(IRate.EUR, R.drawable.ic_euro, true),
            Triple(IRate.ONS_TRY, R.drawable.ic_gold, false),
            Triple(IRate.EUR_USD, R.drawable.ic_exchange_eur_usd, false),
            Triple(IRate.ONS, R.drawable.ic_gold, false),
        )
    }
}
