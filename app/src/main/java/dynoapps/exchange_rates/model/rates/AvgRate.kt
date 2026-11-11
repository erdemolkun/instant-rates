package dynoapps.exchange_rates.model.rates

abstract class AvgRate : BaseRate() {

    var avg_val: String? = null

    var val_real_avg: Float = 0f

    override fun getValue(valueType: Int): Float = val_real_avg

    override fun toString(): String = "AvgRate{avg_val='$avg_val', val_real_avg=$val_real_avg}"
}
