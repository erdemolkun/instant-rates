package dynoapps.exchange_rates.converters

import dynoapps.exchange_rates.model.rates.BaseRate
import dynoapps.exchange_rates.model.rates.BloombergRate
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import retrofit2.Converter

object BloombergConverter : Converter<ResponseBody, List<BaseRate>> {
    private const val HOST = "https://www.bloomberght.com"

    override fun convert(value: ResponseBody): List<BaseRate> =
        Jsoup.parse(value.string(), HOST).getElementsByClass("data-info").map(::parseRate)

    private fun parseRate(element: Element): BloombergRate {
        val lastPriceElement = element.getElementsByClass("value LastPrice")
        return BloombergRate().apply {
            type = lastPriceElement.attr("data-secid")
            avg_val = lastPriceElement[0].textNodes()[0].wholeText
        }
    }
}
