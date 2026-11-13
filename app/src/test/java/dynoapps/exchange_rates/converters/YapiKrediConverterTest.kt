package dynoapps.exchange_rates.converters

import dynoapps.exchange_rates.model.rates.IRate
import dynoapps.exchange_rates.model.rates.YapıKrediRate
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class YapiKrediConverterTest {

    private fun fixture(name: String): String =
        checkNotNull(javaClass.getResource("/fixtures/$name")) { "Missing fixture $name" }.readText()

    private fun convert(html: String): List<YapıKrediRate> =
        YapiKrediConverter.convert(html.toResponseBody()).map { it as YapıKrediRate }

    private fun table(vararg rows: String): String =
        """<table><tbody id="currencyResultContent">${rows.joinToString("")}</tbody></table>"""

    private fun row(code: String, buy: String, sell: String) =
        """<tr><td><i class="icon-arrow-up"></i>$code</td><td>Description</td><td>$buy</td><td>$sell</td><td>(% 0,05)</td></tr>"""

    @Test
    fun `parses gold, USD and EUR from the real page and skips other currencies`() {
        // The fixture lists gold first, then USD, EUR, silver and 13 more currencies.
        val rates = convert(fixture("yapikredi-doviz-kurlari.html")).associateBy { it.rateType }

        assertEquals(setOf(IRate.ONS_TRY, IRate.USD, IRate.EUR), rates.keys)
        // "6.270,99"-style prices: "." groups thousands.
        assertEquals(6270.99f, rates.getValue(IRate.ONS_TRY).valueBuyReal!!, 0.01f)
        assertEquals(6469.90f, rates.getValue(IRate.ONS_TRY).valueSellReal!!, 0.01f)
        assertEquals(49.71f, rates.getValue(IRate.USD).valueBuyReal!!, 0.001f)
        assertEquals(50.71f, rates.getValue(IRate.USD).valueSellReal!!, 0.001f)
        assertEquals(55.17f, rates.getValue(IRate.EUR).valueBuyReal!!, 0.001f)
        assertEquals(56.29f, rates.getValue(IRate.EUR).valueSellReal!!, 0.001f)
    }

    @Test
    fun `maps the Alış column to buy and the Satış column to sell`() {
        val rate = convert(table(row("USD", "49,71", "50,71"))).single()

        assertEquals(49.71f, rate.valueBuyReal!!, 0.001f)
        assertEquals(50.71f, rate.valueSellReal!!, 0.001f)
    }

    @Test
    fun `a malformed row is skipped without losing the other rows`() {
        // The old parser stopped at the first bad row, and the bad row was gold, the first one.
        val rates = convert(table(row("XAU", "-", "6.469,90"), row("USD", "49,71", "50,71"), row("EUR", "55,17", "56,29")))

        assertEquals(listOf(IRate.USD, IRate.EUR), rates.map { it.rateType })
    }

    @Test
    fun `skips rows with too few cells`() {
        val rates = convert(table("<tr><td>USD</td><td>49,71</td></tr>", row("EUR", "55,17", "56,29")))

        assertEquals(listOf(IRate.EUR), rates.map { it.rateType })
    }

    @Test
    fun `fails when the table is missing, for example on a redirect to the home page`() {
        assertThrows(IOException::class.java) { convert("<html><body><h1>Yapı Kredi</h1></body></html>") }
    }

    @Test
    fun `fails when no currency is recognised`() {
        assertThrows(IOException::class.java) { convert(table(row("GBP", "66,01", "67,31"))) }
    }
}
