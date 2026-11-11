package dynoapps.exchange_rates.model.rates

class YapıKrediRate : BuySellRate() {

    override fun toRateType() {
        val plainType = type.orEmpty().replace("\n", "").lowercase()
        rateType = when {
            plainType == "usd" -> IRate.USD
            plainType == "eur" -> IRate.EUR
            plainType.contains("altın") -> IRate.ONS_TRY
            plainType.contains("parite") -> IRate.EUR_USD
            else -> IRate.UNKNOWN
        }
    }
}
