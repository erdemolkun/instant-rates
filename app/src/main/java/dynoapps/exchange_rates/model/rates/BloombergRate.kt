package dynoapps.exchange_rates.model.rates

class BloombergRate : AvgRate() {

    override fun toRateType() {
        rateType = when (type) {
            "USDTRY Curncy" -> IRate.USD
            "EURTRY Curncy" -> IRate.EUR
            "EURUSD Curncy" -> IRate.EUR_USD
            "XAU Curncy" -> IRate.ONS
            else -> IRate.UNKNOWN
        }
    }

    override fun setRealValues() {
        if (rateType == IRate.UNKNOWN) return
        val value = avg_val.orEmpty().replace(".", "").replace(',', '.')
        avg_val = value
        val_real_avg = value.toFloat()
    }
}
