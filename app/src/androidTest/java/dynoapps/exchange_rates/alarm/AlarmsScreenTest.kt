package dynoapps.exchange_rates.alarm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dynoapps.exchange_rates.R
import dynoapps.exchange_rates.ui.compose.RatesTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlarmsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun item(id: Long, enabled: Boolean = true) = AlarmItemState(
        id = id,
        isAbove = true,
        value = "50.30 TL",
        rateIcon = R.drawable.ic_dollar,
        sourceName = "Altın.in",
        sourceColor = 0xFFF1C15A.toInt(),
        isEnabled = enabled,
    )

    /** Shows [initial] with state handled like the ViewModel does, minus the repository. */
    private fun showScreen(initial: List<AlarmItemState>, alarmsEnabled: Boolean = true) {
        composeRule.setContent {
            var items by mutableStateOf(initial)
            var enabled by mutableStateOf(alarmsEnabled)
            RatesTheme {
                AlarmsScreen(
                    items = items,
                    alarmsEnabled = enabled,
                    onAlarmsEnabledChange = { enabled = it },
                    onAlarmEnabledChange = { id, on -> items = items.map { if (it.id == id) it.copy(isEnabled = on) else it } },
                    onDelete = { id -> items = items.filterNot { it.id == id } },
                    onAdd = {},
                    onBack = {},
                )
            }
        }
    }

    @Test
    fun showsTheEmptyStateWithoutAlarms() {
        showScreen(emptyList())

        composeRule.onNodeWithTag(AlarmsScreenTags.EMPTY).assertIsDisplayed()
        composeRule.onNodeWithTag(AlarmsScreenTags.LIST).assertDoesNotExist()
    }

    @Test
    fun togglingAnAlarmSwitchesItOff() {
        showScreen(listOf(item(1)))

        composeRule.onNodeWithTag(AlarmsScreenTags.itemSwitch(1)).assertIsOn().performClick()

        composeRule.onNodeWithTag(AlarmsScreenTags.itemSwitch(1)).assertIsOff()
    }

    @Test
    fun deletingTheLastAlarmShowsTheEmptyState() {
        showScreen(listOf(item(1)))

        composeRule.onNodeWithTag(AlarmsScreenTags.itemDelete(1)).performClick()

        composeRule.onNodeWithTag(AlarmsScreenTags.item(1)).assertDoesNotExist()
        composeRule.onNodeWithTag(AlarmsScreenTags.EMPTY).assertIsDisplayed()
    }

    @Test
    fun theMasterSwitchReportsChanges() {
        var reported: Boolean? = null
        composeRule.setContent {
            RatesTheme {
                AlarmsScreen(
                    items = listOf(item(1)),
                    alarmsEnabled = true,
                    onAlarmsEnabledChange = { reported = it },
                    onAlarmEnabledChange = { _, _ -> },
                    onDelete = {},
                    onAdd = {},
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithTag(AlarmsScreenTags.MASTER_SWITCH).performClick()

        assertEquals(false, reported)
    }
}
