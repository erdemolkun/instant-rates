package dynoapps.exchange_rates.converters

import dynoapps.exchange_rates.model.rates.BaseRate
import dynoapps.exchange_rates.model.rates.IRate
import dynoapps.exchange_rates.model.rates.YapıKrediRate
import dynoapps.exchange_rates.util.RateUtils
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import retrofit2.Converter
import java.io.IOException

/**
 * Parses the rates table on Yapı Kredi's exchange rates page. Each row of
 * `tbody#currencyResultContent` holds the currency code, a description, the bank's buying price
 * (Alış) and its selling price (Satış), in Turkish number format. Unknown currencies and rows that
 * can't be parsed are skipped.
 */
object YapiKrediConverter : Converter<ResponseBody, List<BaseRate>> {
    private const val HOST = "https://www.yapikredi.com.tr"

    override fun convert(value: ResponseBody): List<BaseRate> {
        val rates = Jsoup.parse(value.string(), HOST)
            .select("tbody#currencyResultContent > tr")
            .mapNotNull(::parseRow)
        // An empty result is a failure, so the provider backs off instead of polling at full speed.
        if (rates.isEmpty()) throw IOException("Yapı Kredi page has no recognised rates")
        return rates
    }

    private fun parseRow(row: Element): YapıKrediRate? {
        val cells = row.select("> td")
        if (cells.size < 4) return null
        val buy = RateUtils.parseTurkish(cells[2].text()) ?: return null
        val sell = RateUtils.parseTurkish(cells[3].text()) ?: return null
        return YapıKrediRate().apply {
            type = cells[0].text()
            toRateType()
            valueBuyReal = buy
            valueSellReal = sell
        }.takeIf { it.rateType != IRate.UNKNOWN }
    }
}
