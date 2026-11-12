package dynoapps.exchange_rates.provider

import dynoapps.exchange_rates.data.CurrencyType
import dynoapps.exchange_rates.model.rates.BigparaRate
import dynoapps.exchange_rates.network.Api
import io.reactivex.rxjava3.core.Observable

class BigparaRateProvider(onResult: (List<BigparaRate>, Int) -> Unit) :
    BasePoolingProvider<BigparaRate>(onResult) {

    override val sourceType: Int = CurrencyType.BIGPARA

    override fun getObservable(): Observable<List<BigparaRate>> = Api.bigparaService.rates(SYMBOLS)

    private companion object {
        // Only the rates Bigpara is listed for in SourcesManager. The converter also knows EURTRY,
        // EURUSD, XAUUSD and GLDGR.
        const val SYMBOLS = "USDTRY"
    }
}
