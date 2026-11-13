package dynoapps.exchange_rates.alarm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dynoapps.exchange_rates.R
import dynoapps.exchange_rates.ui.compose.RatesTheme
import dynoapps.exchange_rates.ui.compose.RatesTopAppBar

private val AboveColor = Color(0xFF99CC00)
private val BelowColor = Color(0xFFFF4444)

/** Test tags for [AlarmsScreen]. */
object AlarmsScreenTags {
    const val MASTER_SWITCH = "alarms_master_switch"
    const val LIST = "alarms_list"
    const val EMPTY = "alarms_empty"
    fun item(id: Long) = "alarm_$id"
    fun itemSwitch(id: Long) = "alarm_switch_$id"
    fun itemDelete(id: Long) = "alarm_delete_$id"
}

/**
 * The alarms list with a master switch in the top bar. When alarms are off, the list is dimmed;
 * individual alarms can still be edited.
 */
@Composable
fun AlarmsScreen(
    items: List<AlarmItemState>,
    alarmsEnabled: Boolean,
    onAlarmsEnabledChange: (Boolean) -> Unit,
    onAlarmEnabledChange: (id: Long, enabled: Boolean) -> Unit,
    onDelete: (id: Long) -> Unit,
    onAdd: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            RatesTopAppBar(title = stringResource(R.string.alarms), onBack = onBack) {
                val label = stringResource(R.string.alarms)
                Switch(
                    checked = alarmsEnabled,
                    onCheckedChange = onAlarmsEnabledChange,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .testTag(AlarmsScreenTags.MASTER_SWITCH)
                        .semantics { contentDescription = label },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.3f),
                    ),
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(
                    painterResource(R.drawable.ic_add_alarm_white_24dp),
                    contentDescription = stringResource(R.string.add_alarm),
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
        },
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.have_no_alarms),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier
                        .padding(24.dp)
                        .testTag(AlarmsScreenTags.EMPTY),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (alarmsEnabled) 1f else 0.4f)
                    .testTag(AlarmsScreenTags.LIST),
                // Room for the FAB below the last alarm.
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 4.dp,
                    bottom = padding.calculateBottomPadding() + 88.dp,
                ),
            ) {
                items(items, key = { it.id }) { item ->
                    AlarmItem(
                        item = item,
                        onEnabledChange = { onAlarmEnabledChange(item.id, it) },
                        onDelete = { onDelete(item.id) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

/** One alarm: whether it's on, the rate and value it watches, its source and its direction. */
@Composable
fun AlarmItem(
    item: AlarmItemState,
    onEnabledChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val secondary = MaterialTheme.colorScheme.secondary
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .testTag(AlarmsScreenTags.item(item.id)),
        colors = CardDefaults.cardColors(containerColor = RatesTheme.colors.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp)) {
            Switch(
                checked = item.isEnabled,
                onCheckedChange = onEnabledChange,
                modifier = Modifier.testTag(AlarmsScreenTags.itemSwitch(item.id)),
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.rateIcon != null) {
                        Icon(
                            painterResource(item.rateIcon),
                            contentDescription = null,
                            tint = secondary,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(item.value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = item.sourceName ?: "-",
                        color = item.sourceColor?.let(::Color) ?: secondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // The back arrow, turned to point up for "above" and down for "below".
                    Icon(
                        painterResource(R.drawable.ic_arrow_back_black_24dp),
                        contentDescription = null,
                        tint = if (item.isAbove) AboveColor else BelowColor,
                        modifier = Modifier.rotate(if (item.isAbove) 90f else 270f),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(if (item.isAbove) R.string.if_above else R.string.if_below),
                        color = secondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.testTag(AlarmsScreenTags.itemDelete(item.id))) {
                Icon(painterResource(R.drawable.ic_close_black_20dp), contentDescription = stringResource(R.string.remove))
            }
        }
    }
}

private val previewItems = listOf(
    AlarmItemState(1, isAbove = true, value = "50.30 TL", rateIcon = R.drawable.ic_dollar, sourceName = "Altın.in", sourceColor = 0xFFF1C15A.toInt(), isEnabled = true),
    AlarmItemState(2, isAbove = false, value = "55.10 TL", rateIcon = R.drawable.ic_euro, sourceName = "Bloomberg HT", sourceColor = 0xFF39466A.toInt(), isEnabled = false),
)

@Preview
@Composable
private fun AlarmsScreenPreview() = RatesTheme {
    AlarmsScreen(previewItems, alarmsEnabled = true, onAlarmsEnabledChange = {}, onAlarmEnabledChange = { _, _ -> }, onDelete = {}, onAdd = {}, onBack = {})
}

@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AlarmsScreenDisabledDarkPreview() = RatesTheme {
    AlarmsScreen(previewItems, alarmsEnabled = false, onAlarmsEnabledChange = {}, onAlarmEnabledChange = { _, _ -> }, onDelete = {}, onAdd = {}, onBack = {})
}

@Preview
@Composable
private fun AlarmsScreenEmptyPreview() = RatesTheme {
    AlarmsScreen(emptyList(), alarmsEnabled = true, onAlarmsEnabledChange = {}, onAlarmEnabledChange = { _, _ -> }, onDelete = {}, onAdd = {}, onBack = {})
}
