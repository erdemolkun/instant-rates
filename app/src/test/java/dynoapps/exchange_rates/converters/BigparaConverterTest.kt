package dynoapps.exchange_rates.converters

import dynoapps.exchange_rates.model.rates.BigparaRate
import dynoapps.exchange_rates.model.rates.IRate
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class BigparaConverterTest {

    private fun fixture(name: String): String =
        checkNotNull(javaClass.getResource("/fixtures/$name")) { "Missing fixture $name" }.readText()

    private fun convert(json: String): List<BigparaRate> =
        BigparaConverter.convert(json.toResponseBody()).map { it as BigparaRate }

    @Test
    fun `parses every known symbol from a real response and skips unknown ones`() {
        val rates = convert(fixture("bigpara-current.json"))

        // The fixture also contains GBPTRY, which the app doesn't use.
        assertEquals(
            listOf(IRate.USD, IRate.EUR, IRate.EUR_USD, IRate.ONS, IRate.ONS_TRY),
            rates.map { it.rateType },
        )
        rates.forEach {
            val buy = checkNotNull(it.valueBuyReal)
            val sell = checkNotNull(it.valueSellReal)
            assertTrue("${it.type}: buy $buy, sell $sell", buy > 0f && sell >= buy)
        }
    }

    @Test
    fun `maps bid to buy and ask to sell`() {
        val rate = convert("""{"data":[{"s":"USDTRY","b":50.2508,"a":50.2654}],"errors":[],"success":true}""").single()

        assertEquals(IRate.USD, rate.rateType)
        assertEquals(50.2508f, rate.valueBuyReal!!, 0.00001f)
        assertEquals(50.2654f, rate.valueSellReal!!, 0.00001f)
    }

    @Test
    fun `skips entries without prices`() {
        val rates = convert(
            """{"data":[{"s":"USDTRY"},{"s":"EURTRY","b":55.88,"a":55.89}],"errors":[],"success":true}""",
        )

        assertEquals(listOf(IRate.EUR), rates.map { it.rateType })
    }

    @Test
    fun `fails when the API reports an error`() {
        assertThrows(IOException::class.java) {
            convert("""{"data":[],"errors":["Invalid symbol"],"success":false}""")
        }
    }

    @Test
    fun `fails when no rate is recognised`() {
        assertThrows(IOException::class.java) {
            convert("""{"data":[{"s":"GBPTRY","b":66.1,"a":66.2}],"errors":[],"success":true}""")
        }
    }

    @Test
    fun `fails on a non-JSON response such as an HTML error page`() {
        assertThrows(Exception::class.java) { convert("<html><body>Bad gateway</body></html>") }
    }
}
