package dynoapps.exchange_rates.provider

import dynoapps.exchange_rates.data.CurrencyType
import dynoapps.exchange_rates.model.rates.BloombergRate
import dynoapps.exchange_rates.network.Api
import io.reactivex.rxjava3.core.Observable

class BloombergRateProvider(onResult: (List<BloombergRate>, Int) -> Unit) :
    BasePoolingProvider<BloombergRate>(onResult) {

    override val sourceType: Int = CurrencyType.BLOOMBERGHT

    override fun getObservable(): Observable<List<BloombergRate>> = Api.bloombergService.rates()
}
