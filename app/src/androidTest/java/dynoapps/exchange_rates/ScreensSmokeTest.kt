package dynoapps.exchange_rates

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dynoapps.exchange_rates.alarm.AlarmsActivity
import dynoapps.exchange_rates.model.rates.IRate
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** Opens each screen on a real device and checks it reaches the resumed state without crashing. */
@RunWith(AndroidJUnit4::class)
class ScreensSmokeTest {

    @Test
    fun landingScreen_opens() {
        ActivityScenario.launch(LandingActivity::class.java).use { scenario ->
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
            // Give the providers time to fetch and render the first rates.
            Thread.sleep(8_000)
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
            Screenshots.take("landing")
        }
    }

    @Test
    fun chartScreen_opens() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), ChartActivity::class.java)
            .putExtra(ChartActivity.EXTRA_RATE_TYPE, IRate.USD)
        ActivityScenario.launch<ChartActivity>(intent).use { scenario ->
            Thread.sleep(3_000)
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
            Screenshots.take("chart_usd")
        }
    }

    @Test
    fun alarmsScreen_opens() {
        ActivityScenario.launch(AlarmsActivity::class.java).use { scenario ->
            Thread.sleep(2_000)
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
            Screenshots.take("alarms")
        }
    }
}
