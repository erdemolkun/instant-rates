package dynoapps.exchange_rates.provider

import dynoapps.exchange_rates.data.CurrencyType
import dynoapps.exchange_rates.model.rates.YapıKrediRate
import io.reactivex.rxjava3.core.Observable
import org.jsoup.Jsoup

class YapıKrediRateProvider(onResult: (List<YapıKrediRate>, Int) -> Unit) :
    BasePoolingProvider<YapıKrediRate>(onResult) {

    override val sourceType: Int = CurrencyType.YAPIKREDI

    override fun getObservable(): Observable<List<YapıKrediRate>> = Observable.fromCallable {
        val document = Jsoup.connect(URL)
            .header("Access-Control-Allow-Origin", "*")
            .referrer(REFERRER)
            .timeout(6000)
            .userAgent(USER_AGENT)
            .get()
        val rates = ArrayList<YapıKrediRate>()
        try {
            for (row in document.select("#currencyResultContent").select("tr")) {
                val cells = row.select("td")
                if (cells.size > 3) {
                    rates.add(YapıKrediRate().apply {
                        valueSell = cells[2].text()
                        valueBuy = cells[3].text()
                        type = cells[0].text()
                        toRateType()
                        setRealValues()
                    })
                }
            }
        } catch (ignored: Exception) {
            // Keep the rows parsed so far, as before.
        }
        rates
    }

    private companion object {
        const val URL =
            "https://www.yapikredi.com.tr/yatirimci-kosesi/doviz-bilgileri.aspx/LoadInternetCurrencies"
        const val REFERRER =
            "https://www.yapikredi.com.tr/yatirimci-kosesi/doviz-bilgileri.aspx?section=internet"
        const val USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_1) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/54.0.2840.98 Safari/537.36"
    }
}
