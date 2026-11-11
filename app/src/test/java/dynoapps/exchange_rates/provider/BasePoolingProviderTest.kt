package dynoapps.exchange_rates.provider

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BasePoolingProviderTest {

    @Test
    fun errorRetryDelay_neverBelowBaseDelay_forHealthySource() {
        // Previously 1 error after 100 successes produced a negative delay (instant retry).
        assertEquals(4000L, BasePoolingProvider.errorRetryDelay(1, 100))
        assertEquals(4000L, BasePoolingProvider.errorRetryDelay(1, 1))
        assertEquals(4000L, BasePoolingProvider.errorRetryDelay(0, 0))
    }

    @Test
    fun errorRetryDelay_growsWithFailureRatio() {
        val few = BasePoolingProvider.errorRetryDelay(3, 1)
        val many = BasePoolingProvider.errorRetryDelay(10, 1)
        assertTrue(few > 4000)
        assertTrue(many > few)
    }

    @Test
    fun errorRetryDelay_isCapped() {
        assertEquals(60000L, BasePoolingProvider.errorRetryDelay(Int.MAX_VALUE, 0))
    }
}
