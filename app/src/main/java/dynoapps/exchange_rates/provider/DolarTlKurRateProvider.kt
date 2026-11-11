package dynoapps.exchange_rates.provider

import dynoapps.exchange_rates.data.CurrencyType
import dynoapps.exchange_rates.model.rates.DolarTlKurRate
import dynoapps.exchange_rates.network.Api
import io.reactivex.rxjava3.core.Observable

class DolarTlKurRateProvider(onResult: (List<DolarTlKurRate>, Int) -> Unit) :
    BasePoolingProvider<DolarTlKurRate>(onResult) {

    override val sourceType: Int = CurrencyType.TLKUR

    // The timestamp query parameter defeats caching.
    override fun getObservable(): Observable<List<DolarTlKurRate>> =
        Api.dolarTlKurService.rates(System.currentTimeMillis().toString())
}
