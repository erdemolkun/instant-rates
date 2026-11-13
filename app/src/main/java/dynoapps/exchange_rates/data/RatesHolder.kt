package dynoapps.exchange_rates.data

import android.util.SparseArray
import dynoapps.exchange_rates.event.RatesEvent
import dynoapps.exchange_rates.model.rates.BaseRate

/** Keeps the latest [MAX_RATES_COUNT] fetch results per source. */
object RatesHolder {
    private const val MAX_RATES_COUNT = 10

    private val rateEvents = SparseArray<ArrayDeque<RatesEvent<*>>>()

    @Synchronized
    fun getLatestEvent(sourceType: Int): RatesEvent<*>? = rateEvents.get(sourceType)?.lastOrNull()

    /**
     * Stores a fetch result, keeping only the latest [MAX_RATES_COUNT] events per source so a
     * long-running polling service doesn't grow without bound.
     */
    @Synchronized
    fun <T : BaseRate> addRate(rates: List<T>, type: Int, fetchTime: Long = System.currentTimeMillis()) {
        val events = rateEvents.get(type) ?: ArrayDeque<RatesEvent<*>>().also { rateEvents.put(type, it) }
        events.addLast(RatesEvent(rates, type, fetchTime))
        while (events.size > MAX_RATES_COUNT) {
            events.removeFirst()
        }
    }
}
