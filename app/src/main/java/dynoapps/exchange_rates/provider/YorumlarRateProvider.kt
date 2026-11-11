package dynoapps.exchange_rates.provider

import dynoapps.exchange_rates.data.CurrencyType
import dynoapps.exchange_rates.model.rates.YorumlarRate
import dynoapps.exchange_rates.network.Api
import io.reactivex.rxjava3.core.Observable

class YorumlarRateProvider(onResult: (List<YorumlarRate>, Int) -> Unit) :
    BasePoolingProvider<YorumlarRate>(onResult) {

    override val sourceType: Int = CurrencyType.ALTININ

    override fun getObservable(): Observable<List<YorumlarRate>> = Api.yorumlarService.rates("ons")
}
