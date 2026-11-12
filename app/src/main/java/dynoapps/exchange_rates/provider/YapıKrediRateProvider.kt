package dynoapps.exchange_rates.provider

import dynoapps.exchange_rates.data.CurrencyType
import dynoapps.exchange_rates.model.rates.YapıKrediRate
import dynoapps.exchange_rates.network.Api
import io.reactivex.rxjava3.core.Observable

class YapıKrediRateProvider(onResult: (List<YapıKrediRate>, Int) -> Unit) :
    BasePoolingProvider<YapıKrediRate>(onResult) {

    override val sourceType: Int = CurrencyType.YAPIKREDI

    override fun getObservable(): Observable<List<YapıKrediRate>> = Api.yapiKrediService.rates()
}
