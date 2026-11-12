package dynoapps.exchange_rates

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dynoapps.exchange_rates.ui.compose.RatesTheme
import dynoapps.exchange_rates.ui.compose.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemeDialogTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val chosen = mutableListOf<ThemeMode>()
    private var dismissed = false

    /** Shows the dialog with the selection handled like ThemeSettings does. */
    private fun showDialog() {
        var selected by mutableStateOf(ThemeMode.SYSTEM)
        composeRule.setContent {
            RatesTheme {
                ThemeDialog(
                    selected = selected,
                    onSelect = { chosen += it; selected = it },
                    onDismiss = { dismissed = true },
                )
            }
        }
    }

    @Test
    fun tappingAnOptionAppliesItAtOnce() {
        showDialog()
        composeRule.onNodeWithText("System default").assertIsSelected()

        composeRule.onNodeWithText("Dark").performClick()

        composeRule.onNodeWithText("Dark").assertIsSelected()
        composeRule.onNodeWithText("System default").assertIsNotSelected()
        assertEquals(listOf(ThemeMode.DARK), chosen)
        assertEquals(false, dismissed)
    }

    @Test
    fun okIsTheOnlyButtonAndClosesTheDialog() {
        showDialog()
        composeRule.onNodeWithText("Apply").assertDoesNotExist()
        composeRule.onNodeWithText("Dismiss").assertDoesNotExist()

        composeRule.onNodeWithText("OK").performClick()

        assertTrue(dismissed)
        assertEquals(emptyList<ThemeMode>(), chosen)
    }
}
