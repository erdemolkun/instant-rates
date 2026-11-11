package dynoapps.exchange_rates

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dynoapps.exchange_rates.model.rates.AvgRate
import dynoapps.exchange_rates.model.rates.BaseRate
import dynoapps.exchange_rates.model.rates.BuySellRate
import dynoapps.exchange_rates.model.rates.IRate
import dynoapps.exchange_rates.provider.BasePoolingProvider
import dynoapps.exchange_rates.provider.BigparaRateProvider
import dynoapps.exchange_rates.provider.BloombergRateProvider
import dynoapps.exchange_rates.provider.YapıKrediRateProvider
import dynoapps.exchange_rates.provider.YorumlarRateProvider
import dynoapps.exchange_rates.util.RateUtils
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Fetches every rate source once through its real service and converter and writes a report to
 * `files/sources-report.txt`, which the CI emulator job prints. It never fails: an external site
 * being down must not break CI. The report shows which sources still work.
 */
@RunWith(AndroidJUnit4::class)
class SourcesProbeTest {

    @Test
    fun probeAllSources() {
        val ignore: (List<BaseRate>, Int) -> Unit = { _, _ -> }
        val providers: List<BasePoolingProvider<out BaseRate>> = listOf(
            YorumlarRateProvider(ignore),
            BigparaRateProvider(ignore),
            YapıKrediRateProvider(ignore),
            BloombergRateProvider(ignore),
        )

        val report = buildString {
            for (provider in providers) {
                val name = SourcesManager.getSourceName(provider.sourceType)
                val line = try {
                    val rates = provider.fetchBlocking().filter { it.rateType != IRate.UNKNOWN }
                    if (rates.isEmpty()) {
                        "EMPTY  no recognised rates"
                    } else {
                        "OK     " + rates.joinToString { "${RateUtils.rateName(it.rateType).ifEmpty { "ONS_TRY" }}=${value(it)}" }
                    }
                } catch (e: Throwable) {
                    val cause = generateSequence(e) { it.cause }.last()
                    "ERROR  ${cause.javaClass.simpleName}: ${cause.message?.take(160)}"
                }
                appendLine("${name.padEnd(14)} $line")
            }
        }

        val dir = ApplicationProvider.getApplicationContext<App>().getExternalFilesDir(null)
        File(dir, "sources-report.txt").writeText(report)
    }

    private fun value(rate: BaseRate): String = when (rate) {
        is AvgRate -> rate.val_real_avg.toString()
        is BuySellRate -> "${rate.valueBuyReal}/${rate.valueSellReal}"
        else -> "?"
    }
}
