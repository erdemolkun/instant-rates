package dynoapps.exchange_rates

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import dynoapps.exchange_rates.data.RatesHolder
import dynoapps.exchange_rates.model.rates.AvgRate
import dynoapps.exchange_rates.model.rates.BaseRate
import dynoapps.exchange_rates.model.rates.BuySellRate
import dynoapps.exchange_rates.model.rates.IRate
import dynoapps.exchange_rates.time.TimeIntervalManager
import dynoapps.exchange_rates.ui.compose.ConfirmDialog
import dynoapps.exchange_rates.ui.compose.showComposeDialog
import dynoapps.exchange_rates.util.RateUtils
import dynoapps.exchange_rates.util.ViewUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/** Live line chart of one rate type across all sources. */
class ChartActivity : BaseActivity() {

    private lateinit var lineChart: LineChart

    /** The chart's data: one data set per average source, two (sell, buy) per buy/sell source. */
    private val lineData = LineData()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val hourFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val compositeDisposable = CompositeDisposable()

    @IRate.RateDef
    private var rateType = IRate.USD

    /** Chart x values are milliseconds since this time. */
    private var startMillis: Long = 0

    @ColorInt
    private var chartTextColor = 0

    override val layoutId: Int = R.layout.activity_chart

    private val visibleTimeInMillis: Long
        get() = TimeIntervalManager.getPollingInterval() * DATA_COUNT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tvTitle: TextView = findViewById(R.id.tv_chart_title)
        lineChart = findViewById(R.id.line_chart)
        swipeRefreshLayout = findViewById(R.id.swipe_to_refresh)
        rateType = intent.getIntExtra(EXTRA_RATE_TYPE, rateType)

        tvTitle.setText(
            when (rateType) {
                IRate.EUR -> R.string.euro_tl_graph
                IRate.EUR_USD -> R.string.euro_usd_graph
                IRate.ONS -> R.string.ons_dollar_grapsh
                else -> R.string.dollar_tl_graph
            }
        )

        setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        actionBarToolbar?.setNavigationOnClickListener { finish() }

        chartTextColor = ContextCompat.getColor(applicationContext, android.R.color.black)
        startMillis = System.currentTimeMillis()
        initChart()

        // Plot the cached rates, starting the time axis at the oldest of them.
        val cachedEvents = SourcesManager.getCurrencySources()
            .filter { it.isEnabled }
            .mapNotNull { RatesHolder.getLatestEvent(it.type) }
        cachedEvents.minOfOrNull { it.fetchTime }?.let { startMillis = minOf(startMillis, it) }
        cachedEvents.forEach { update(it.rates, it.sourceType, it.fetchTime) }

        swipeRefreshLayout.setColorSchemeResources(
            R.color.refresh_progress_1,
            R.color.refresh_progress_2,
            R.color.refresh_progress_3,
        )
        swipeRefreshLayout.isEnabled = true
        val top = ViewUtils.calculateActionBarSize(this)
        val progressBarStartMargin = resources.getDimensionPixelSize(R.dimen.swipe_refresh_progress_bar_start_margin)
        val progressBarEndMargin = resources.getDimensionPixelSize(R.dimen.swipe_refresh_progress_bar_end_margin)
        swipeRefreshLayout.setProgressViewOffset(true, top + progressBarStartMargin, top + progressBarEndMargin)
        swipeRefreshLayout.setOnRefreshListener {
            ProvidersManager.triggerUpdate()
            compositeDisposable.add(
                Completable.timer(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                    .subscribe { swipeRefreshLayout.isRefreshing = false }
            )
        }

        compositeDisposable.add(
            ProvidersManager.ratesEventPublishSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { event -> update(event.rates, event.sourceType, event.fetchTime) }
        )
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun initChart() {
        lineChart.description.isEnabled = false
        lineChart.data = lineData
        lineChart.xAxis.isDrawGridLinesEnabled = false
        lineChart.axisRight.isDrawGridLinesEnabled = true
        lineChart.xAxis.labelCount = 4
        lineChart.axisLeft.isEnabled = false
        lineChart.xAxis.valueFormatter = IAxisValueFormatter { value, _ ->
            val offset = value.toInt()
            if (offset <= 0) {
                ""
            } else {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.MILLISECOND, offset)
                hourFormatter.format(calendar.time)
            }
        }
        lineChart.axisRight.valueFormatter = IAxisValueFormatter { value, _ -> RateUtils.valueToUI(value, rateType) }
        lineChart.isScaleEnabled = false
        lineChart.extraBottomOffset = 12f
        lineChart.extraTopOffset = 12f
        lineChart.isPinchZoomEnabled = false

        // Data sets are added in the order of CurrencySource.chartIndex.
        for (source in SourcesManager.getCurrencySources()) {
            if (SourcesManager.isAvgType(source.type)) {
                lineData.addDataSet(createDataSet(source.color, source.name))
            } else {
                lineData.addDataSet(createDataSet(source.color, source.name + " " + getString(R.string.sell)))
                lineData.addDataSet(createDataSet(source.color, source.name + " " + getString(R.string.buy)))
            }
        }

        lineChart.legend.apply {
            textSize = 13f
            textColor = chartTextColor
            yOffset = 6f
            form = Legend.LegendForm.CIRCLE
            isWordWrapEnabled = true
            xEntrySpace = 10f
        }
        lineChart.isHighlightPerTapEnabled = true
        val markerView = CustomMarkerView(this, R.layout.view_marker, rateType)
        markerView.setOffset(ViewUtils.dpToPx(4f).toFloat(), (-markerView.measuredHeight - ViewUtils.dpToPx(4f)).toFloat())
        lineChart.marker = markerView
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.xAxis.textColor = chartTextColor
        lineChart.axisRight.textColor = chartTextColor
        lineChart.notifyDataSetChanged()
    }

