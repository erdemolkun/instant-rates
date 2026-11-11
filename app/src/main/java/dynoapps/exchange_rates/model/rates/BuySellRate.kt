package dynoapps.exchange_rates.model.rates

import dynoapps.exchange_rates.interfaces.ValueType

abstract class BuySellRate : BaseRate() {

    var valueSellReal: Float? = null

    var valueBuyReal: Float? = null

    var valueSell: String? = null

    var valueBuy: String? = null

    override fun toRateType() {
        val plainType = type.orEmpty().replace("\n", "")
        rateType = when {
            plainType == "USD" -> IRate.USD
            plainType == "EUR" -> IRate.EUR
            plainType.contains("altın") -> IRate.ONS_TRY
            plainType.contains("parite") -> IRate.EUR_USD
            else -> IRate.UNKNOWN
        }
    }

    override fun setRealValues() {
        valueSellReal = parse(valueSell)
        valueBuyReal = parse(valueBuy)
    }

    /** Parses values like "41,85 TL". Throws [NumberFormatException] for malformed input. */
    protected open fun parse(value: String?): Float =
        value.orEmpty().replace(" TL", "").replace(",", ".").trim().toFloat()

    override fun getValue(valueType: Int): Float = when (valueType) {
        ValueType.BUY -> valueBuyReal ?: 0f
        ValueType.SELL -> valueSellReal ?: 0f
        else -> 0f
    }

    override fun toString(): String =
        "${type.orEmpty().split("_")[0]} -> $valueSellReal : $valueBuyReal"
}
