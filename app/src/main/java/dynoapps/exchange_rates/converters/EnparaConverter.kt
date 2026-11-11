package dynoapps.exchange_rates.converters

import dynoapps.exchange_rates.model.rates.BaseRate
import dynoapps.exchange_rates.model.rates.EnparaRate
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import retrofit2.Converter

object EnparaConverter : Converter<ResponseBody, List<BaseRate>> {
    private const val HOST = "https://www.qnbfinansbank.enpara.com"

    override fun convert(value: ResponseBody): List<BaseRate> =
        Jsoup.parse(value.string(), HOST)
            .getElementsByClass("enpara-gold-exchange-rates__table-item")
            .mapNotNull(::parseRate)

    /** @return the parsed rate, or null for rows that don't have type, buy and sell values. */
    private fun parseRate(element: Element): EnparaRate? {
        val spans = element.select("div span")
        if (spans.size <= 2) return null
        return EnparaRate().apply {
            type = spans[0].text()
            valueBuy = spans[1].text()
            valueSell = spans[2].text()
        }
    }
}
