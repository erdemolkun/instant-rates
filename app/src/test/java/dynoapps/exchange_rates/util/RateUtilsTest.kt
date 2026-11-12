package dynoapps.exchange_rates.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RateUtilsTest {

    @Test
    fun `parseTurkish reads dot as thousands grouping and comma as decimal separator`() {
        assertEquals(3946.41f, RateUtils.parseTurkish("3.946,41")!!, 0.001f)
        assertEquals(11830.06f, RateUtils.parseTurkish("11.830,06")!!, 0.001f)
        assertEquals(50.2582f, RateUtils.parseTurkish(" 50,2582 ")!!, 0.00001f)
        assertEquals(-1.04f, RateUtils.parseTurkish("-1,04")!!, 0.00001f)
        assertEquals(42f, RateUtils.parseTurkish("42")!!, 0f)
    }

    @Test
    fun `parseTurkish returns null for text that isn't a number`() {
        listOf(null, "", "  ", "n/a", "1,2,3").forEach { assertNull("\"$it\"", RateUtils.parseTurkish(it)) }
    }
}
