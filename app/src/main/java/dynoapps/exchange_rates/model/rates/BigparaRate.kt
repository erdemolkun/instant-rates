package dynoapps.exchange_rates.model.rates

class BigparaRate : BuySellRate() {

    override fun toRateType() {
        val plainType = type.orEmpty().lowercase()
        rateType = when {
            plainType.contains("dolar") -> IRate.USD
            plainType.contains("euro") -> IRate.EUR
            plainType.contains("altın") -> IRate.ONS_TRY
            plainType.contains("parite") -> IRate.EUR_USD
            else -> IRate.UNKNOWN
        }
    }
}
