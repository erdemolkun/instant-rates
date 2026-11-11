package dynoapps.exchange_rates.model.rates

import dynoapps.exchange_rates.util.RateUtils

class ParaGarantiRate : AvgRate() {

    var symbol: String? = null

    var last: String? = null

    override fun toRateType() {
        rateType = when (symbol) {
            "KUSD" -> IRate.USD
            "KEUR" -> IRate.EUR
            "EUR" -> IRate.EUR_USD
            else -> IRate.UNKNOWN
        }
    }

    override fun setRealValues() {
        if (rateType == IRate.UNKNOWN) return
        val value = last.orEmpty().replace(',', '.')
        last = value
        val_real_avg = RateUtils.toFloat(value.replace("'", "").replace("$", "").trim()) ?: 0f
    }

    override fun toString(): String = "SYMBOL : $symbol Value : $last"
}
