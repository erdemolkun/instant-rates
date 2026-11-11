package dynoapps.exchange_rates.provider

import dynoapps.exchange_rates.data.CurrencyType
import dynoapps.exchange_rates.model.rates.ParaGarantiRate
import dynoapps.exchange_rates.network.Api
import io.reactivex.rxjava3.core.Observable

class ParaGarantiRateProvider(onResult: (List<ParaGarantiRate>, Int) -> Unit) :
    BasePoolingProvider<ParaGarantiRate>(onResult) {

    override val sourceType: Int = CurrencyType.PARAGARANTI

    override fun getObservable(): Observable<List<ParaGarantiRate>> =
        Api.paraGarantiService.rates().map { rates ->
            rates.onEach {
                it.toRateType()
                it.setRealValues()
            }
        }
}
