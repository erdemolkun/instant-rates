package dynoapps.exchange_rates.util

import androidx.annotation.DrawableRes
import dynoapps.exchange_rates.App
import dynoapps.exchange_rates.R
import dynoapps.exchange_rates.model.rates.BaseRate
import dynoapps.exchange_rates.model.rates.IRate
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

object RateUtils {
    private val formatter2 = Formatter(2, 0)
    private val formatter5 = Formatter(5, 2)

    fun <T : BaseRate> getRate(rates: List<T>?, @IRate.RateDef rateType: Int): T? =
        rates?.firstOrNull { it.rateType == rateType }

    fun valueToUI(value: Float?, @IRate.RateDef rateType: Int): String {
        if (value == null) return ""
        val formatted = formatValue(value, rateType)
        return when (rateType) {
            IRate.EUR_USD -> formatted
            IRate.ONS -> App.context().getString(R.string.placeholder_dollar, formatted)
            else -> App.context().getString(R.string.placeholder_tl, formatted)
        }
    }

    fun formatValue(value: Float, rateType: Int): String =
        if (rateType == IRate.ONS || rateType == IRate.ONS_TRY) {
            formatter2.format(value)
        } else {
            formatter5.format(value)
        }

    // TODO refactor with side menu names.
    fun rateName(rateType: Int): String = when (rateType) {
        IRate.EUR -> "EUR"
        IRate.USD -> "USD"
        IRate.EUR_USD -> "EUR_USD"
        IRate.ONS -> "ONS"
        else -> ""
    }

    @DrawableRes
    fun getRateIcon(rateType: Int): Int = when (rateType) {
        IRate.EUR -> R.drawable.ic_euro
        IRate.ONS -> R.drawable.ic_gold
        IRate.USD -> R.drawable.ic_dollar
        IRate.EUR_USD -> R.drawable.ic_exchange_eur_usd
        else -> -1
    }

    /**
     * Parses [str] as a float, first in the "1234.56" form and then using the device locale's
     * separators. Returns null when neither works.
     */
    fun toFloat(str: String?): Float? {
        if (str.isNullOrEmpty()) return null
        val trimmed = str.trim()
        trimmed.toFloatOrNull()?.let { return it }
        return try {
            val localeSymbols = DecimalFormatSymbols.getInstance()
            val symbols = DecimalFormatSymbols().apply {
                decimalSeparator = localeSymbols.decimalSeparator
                groupingSeparator = localeSymbols.groupingSeparator
            }
            DecimalFormat("###,###,###,##0.#####", symbols).parse(trimmed)?.toFloat()
        } catch (ignored: Exception) {
            null
        }
    }
}
