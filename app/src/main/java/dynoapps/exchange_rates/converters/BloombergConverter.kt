package dynoapps.exchange_rates.converters

import dynoapps.exchange_rates.model.rates.BaseRate
import dynoapps.exchange_rates.model.rates.BloombergRate
import dynoapps.exchange_rates.model.rates.IRate
import dynoapps.exchange_rates.util.RateUtils
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Converter
import java.io.IOException

/**
 * Parses Bloomberg HT's `finance-slider-data` response:
 * `{"body":{"financeSlider":{"items":[{"slug":"dolar","lastPrice":"50,2582",…},…]}}}`.
 * Prices are Turkish-formatted strings. Unknown slugs and unparseable prices are skipped.
 */
object BloombergConverter : Converter<ResponseBody, List<BaseRate>> {

    override fun convert(value: ResponseBody): List<BaseRate> {
        val items = JSONObject(value.string())
            .optJSONObject("body")
            ?.optJSONObject("financeSlider")
            ?.optJSONArray("items")
            ?: throw IOException("Bloomberg HT response has no finance slider items")

        val rates = (0 until items.length()).mapNotNull { index ->
            val item = items.optJSONObject(index) ?: return@mapNotNull null
            val lastPrice = item.optString("lastPrice")
            val price = RateUtils.parseTurkish(lastPrice) ?: return@mapNotNull null
            BloombergRate().apply {
                type = item.optString("slug")
                toRateType()
                avg_val = lastPrice
                val_real_avg = price
            }.takeIf { it.rateType != IRate.UNKNOWN }
        }
        // An empty result is a failure, so the provider backs off instead of polling at full speed.
        if (rates.isEmpty()) throw IOException("Bloomberg HT response has no recognised rates")
        return rates
    }
}
