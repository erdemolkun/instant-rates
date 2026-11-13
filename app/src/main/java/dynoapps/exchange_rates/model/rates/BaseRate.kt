package dynoapps.exchange_rates.model.rates

import dynoapps.exchange_rates.util.RateUtils

abstract class BaseRate internal constructor() : IConvertable, IRate {

    var type: String? = null

    @get:IRate.RateDef
    override var rateType: Int = IRate.UNKNOWN
        protected set

    fun getFormatted(value: Float): String = RateUtils.valueToUI(value, rateType)
}
