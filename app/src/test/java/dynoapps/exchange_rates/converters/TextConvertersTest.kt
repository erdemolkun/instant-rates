package dynoapps.exchange_rates.converters

import dynoapps.exchange_rates.model.rates.IRate
import dynoapps.exchange_rates.model.rates.YorumlarRate
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

class TextConvertersTest {

    @Test
    fun yorumlar_parsesStatements_andSkipsTrailingEmptyParts() {
        // The trailing ";" and ")" leave empty parts that Java's split dropped; they must be skipped.
        val body = "dolar_guncelle(41.85,'12:00');euro_guncelle(48.12,'12:01');\n".toResponseBody()

        val rates = YorumlarConverter.convert(body).map { it as YorumlarRate }

        assertEquals(listOf(IRate.USD, IRate.EUR), rates.map { it.rateType })
        assertEquals(41.85f, rates[0].val_real_avg, 0.0001f)
        assertEquals("'12:01'", rates[1].time)
    }
}
