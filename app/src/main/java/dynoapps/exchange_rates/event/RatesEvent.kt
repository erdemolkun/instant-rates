package dynoapps.exchange_rates.event

import dynoapps.exchange_rates.model.rates.BaseRate

class RatesEvent<T : BaseRate>(
    val rates: List<T>,
    val sourceType: Int,
    val fetchTime: Long,
)
