package dynoapps.exchange_rates.model.rates

/** A Bigpara rate. [type] is the API's symbol code, such as "USDTRY". */
class BigparaRate : BuySellRate() {

    override fun toRateType() {
        rateType = when (type) {
            "USDTRY" -> IRate.USD
            "EURTRY" -> IRate.EUR
            "EURUSD" -> IRate.EUR_USD
            "XAUUSD" -> IRate.ONS
            "GLDGR" -> IRate.ONS_TRY
            else -> IRate.UNKNOWN
        }
    }

    /** The API returns numbers, which the converter stores directly; there is no text to parse. */
    override fun setRealValues() = Unit
}
