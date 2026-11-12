package dynoapps.exchange_rates

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dynoapps.exchange_rates.interfaces.ValueType
import dynoapps.exchange_rates.model.rates.IRate
import dynoapps.exchange_rates.ui.compose.RatesTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LandingScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val altinUsd = RateCardState(sourceType = 1, valueType = ValueType.AVG, title = "Altın.in", value = "50.2553 TL")

    private val rows = listOf(
        RateRowState(IRate.USD, R.drawable.ic_dollar, insetIcon = true, cards = listOf(altinUsd)),
        RateRowState(IRate.ONS_TRY, R.drawable.ic_gold, insetIcon = false, cards = emptyList()),
    )

    private var clickedRate: Int? = null
    private var longClicked: Pair<Int, RateCardState>? = null
    private var sourcesClicks = 0

    private fun showScreen() {
        composeRule.setContent {
            RatesTheme {
                LandingScreen(
                    rows = rows,
                    intervalSelection = "10 secs",
                    onCardClick = { clickedRate = it },
                    onCardLongClick = { rateType, card -> longClicked = rateType to card },
                    onMenuClick = {},
                    onIntervalClick = {},
                    onSourcesClick = { sourcesClicks++ },
                    onAddAlarmClick = {},
                )
            }
        }
    }

    @Test
    fun showsCardValuesAndTheNoProviderMessage() {
        showScreen()

        composeRule.onNodeWithText("50.2553 TL").assertIsDisplayed()
        composeRule.onNodeWithTag(LandingScreenTags.noProvider(IRate.ONS_TRY)).assertIsDisplayed()
        composeRule.onNodeWithTag(LandingScreenTags.noProvider(IRate.USD)).assertDoesNotExist()
    }

    @Test
    fun tappingACardOpensItsRateAndLongPressOffersAnAlarm() {
        showScreen()
        val card = composeRule.onNodeWithTag(LandingScreenTags.card(IRate.USD, altinUsd.sourceType, altinUsd.valueType))

        card.performClick()
        card.performTouchInput { longClick() }

        assertEquals(IRate.USD, clickedRate)
        assertEquals(IRate.USD to altinUsd, longClicked)
    }

    @Test
    fun theSettingsIconInTheMessageOpensTheSources() {
        showScreen()

        composeRule.onNodeWithTag(LandingScreenTags.NO_PROVIDER_SETTINGS, useUnmergedTree = true).performClick()

        assertEquals(1, sourcesClicks)
    }
}
