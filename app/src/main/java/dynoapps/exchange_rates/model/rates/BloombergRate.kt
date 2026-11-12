package dynoapps.exchange_rates.model.rates

/** A Bloomberg HT rate. [type] is the site's slug, such as "dolar". */
class BloombergRate : AvgRate() {

    override fun toRateType() {
        rateType = when (type) {
            "dolar" -> IRate.USD
            "euro" -> IRate.EUR
            "eur-usd" -> IRate.EUR_USD
            "altin-ons" -> IRate.ONS
            else -> IRate.UNKNOWN
        }
    }

    /** The converter parses the price itself, so it can skip items whose price is malformed. */
    override fun setRealValues() = Unit
}
