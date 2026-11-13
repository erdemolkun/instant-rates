package dynoapps.exchange_rates.alarm

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dynoapps.exchange_rates.R
import dynoapps.exchange_rates.data.CurrencySource
import dynoapps.exchange_rates.data.CurrencyType
import dynoapps.exchange_rates.model.rates.IRate
import dynoapps.exchange_rates.ui.compose.RatesTheme
import dynoapps.exchange_rates.util.RateUtils
import java.math.RoundingMode

private const val MAX_DECIMAL_DIGITS = 4
private const val MIN_ALARM_VALUE = 1.0
private const val MAX_ALARM_VALUE = 10_000.0
private val ALARM_INPUT_PATTERN = Regex("""\d*([.,]\d{0,$MAX_DECIMAL_DIGITS})?""")

/**
 * Whether [text] may be typed into the alarm value field: digits with at most one "." or ","
 * separator followed by up to 4 digits, and a value between 1 and 10000. Empty text is allowed
 * so the field can be cleared.
 */
internal fun isAcceptableAlarmInput(text: String): Boolean {
    if (text.isEmpty()) return true
    if (!ALARM_INPUT_PATTERN.matches(text)) return false
    val value = parseAlarmInput(text) ?: return false
    return value.toDouble() in MIN_ALARM_VALUE..MAX_ALARM_VALUE
}

/** The value typed into the field. Its only separator is the decimal one, whether "." or ",". */
internal fun parseAlarmInput(text: String): Float? = text.replace(',', '.').toFloatOrNull()

/** [value] as plain text the value field accepts: no grouping, "." separator, at most 4 decimals. */
internal fun alarmInputText(value: Float): String =
    value.toBigDecimal().setScale(MAX_DECIMAL_DIGITS, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()

/**
 * The "add alarm" form: a value, whether to fire above or below it, and the source and rate type
 * to watch. [onAdd] is only called with valid input; otherwise the dialog stays open and shows an
 * error.
 */
@Composable
fun AddAlarmDialog(
    sources: List<CurrencySource>,
    initialSourceType: Int,
    initialRateType: Int,
    initialValue: String,
    onAdd: (value: Float, isAbove: Boolean, sourceType: Int, rateType: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var value by remember { mutableStateOf(TextFieldValue(initialValue, TextRange(initialValue.length))) }
    var showError by remember { mutableStateOf(false) }
    var isAbove by remember { mutableStateOf(true) }
    var source by remember {
        mutableStateOf(sources.firstOrNull { it.type == initialSourceType } ?: sources.firstOrNull())
    }
    val rateTypes = remember(source) {
        source?.supportedRates?.filter { RateUtils.rateName(it).isNotEmpty() }.orEmpty()
    }
    // Reset to the requested rate type, or the first one, whenever the source changes.
    var rateType by remember(source) {
        mutableStateOf(rateTypes.firstOrNull { it == initialRateType } ?: rateTypes.firstOrNull())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_store_icon_24dp),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text(stringResource(R.string.add_alarm)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        if (isAcceptableAlarmInput(it.text)) {
                            value = it
                            showError = false
                        }
                    },
                    label = { Text(stringResource(R.string.enter_value)) },
                    isError = showError,
                    supportingText = if (showError) {
                        { Text(stringResource(R.string.check_value)) }
                    } else {
                        null
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(Modifier.selectableGroup()) {
                    DirectionOption(stringResource(R.string.if_above), selected = isAbove) { isAbove = true }
                    DirectionOption(stringResource(R.string.if_below), selected = !isAbove) { isAbove = false }
                }
                DropdownField(
                    label = stringResource(R.string.sources),
                    items = sources,
                    selected = source,
                    itemText = { it.name },
                    onSelected = { source = it },
                )
                if (rateTypes.isNotEmpty()) {
                    DropdownField(
                        label = stringResource(R.string.rate_types),
                        items = rateTypes,
                        selected = rateType,
                        itemText = RateUtils::rateName,
                        onSelected = { rateType = it },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val alarmValue = parseAlarmInput(value.text)
                    val alarmSource = source
                    val alarmRateType = rateType
                    if (alarmValue == null || alarmSource == null || alarmRateType == null) {
                        showError = true
                        Toast.makeText(context, R.string.check_value, Toast.LENGTH_SHORT).show()
                    } else {
                        onAdd(alarmValue, isAbove, alarmSource.type, alarmRateType)
                    }
                },
            ) { Text(stringResource(R.string.add)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dismiss)) } },
    )
}

@Composable
private fun RowScope.DirectionOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 48.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownField(
    label: String,
    items: List<T>,
    selected: T?,
    itemText: (T) -> String,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.let(itemText).orEmpty(),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemText(item)) },
                    onClick = {
                        onSelected(item)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Preview
@Composable
private fun AddAlarmDialogPreview() = RatesTheme {
    AddAlarmDialog(
        sources = listOf(
            CurrencySource("Altın.in", CurrencyType.ALTININ, 0, true, intArrayOf(IRate.USD, IRate.EUR)),
            CurrencySource("Bloomberg HT", CurrencyType.BLOOMBERGHT, 0, true, intArrayOf(IRate.USD)),
        ),
        initialSourceType = CurrencyType.ALTININ,
        initialRateType = IRate.EUR,
        initialValue = "38.1234",
        onAdd = { _, _, _, _ -> },
        onDismiss = {},
    )
}
