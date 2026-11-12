package dynoapps.exchange_rates.provider

import dynoapps.exchange_rates.data.CurrencyType
import dynoapps.exchange_rates.model.rates.BloombergRate
import dynoapps.exchange_rates.network.Api

class BloombergRateProvider(onResult: (List<BloombergRate>, Int) -> Unit) :
    BasePoolingProvider<BloombergRate>(onResult) {

    override val sourceType: Int = CurrencyType.BLOOMBERGHT

    override suspend fun fetch(): List<BloombergRate> = Api.bloombergService.rates()
}
