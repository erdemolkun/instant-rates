package dynoapps.exchange_rates.provider

import dynoapps.exchange_rates.data.CurrencyType
import dynoapps.exchange_rates.model.rates.EnparaRate
import dynoapps.exchange_rates.network.Api
import dynoapps.exchange_rates.util.L
import io.reactivex.rxjava3.core.Observable

class EnparaRateProvider(onResult: (List<EnparaRate>, Int) -> Unit) :
    BasePoolingProvider<EnparaRate>(onResult) {

    override val sourceType: Int = CurrencyType.ENPARA

    /** Rows whose values don't parse are dropped instead of failing the whole fetch. */
    override fun getObservable(): Observable<List<EnparaRate>> =
        Api.enparaService.rates().map { rates ->
            rates.filter { rate ->
                try {
                    rate.toRateType()
                    rate.setRealValues()
                    true
                } catch (e: RuntimeException) {
                    L.ex(e, "Skipping unparsable Enpara rate: ${rate.type}")
                    false
                }
            }
        }
}
