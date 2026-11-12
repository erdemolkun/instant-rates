package dynoapps.exchange_rates.alarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmInputTest {

    @Test
    fun `accepts empty text and values in range with up to four decimals`() {
        listOf("", "1", "38", "38.", "38.1234", "38,1234", "10000").forEach {
            assertTrue("\"$it\" should be accepted", isAcceptableAlarmInput(it))
        }
    }

    @Test
    fun `rejects too many decimals, extra separators, letters and out-of-range values`() {
        listOf("38.12345", "3.8.1", "38,1.2", "abc", "-5", "0", "0.5", "10000.5", "20000").forEach {
            assertFalse("\"$it\" should be rejected", isAcceptableAlarmInput(it))
        }
    }

    @Test
    fun `parses either separator as the decimal one`() {
        assertEquals(38.5f, parseAlarmInput("38,5"))
        assertEquals(38.5f, parseAlarmInput("38.5"))
        assertEquals(38f, parseAlarmInput("38."))
    }

    @Test
    fun `formats prefilled values as plain text the field accepts`() {
        assertEquals("38.1235", alarmInputText(38.12349f))
        assertEquals("2650.12", alarmInputText(2650.12f))
        assertEquals("40", alarmInputText(40f))
        assertTrue(isAcceptableAlarmInput(alarmInputText(2650.12f)))
    }
}
