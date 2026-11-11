package dynoapps.exchange_rates

import dynoapps.exchange_rates.alarm.Alarm
import dynoapps.exchange_rates.data.RatesHolder
import dynoapps.exchange_rates.event.RatesEvent
import dynoapps.exchange_rates.interfaces.ValueType
import dynoapps.exchange_rates.model.rates.AvgRate
import dynoapps.exchange_rates.model.rates.BaseRate
import dynoapps.exchange_rates.model.rates.BuySellRate
import dynoapps.exchange_rates.model.rates.IRate
import dynoapps.exchange_rates.notification.NotificationHelper
import dynoapps.exchange_rates.provider.BigparaRateProvider
import dynoapps.exchange_rates.provider.BloombergRateProvider
import dynoapps.exchange_rates.provider.DolarTlKurRateProvider
import dynoapps.exchange_rates.provider.EnparaRateProvider
import dynoapps.exchange_rates.provider.IPollingSource
import dynoapps.exchange_rates.provider.ParaGarantiRateProvider
import dynoapps.exchange_rates.provider.YahooRateProvider
import dynoapps.exchange_rates.provider.YapıKrediRateProvider
import dynoapps.exchange_rates.provider.YorumlarRateProvider
import dynoapps.exchange_rates.time.TimeIntervalManager
import dynoapps.exchange_rates.util.Formatter
import dynoapps.exchange_rates.util.L
import dynoapps.exchange_rates.util.RateUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject

/** Owns the rate providers, publishes their results and fires price alarms. */
object ProvidersManager {
    private const val TAG = "ProvidersManager"

    private val formatter2 = Formatter(3, 0)
    private val formatter5 = Formatter(5, 1)

    /** Emits every successful fetch from any provider. */
    val ratesEventPublishSubject: PublishSubject<RatesEvent<*>> = PublishSubject.create()

    /** Null until [startSources] is first called. */
    var pollingSources: List<IPollingSource>? = null
        private set

    private fun providerFor(sourceType: Int): IPollingSource? =
        pollingSources?.firstOrNull { it.sourceType == sourceType }

    fun triggerUpdate() {
        SourcesManager.getCurrencySources()
            .filter { it.isEnabled }
            .forEach { providerFor(it.type)?.oneShot() }
    }

    fun registerIntervalUpdates(): Disposable =
        TimeIntervalManager.intervalUpdates
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ isImmediate ->
                pollingSources?.forEach { it.refreshIntervals(isImmediate) }
            }, { th -> L.e(TAG, th.localizedMessage) })

    private fun init() {
        if (pollingSources != null) return
        val onResult: (List<BaseRate>, Int) -> Unit = ::onProviderResult
        pollingSources = listOf(
            EnparaRateProvider(onResult),
            YorumlarRateProvider(onResult),
            BigparaRateProvider(onResult),
            DolarTlKurRateProvider(onResult),
            YapıKrediRateProvider(onResult),
            YahooRateProvider(onResult),
            ParaGarantiRateProvider(onResult),
            BloombergRateProvider(onResult),
        )
    }

    fun startSources() {
        init()
        startOrStopSources()
    }

    private fun onProviderResult(rates: List<BaseRate>, sourceType: Int) {
        // Capture the previous rates before storing the new ones: alarms are loaded asynchronously
        // when the cache is cold, and must compare against the old values, not the new ones.
        val previousEvent = RatesHolder.getLatestEvent(sourceType)
        updateAlarmsAndCheck(rates, previousEvent, sourceType)
        RatesHolder.addRate(rates, sourceType)
        ratesEventPublishSubject.onNext(RatesEvent(rates, sourceType, System.currentTimeMillis()))
    }

    fun stopAll() {
        L.i(TAG, "stopAll")
        pollingSources?.forEach { it.stop() }
    }

    fun startOrStopSources() {
        for (source in SourcesManager.getCurrencySources()) {
            val provider = providerFor(source.type) ?: continue
            if (source.isEnabled) provider.start() else provider.stop()
        }
    }

    private fun updateAlarmsAndCheck(rates: List<BaseRate>, previousEvent: RatesEvent<*>?, sourceType: Int) {
        if (rates.isEmpty() || previousEvent == null) return
        App.getInstance().provideAlarmsRepository().getAlarms { alarms ->
            checkAlarms(rates, previousEvent, sourceType, alarms)
        }
    }

    private fun checkAlarms(
        rates: List<BaseRate>,
        previousEvent: RatesEvent<*>,
        sourceType: Int,
        alarms: List<Alarm>,
    ) {
        val repository = App.getInstance().provideAlarmsRepository()
        for (alarm in alarms) {
            if (alarm.sourceType != sourceType || !alarm.isEnabled) continue
            val alarmValue = alarm.`val` ?: continue
            val current = RateUtils.getRate(rates, alarm.rateType) ?: continue
            val old = RateUtils.getRate(previousEvent.rates, alarm.rateType) ?: continue

            val valueCurrent = alarmComparableValue(current, alarm.value_type)
            val valueOld = alarmComparableValue(old, alarm.value_type)

            val crossedAbove = alarm.isAbove && valueCurrent > alarmValue && valueOld <= alarmValue
            val crossedBelow = !alarm.isAbove && valueCurrent < alarmValue && valueOld >= alarmValue
            if (!crossedAbove && !crossedBelow) continue

            repository.deleteAlarm(alarm)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ }, { th -> L.e(TAG, "Deleting alarm failed: $th") })

            val formattedValue = if (alarm.rateType == IRate.ONS) {
                formatter2.format(alarmValue)
            } else {
                formatter5.format(alarmValue)
            }
            val context = App.context()
            val message = context.getString(
                if (crossedAbove) R.string.is_above_val else R.string.is_below_value,
                SourcesManager.getSourceName(alarm.sourceType),
                RateUtils.rateName(alarm.rateType),
                formattedValue,
            )
            NotificationHelper.showAlarmNotification(
                context,
                message,
                if (crossedAbove) "increasing" else "decreasing",
                Alarm.getPushId(alarm),
            )
        }
    }

    /** The value an alarm compares against: the average, or buy/sell as chosen (sell by default). */
    private fun alarmComparableValue(rate: BaseRate, valueType: Int): Float = when (rate) {
        is AvgRate -> rate.val_real_avg
        is BuySellRate -> if (valueType != ValueType.NONE) rate.getValue(valueType) else rate.valueSellReal ?: 0f
        else -> 0f
    }
}
