package dynoapps.exchange_rates.model.rates

class EnparaRate : BuySellRate() {

    override fun toRateType() {
        val plainType = type.orEmpty().replace("\n", "").lowercase()
        rateType = when {
            plainType.contains("parite") -> IRate.EUR_USD
            plainType.contains("usd") -> IRate.USD
            plainType.contains("eur") -> IRate.EUR
            plainType.contains("(gram)") -> IRate.ONS_TRY
            else -> IRate.UNKNOWN
        }
    }

    /** Enpara uses "." as the thousands separator and "," as the decimal separator. */
    override fun parse(value: String?): Float =
        value.orEmpty().replace(" TL", "").replace(".", "").replace(",", ".").trim().toFloat()
}
