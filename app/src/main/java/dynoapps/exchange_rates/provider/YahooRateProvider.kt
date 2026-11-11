package dynoapps.exchange_rates.provider

import dynoapps.exchange_rates.data.CurrencyType
import dynoapps.exchange_rates.model.rates.YahooRate
import dynoapps.exchange_rates.network.Api
import io.reactivex.rxjava3.core.Observable

class YahooRateProvider(onResult: (List<YahooRate>, Int) -> Unit) :
    BasePoolingProvider<YahooRate>(onResult) {

    override val sourceType: Int = CurrencyType.YAHOO

    override fun getObservable(): Observable<List<YahooRate>> = Api.yahooService.rates()
}
