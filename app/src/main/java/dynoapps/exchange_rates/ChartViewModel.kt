package dynoapps.exchange_rates

import androidx.annotation.ColorInt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import dynoapps.exchange_rates.data.RatesHolder
import dynoapps.exchange_rates.model.rates.AvgRate
import dynoapps.exchange_rates.model.rates.BaseRate
import dynoapps.exchange_rates.model.rates.BuySellRate
import dynoapps.exchange_rates.model.rates.IRate
import dynoapps.exchange_rates.time.TimeIntervalManager
import dynoapps.exchange_rates.util.RateUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlin.math.abs

/**
 * The live chart data of one rate type across all sources: one data set per average source, two
 * (sell, buy) per buy/sell source, in the order of `CurrencySource.chartIndex`. The x axis is
 * milliseconds since [startMillis]. Old entries are dropped once a data set spans well beyond the
 * visible time range.
 */
class ChartViewModel(
    @IRate.RateDef val rateType: Int,
    sellLabel: String,
    buyLabel: String,
) : ViewModel() {

    private val disposables = CompositeDisposable()

    /** Changed in place; [version] tells the chart when. */
    val lineData = LineData()

    /** Chart x values are milliseconds since this time. */
    private var startMillis: Long = System.currentTimeMillis()

    /** Increases with every added entry, so the chart knows to refresh. */
    var version: Int by mutableIntStateOf(0)
        private set

    /** The x value of the newest entry, which the chart keeps in view. */
    var latestX: Float? by mutableStateOf(null)
        private set

    /** The time range the chart shows: [DATA_COUNT] polling intervals. */
    val visibleTimeMillis: Long
        get() = TimeIntervalManager.getPollingInterval() * DATA_COUNT

    init {
        for (source in SourcesManager.getCurrencySources()) {
            if (SourcesManager.isAvgType(source.type)) {
                lineData.addDataSet(createDataSet(source.color, source.name))
            } else {
                lineData.addDataSet(createDataSet(source.color, "${source.name} $sellLabel"))
                lineData.addDataSet(createDataSet(source.color, "${source.name} $buyLabel"))
            }
        }

        // Plot the cached rates, starting the time axis at the oldest of them.
        val cachedEvents = SourcesManager.getCurrencySources()
            .filter { it.isEnabled }
            .mapNotNull { RatesHolder.getLatestEvent(it.type) }
        cachedEvents.minOfOrNull { it.fetchTime }?.let { startMillis = minOf(startMillis, it) }
        cachedEvents.forEach { add(it.rates, it.sourceType, it.fetchTime) }

        disposables.add(
            ProvidersManager.ratesEventPublishSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { event -> add(event.rates, event.sourceType, event.fetchTime) }
        )
    }

    override fun onCleared() {
        disposables.dispose()
    }

    private fun add(rates: List<BaseRate>, sourceType: Int, fetchTimeMillis: Long) {
        val rate = RateUtils.getRate(rates, rateType) ?: return
        val chartIndex = SourcesManager.getSource(sourceType)?.chartIndex ?: return
        when (rate) {
            is AvgRate -> addEntry(rate.val_real_avg, chartIndex, fetchTimeMillis)
            is BuySellRate -> {
                rate.valueSellReal?.let { addEntry(it, chartIndex, fetchTimeMillis) }
                rate.valueBuyReal?.let { addEntry(it, chartIndex + 1, fetchTimeMillis) }
            }
        }
    }

    private fun addEntry(value: Float, chartIndex: Int, millis: Long) {
        // Values this small are parse errors, not rates.
        if (value < THRESHOLD_ERROR_USD_TRY) return
        val dataSet = lineData.getDataSetByIndex(chartIndex) ?: return
        val newX = (millis - startMillis).toFloat()
        lineData.addEntry(Entry(newX, value), chartIndex)
        if (abs(dataSet.xMin - dataSet.xMax) > visibleTimeMillis * 3 && dataSet.entryCount > DATA_COUNT) {
            dataSet.removeEntry(0)
        }
        latestX = newX
        version++
    }

    private fun createDataSet(@ColorInt color: Int, name: String): LineDataSet<Nothing?> =
        LineDataSet(mutableListOf<Entry<Nothing?>>(), name).apply {
            cubicIntensity = 0.1f
            isDrawCircleHoleEnabled = false
            circleColor = color
            lineWidth = 2f
            circleRadius = 2.5f
            isDrawCirclesEnabled = true
            highlightColor = android.graphics.Color.rgb(155, 155, 155)
            axisDependency = YAxis.AxisDependency.LEFT
            this.color = color
            fillAlpha = (256 * 0.3f).toInt()
            fillColor = color
            valueTextColor = color
            valueTextSize = 16f
            isDrawValuesEnabled = false
        }

    private companion object {
        const val THRESHOLD_ERROR_USD_TRY = 0.2f
        const val DATA_COUNT = 20
    }
}
