package dynoapps.exchange_rates.provider;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BasePoolingProviderTest {

    @Test
    public void errorRetryDelay_neverBelowBaseDelay_forHealthySource() {
        // Previously 1 error after 100 successes produced a negative delay (instant retry).
        assertEquals(4000, BasePoolingProvider.errorRetryDelay(1, 100));
        assertEquals(4000, BasePoolingProvider.errorRetryDelay(1, 1));
        assertEquals(4000, BasePoolingProvider.errorRetryDelay(0, 0));
    }

    @Test
    public void errorRetryDelay_growsWithFailureRatio() {
        long few = BasePoolingProvider.errorRetryDelay(3, 1);
        long many = BasePoolingProvider.errorRetryDelay(10, 1);
        assertTrue(few > 4000);
        assertTrue(many > few);
    }

    @Test
    public void errorRetryDelay_isCapped() {
        assertEquals(60000, BasePoolingProvider.errorRetryDelay(Integer.MAX_VALUE, 0));
    }
}
