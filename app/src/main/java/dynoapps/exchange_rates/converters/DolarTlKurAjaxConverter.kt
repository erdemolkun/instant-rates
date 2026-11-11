package dynoapps.exchange_rates.converters

import dynoapps.exchange_rates.model.rates.BaseRate
import dynoapps.exchange_rates.model.rates.DolarTlKurRate
import okhttp3.ResponseBody
import retrofit2.Converter

/** Parses lines like "USDTRY:41.85". */
object DolarTlKurAjaxConverter : Converter<ResponseBody, List<BaseRate>> {

    override fun convert(value: ResponseBody): List<BaseRate> =
        value.string().splitDroppingTrailingEmpty(Regex("\n"))
            .map { it.splitDroppingTrailingEmpty(Regex(":")) }
            .filter { it.size == 2 }
            .map { parts ->
                DolarTlKurRate().apply {
                    type = parts[0]
                    avg_val = parts[1]
                    toRateType()
                    setRealValues()
                }
            }
}
