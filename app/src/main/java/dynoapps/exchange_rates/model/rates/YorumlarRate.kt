package dynoapps.exchange_rates.model.rates

class YorumlarRate : AvgRate() {

    var time: String? = null

    override fun toRateType() {
        rateType = when (type) {
            "dolar_guncelle" -> IRate.USD
            "euro_guncelle" -> IRate.EUR
            "parite_guncelle" -> IRate.EUR_USD
            "ons_guncelle" -> IRate.ONS
            else -> IRate.UNKNOWN
        }
    }

    override fun setRealValues() {
        if (rateType == IRate.UNKNOWN) return
        val_real_avg = avg_val.orEmpty().replace("'", "").replace("$", "").trim().toFloat()
    }

    override fun toString(): String =
        "${type.orEmpty().split("_")[0]} -> : $val_real_avg : Time -> ${time.orEmpty().replace("'", "")}"
}
