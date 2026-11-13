package dynoapps.exchange_rates.converters

import dynoapps.exchange_rates.model.rates.BaseRate
import dynoapps.exchange_rates.model.rates.BigparaRate
import dynoapps.exchange_rates.model.rates.IRate
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Converter
import java.io.IOException

/**
 * Parses Bigpara's `api/trade/current` response:
 * `{"data":[{"s":"USDTRY","b":50.25,"a":50.26,…},…],"success":true}`, where `b` is the bid
 * (Alış) and `a` the ask (Satış). Unknown symbols and entries without prices are skipped.
 */
object BigparaConverter : Converter<ResponseBody, List<BaseRate>> {

    override fun convert(value: ResponseBody): List<BaseRate> {
        val json = JSONObject(value.string())
        if (!json.optBoolean("success")) {
            throw IOException("Bigpara request failed: ${json.optJSONArray("errors")}")
        }
        val data = json.optJSONArray("data") ?: throw IOException("Bigpara response has no data")

        val rates = (0 until data.length()).mapNotNull { index ->
            val item = data.optJSONObject(index) ?: return@mapNotNull null
            val buy = item.optDouble("b")
            val sell = item.optDouble("a")
            if (buy.isNaN() || sell.isNaN()) return@mapNotNull null
            BigparaRate().apply {
                type = item.optString("s")
                toRateType()
                valueBuyReal = buy.toFloat()
                valueSellReal = sell.toFloat()
            }.takeIf { it.rateType != IRate.UNKNOWN }
        }
        // An empty result is a failure, so the provider backs off instead of polling at full speed.
        if (rates.isEmpty()) throw IOException("Bigpara response has no recognised rates")
        return rates
    }
}
