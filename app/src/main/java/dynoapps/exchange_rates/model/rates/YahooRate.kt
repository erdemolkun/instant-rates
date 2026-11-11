package dynoapps.exchange_rates.model.rates

class YahooRate : AvgRate() {

    override fun toRateType() {
        val value = type.orEmpty()
        rateType = when {
            value.contains("USDTRY") -> IRate.USD
            value.contains("EURTRY") -> IRate.EUR
            value.contains("EURUSD") -> IRate.EUR_USD
            value.contains("GC") -> IRate.ONS
            else -> IRate.UNKNOWN
        }
    }

    override fun setRealValues() {
        if (rateType == IRate.UNKNOWN) return
        val_real_avg = avg_val.orEmpty().toFloat()
    }
}
