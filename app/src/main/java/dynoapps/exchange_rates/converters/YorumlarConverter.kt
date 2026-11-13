package dynoapps.exchange_rates.converters

import dynoapps.exchange_rates.model.rates.BaseRate
import dynoapps.exchange_rates.model.rates.YorumlarRate
import okhttp3.ResponseBody
import retrofit2.Converter

/** Parses `type(value,'time');` statements. */
object YorumlarConverter : Converter<ResponseBody, List<BaseRate>> {

    override fun convert(value: ResponseBody): List<BaseRate> =
        value.string().splitDroppingTrailingEmpty(Regex(";"))
            .map { it.splitDroppingTrailingEmpty(Regex("[(),]")) }
            .filter { it.size > 2 }
            .map { parts ->
                YorumlarRate().apply {
                    type = parts[0]
                    avg_val = parts[1]
                    time = parts[2]
                    toRateType()
                    setRealValues()
                }
            }
}
