package dynoapps.exchange_rates.converters

import dynoapps.exchange_rates.model.rates.BloombergRate
import dynoapps.exchange_rates.model.rates.IRate
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class BloombergConverterTest {

    private fun fixture(name: String): String =
        checkNotNull(javaClass.getResource("/fixtures/$name")) { "Missing fixture $name" }.readText()

    private fun convert(json: String): List<BloombergRate> =
        BloombergConverter.convert(json.toResponseBody()).map { it as BloombergRate }

    private fun slider(vararg items: String): String =
        """{"body":{"financeSlider":{"items":[${items.joinToString()}]}}}"""

    private fun item(slug: String, lastPrice: String) = """{"slug":"$slug","lastPrice":"$lastPrice"}"""

    @Test
    fun `parses the four rates from a real response and skips the other slider items`() {
        // The fixture also has BIST 100, interest rate, Brent and the Baltic Dry index.
        val rates = convert(fixture("bloomberg-finance-slider.json")).associateBy { it.rateType }

        assertEquals(setOf(IRate.USD, IRate.EUR, IRate.EUR_USD, IRate.ONS), rates.keys)
        assertEquals(50.2579f, rates.getValue(IRate.USD).val_real_avg, 0.0001f)
        assertEquals(55.8984f, rates.getValue(IRate.EUR).val_real_avg, 0.0001f)
        assertEquals(1.1097f, rates.getValue(IRate.EUR_USD).val_real_avg, 0.0001f)
        // "3.946,41"-style prices: "." groups thousands.
        assertEquals(3947.76f, rates.getValue(IRate.ONS).val_real_avg, 0.01f)
    }

    @Test
    fun `skips items whose price is missing or malformed`() {
        val rates = convert(slider(item("dolar", ""), item("euro", "n/a"), item("eur-usd", "1,1093")))

        assertEquals(listOf(IRate.EUR_USD), rates.map { it.rateType })
    }

    @Test
    fun `fails when no rate is recognised`() {
        assertThrows(IOException::class.java) { convert(slider(item("bist-100", "11.830,06"))) }
    }

    @Test
    fun `fails when the response has no slider items`() {
        assertThrows(IOException::class.java) { convert("""{"body":{}}""") }
    }

    @Test
    fun `fails on a non-JSON response such as an HTML error page`() {
        assertThrows(Exception::class.java) { convert("<html><body>Bad gateway</body></html>") }
    }
}
