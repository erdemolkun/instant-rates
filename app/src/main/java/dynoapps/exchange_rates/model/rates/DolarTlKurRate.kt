package dynoapps.exchange_rates.model.rates

class DolarTlKurRate : AvgRate() {

    override fun toRateType() {
        rateType = when (type) {
            "USDTRY" -> IRate.USD
            "EURTRY" -> IRate.EUR
            "EURUSD" -> IRate.EUR_USD
            "XAUUSD" -> IRate.ONS
            else -> IRate.UNKNOWN
        }
    }

    override fun setRealValues() {
        if (rateType == IRate.UNKNOWN) return
        val_real_avg = avg_val.orEmpty().replace("'", "").replace("$", "").trim().toFloat()
    }

    override fun toString(): String = "${type.orEmpty().split("_")[0]} -> : $val_real_avg"
}
