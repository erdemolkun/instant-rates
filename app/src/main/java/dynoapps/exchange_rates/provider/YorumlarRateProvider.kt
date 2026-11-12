package dynoapps.exchange_rates.provider

import dynoapps.exchange_rates.data.CurrencyType
import dynoapps.exchange_rates.model.rates.YorumlarRate
import dynoapps.exchange_rates.network.Api

class YorumlarRateProvider(onResult: (List<YorumlarRate>, Int) -> Unit) :
    BasePoolingProvider<YorumlarRate>(onResult) {

    override val sourceType: Int = CurrencyType.ALTININ

    override suspend fun fetch(): List<YorumlarRate> = Api.yorumlarService.rates("ons")
}
