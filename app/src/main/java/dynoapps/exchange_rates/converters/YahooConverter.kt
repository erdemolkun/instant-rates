package dynoapps.exchange_rates.converters

import dynoapps.exchange_rates.model.rates.BaseRate
import dynoapps.exchange_rates.model.rates.YahooRate
import okhttp3.ResponseBody
import retrofit2.Converter

/** Parses CSV lines like `"USDTRY=X",3.4837,"12/9/2016","11:45pm"`. */
object YahooConverter : Converter<ResponseBody, List<BaseRate>> {

    override fun convert(value: ResponseBody): List<BaseRate> =
        value.string().splitDroppingTrailingEmpty(Regex("\n"))
            .map { it.splitDroppingTrailingEmpty(Regex(",")) }
            .filter { it.size > 2 }
            .map { parts ->
                YahooRate().apply {
                    type = parts[0].replace("\"", "")
                    avg_val = parts[1]
                    toRateType()
                    setRealValues()
                }
            }
}
