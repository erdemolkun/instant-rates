package dynoapps.exchange_rates.converters

import dynoapps.exchange_rates.model.rates.BaseRate
import dynoapps.exchange_rates.model.rates.BigparaRate
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Converter

object BigparaConverter : Converter<ResponseBody, List<BaseRate>> {
    private const val HOST = "http://www.bigpara.com"

    override fun convert(value: ResponseBody): List<BaseRate> {
        val elements = Jsoup.parse(value.string(), HOST)
            .select("#content").select(".kurdetail").select(".kurbox")
        val rate = BigparaRate().apply {
            type = elements[0].text()
            valueBuy = elements[1].select(".value").text()
            valueSell = elements[2].select(".value").text()
            toRateType()
            setRealValues()
        }
        return listOf(rate)
    }
}
