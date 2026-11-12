package dynoapps.exchange_rates.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/** A dialog with a checkbox per item. [onConfirm] gets the checked state of every item. */
@Composable
fun MultiChoiceDialog(
    title: String,
    items: List<String>,
    initialChecked: List<Boolean>,
    confirmText: String,
    dismissText: String,
    onConfirm: (checked: List<Boolean>) -> Unit,
    onDismiss: () -> Unit,
) {
    val checked = remember { initialChecked.toMutableStateList() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                items.forEachIndexed { index, item ->
                    ChoiceRow(
                        text = item,
                        modifier = Modifier.toggleable(
                            value = checked[index],
                            role = Role.Checkbox,
                            onValueChange = { checked[index] = it },
                        ),
                    ) {
                        Checkbox(checked = checked[index], onCheckedChange = null)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(checked.toList()) }) { Text(confirmText) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissText) } },
    )
}

/** A dialog with a radio button per item. [onConfirm] gets the selected index. */
@Composable
fun SingleChoiceDialog(
    title: String,
    items: List<String>,
    initialSelection: Int,
    confirmText: String,
    dismissText: String,
    onConfirm: (selected: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember { mutableIntStateOf(initialSelection) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.selectableGroup().verticalScroll(rememberScrollState())) {
                items.forEachIndexed { index, item ->
                    ChoiceRow(
                        text = item,
                        modifier = Modifier.selectable(
                            selected = index == selected,
                            role = Role.RadioButton,
                            onClick = { selected = index },
                        ),
                    ) {
                        RadioButton(selected = index == selected, onClick = null)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(selected) }) { Text(confirmText) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissText) } },
    )
}

@Composable
private fun ChoiceRow(text: String, modifier: Modifier, control: @Composable () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        control()
        Spacer(Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview
@Composable
private fun MultiChoiceDialogPreview() = RatesTheme {
    MultiChoiceDialog(
        title = "Select sources",
        items = listOf("Altın.in", "Bigpara", "Yapı Kredi", "Bloomberg HT"),
        initialChecked = listOf(true, false, false, true),
        confirmText = "Apply",
        dismissText = "Dismiss",
        onConfirm = {},
        onDismiss = {},
    )
}

@Preview
@Composable
private fun SingleChoiceDialogPreview() = RatesTheme {
    SingleChoiceDialog(
        title = "Select time interval",
        items = listOf("3 sec", "5 sec", "10 sec", "1 min"),
        initialSelection = 2,
        confirmText = "Apply",
        dismissText = "Dismiss",
        onConfirm = {},
        onDismiss = {},
    )
}