    private fun update(rates: List<BaseRate>, sourceType: Int, fetchTimeMillis: Long) {
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_chart, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_item_sources -> {
            SourcesManager.selectSources(this)
            true
        }
        R.id.menu_time_interval -> {
            TimeIntervalManager.selectInterval(this)
            true
        }
        R.id.menu_item_refresh -> {
            confirmClearChart()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun confirmClearChart() {
        showComposeDialog { dismiss ->
            ConfirmDialog(
                title = getString(R.string.refresh),
                message = getString(R.string.clear_sure_message),
                confirmText = getString(R.string.sure),
                dismissText = getString(R.string.ignore),
                onConfirm = {
                    clearChart()
                    dismiss()
                },
                onDismiss = dismiss,
            )
        }
    }

    private fun clearChart() {
        for (i in 0 until lineData.dataSetCount) {
            lineData.getDataSetByIndex(i)?.clear()
        }
        lineChart.xAxis.resetAxisMaximum()
        lineChart.notifyDataSetChanged()
        startMillis = System.currentTimeMillis()
        lineChart.moveViewToX(0f)
    }

    private fun addEntry(value: Float, chartIndex: Int, millis: Long) {
        // Values this small are parse errors, not rates.
        if (value < THRESHOLD_ERROR_USD_TRY) return
        val dataSet = lineData.getDataSetByIndex(chartIndex) ?: return
        val newX = (millis - startMillis).toFloat()
        lineData.addEntry(Entry(newX, value), chartIndex)

        if (abs(dataSet.xMin - dataSet.xMax) > visibleTimeInMillis * 3 && dataSet.entryCount > DATA_COUNT) {
            dataSet.removeEntry(0)
        }

        // Refreshes the data and redraws.
        lineChart.notifyDataSetChanged()
        lineChart.setVisibleXRangeMaximum(visibleTimeInMillis.toFloat())
        when {
            lineChart.xAxis.axisMaximum <= newX -> lineChart.moveViewToX(newX)
            lineChart.visibleXRange < newX -> lineChart.moveViewToX(newX + lineChart.visibleXRange)
        }
    }

    private fun createDataSet(@ColorInt color: Int, name: String): LineDataSet<Nothing?> =
        LineDataSet(mutableListOf<Entry<Nothing?>>(), name).apply {
            cubicIntensity = 0.1f
            isDrawCircleHoleEnabled = false
            circleColor = color
            lineWidth = 2f
            circleRadius = 2.5f
            isDrawCirclesEnabled = true
            highlightColor = Color.rgb(155, 155, 155)
            axisDependency = YAxis.AxisDependency.LEFT
            this.color = color
            fillAlpha = (256 * 0.3f).toInt()
            fillColor = color
            valueTextColor = color
            valueTextSize = 16f
            isDrawValuesEnabled = false
        }

    @SuppressLint("ViewConstructor")
    private class CustomMarkerView(context: Context, layoutResource: Int, private val rateType: Int) :
        MarkerView(context, layoutResource) {

        private val tvMarker: TextView = findViewById(R.id.tv_marker)

        override fun refreshContent(e: Entry<*>, highlight: Highlight) {
            tvMarker.text = RateUtils.valueToUI(e.y, rateType)
            super.refreshContent(e, highlight)
        }
    }

    companion object {
        const val EXTRA_RATE_TYPE = "EXTRA_RATE_TYPE"
        private const val THRESHOLD_ERROR_USD_TRY = 0.2f
        private const val DATA_COUNT = 20
    }
}
