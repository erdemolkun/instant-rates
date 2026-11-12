package dynoapps.exchange_rates.model.rates

import dynoapps.exchange_rates.interfaces.ValueType

/** A rate with separate buying (Alış) and selling (Satış) prices, set by the source's converter. */
abstract class BuySellRate : BaseRate() {

    var valueSellReal: Float? = null

    var valueBuyReal: Float? = null

    /** Converters parse the prices themselves, so they can skip entries that are malformed. */
    override fun setRealValues() = Unit

    override fun getValue(valueType: Int): Float = when (valueType) {
        ValueType.BUY -> valueBuyReal ?: 0f
        ValueType.SELL -> valueSellReal ?: 0f
        else -> 0f
    }

    override fun toString(): String =
        "${type.orEmpty().split("_")[0]} -> $valueSellReal : $valueBuyReal"
}
