package dynoapps.exchange_rates.model.rates

/** A Yapı Kredi rate. [type] is the currency code from the rates table, such as "USD". */
class YapıKrediRate : BuySellRate() {

    override fun toRateType() {
        rateType = when (type?.trim()) {
            "USD" -> IRate.USD
            "EUR" -> IRate.EUR
            // Gold in TL per gram.
            "XAU" -> IRate.ONS_TRY
            else -> IRate.UNKNOWN
        }
    }
}
