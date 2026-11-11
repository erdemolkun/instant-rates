package dynoapps.exchange_rates.converters

import dynoapps.exchange_rates.model.rates.EnparaRate
import dynoapps.exchange_rates.model.rates.IRate
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

class EnparaConverterTest {

    @Test
    fun convert_parsesRowsAndSkipsMalformedOnes() {
        val rates = EnparaConverter.convert(HTML.toResponseBody("text/html".toMediaType()))

        assertEquals(2, rates.size)

        val usd = rates[0] as EnparaRate
        usd.toRateType()
        usd.setRealValues()
        assertEquals(IRate.USD, usd.rateType)
        assertEquals(41.85f, usd.valueBuyReal!!, 0.0001f)
        assertEquals(42.15f, usd.valueSellReal!!, 0.0001f)

        val gold = rates[1] as EnparaRate
        gold.toRateType()
        gold.setRealValues()
        assertEquals(IRate.ONS_TRY, gold.rateType)
        assertEquals(4512.30f, gold.valueBuyReal!!, 0.01f)
        assertEquals(4590.10f, gold.valueSellReal!!, 0.01f)
    }

    private companion object {
        val HTML = """
            <html><body>
            <div class="enpara-gold-exchange-rates__table-item">
              <div><span>USD ($)</span></div><div><span>41,8500 TL</span></div><div><span>42,1500 TL</span></div>
            </div>
            <div class="enpara-gold-exchange-rates__table-item">
              <div><span>Altın (gram)</span></div><div><span>4.512,30 TL</span></div><div><span>4.590,10 TL</span></div>
            </div>
            <!-- A header or malformed row without values must be skipped instead of producing a null type. -->
            <div class="enpara-gold-exchange-rates__table-item"><div><span>Döviz</span></div></div>
            </body></html>
        """.trimIndent()
    }
}
