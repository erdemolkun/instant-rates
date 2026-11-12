package dynoapps.exchange_rates

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dynoapps.exchange_rates.interfaces.ValueType
import dynoapps.exchange_rates.model.rates.IRate
import dynoapps.exchange_rates.ui.compose.BarAction
import dynoapps.exchange_rates.ui.compose.RatesTheme

/** Test tags for [LandingScreen]. */
object LandingScreenTags {
    const val NO_PROVIDER_SETTINGS = "no_provider_settings"
    fun row(rateType: Int) = "rate_row_$rateType"
    fun noProvider(rateType: Int) = "no_provider_$rateType"
    fun card(rateType: Int, sourceType: Int, valueType: Int) = "rate_card_${rateType}_${sourceType}_$valueType"
}

private const val SETTINGS_ICON_ID = "settings"
private const val PLACEHOLDER = "%1\$s"

/** The main screen: the polling interval and a row of rate cards per rate type. */
@Composable
fun LandingScreen(
    rows: List<RateRowState>,
    intervalSelection: String,
    onCardClick: (rateType: Int) -> Unit,
    onCardLongClick: (rateType: Int, card: RateCardState) -> Unit,
    onMenuClick: () -> Unit,
    onIntervalClick: () -> Unit,
    onSourcesClick: () -> Unit,
    onAddAlarmClick: () -> Unit,
) {
    val colors = RatesTheme.colors
    Scaffold(
        bottomBar = {
            BottomAppBar(containerColor = colors.bar, contentColor = colors.onBar) {
                BarAction(R.drawable.ic_hamburger, stringResource(R.string.menu), onMenuClick)
                Spacer(Modifier.weight(1f))
                BarAction(R.drawable.ic_update_white_24dp, stringResource(R.string.time_interval), onIntervalClick)
                BarAction(R.drawable.ic_settings_white_24dp, stringResource(R.string.sources), onSourcesClick)
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAlarmClick, containerColor = colors.bar, contentColor = colors.onBar) {
                Icon(painterResource(R.drawable.ic_add_alarm_white_24dp), stringResource(R.string.add_alarm))
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = stringResource(R.string.interval_hint, intervalSelection),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 12.dp),
            )
            rows.forEach { row ->
                RateRow(
                    row = row,
                    onCardClick = { onCardClick(row.rateType) },
                    onCardLongClick = { card -> onCardLongClick(row.rateType, card) },
                    onSettingsClick = onSourcesClick,
                )
            }
        }
    }
}

/**
 * The rate type's icon followed by its cards, which scroll sideways, or by a message when no
 * enabled source provides the rate.
 */
@Composable
fun RateRow(
    row: RateRowState,
    onCardClick: () -> Unit,
    onCardLongClick: (RateCardState) -> Unit,
    onSettingsClick: () -> Unit,
) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
        .testTag(LandingScreenTags.row(row.rateType))
    if (row.cards.isEmpty()) {
        Row(modifier = rowModifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            RateIcon(row.icon, row.insetIcon)
            NoProviderLabel(
                onSettingsClick = onSettingsClick,
                modifier = Modifier
                    .weight(1f)
                    .testTag(LandingScreenTags.noProvider(row.rateType)),
            )
        }
    } else {
        Row(
            modifier = rowModifier
                .horizontalScroll(rememberScrollState())
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RateIcon(row.icon, row.insetIcon)
            row.cards.forEach { card ->
                RateCard(
                    card = card,
                    onClick = onCardClick,
                    onLongClick = { onCardLongClick(card) },
                    modifier = Modifier.testTag(LandingScreenTags.card(row.rateType, card.sourceType, card.valueType)),
                )
            }
        }
    }
}

@Composable
private fun RateIcon(@DrawableRes icon: Int, inset: Boolean) {
    Icon(
        painter = painterResource(icon),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.secondary,
        modifier = Modifier
            .padding(6.dp)
            .size(32.dp)
            .padding(if (inset) 4.dp else 0.dp),
    )
}

/**
 * A source's value for a rate type. Tapping opens the chart, a long press offers an alarm. The value
 * fades out and in when it changes.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RateCard(card: RateCardState, onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = RatesTheme.colors
    val valueAlpha = remember { Animatable(1f) }
    var shownValue by remember { mutableStateOf(card.value) }
    LaunchedEffect(card.value) {
        // Only animate changes, not the first value.
        if (card.value == shownValue) return@LaunchedEffect
        shownValue = card.value
        valueAlpha.animateTo(0.3f, tween(450, easing = FastOutSlowInEasing))
        valueAlpha.animateTo(1f, tween(450, easing = LinearOutSlowInEasing))
    }
    Card(
        modifier = modifier.padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .defaultMinSize(minWidth = 100.dp, minHeight = 72.dp)
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(card.title, color = colors.cardTitle, fontSize = 15.sp, modifier = Modifier.padding(4.dp))
            Text(
                text = card.value,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer { alpha = valueAlpha.value },
            )
        }
    }
}

/**
 * "No supported provider found, click ⚙ to change", with the bottom bar's settings icon in the
 * text. Clicking the icon opens the source selection. Screen readers hear the word "Settings", and
 * activating the label opens the source selection too.
 */
@Composable
fun NoProviderLabel(onSettingsClick: () -> Unit, modifier: Modifier = Modifier) {
    val template = stringResource(R.string.no_provider_found)
    val settingsWord = stringResource(R.string.no_provider_found_settings)
    val selectSources = stringResource(R.string.select_sources)
    val color = MaterialTheme.colorScheme.secondary
    val text = buildAnnotatedString {
        val parts = template.split(PLACEHOLDER, limit = 2)
        append(parts[0])
        if (parts.size == 2) {
            appendInlineContent(SETTINGS_ICON_ID, settingsWord)
            append(parts[1])
        }
    }
    val inlineContent = mapOf(
        SETTINGS_ICON_ID to InlineTextContent(Placeholder(1.4.em, 1.4.em, PlaceholderVerticalAlign.TextCenter)) {
            Icon(
                painter = painterResource(R.drawable.ic_settings_white_24dp),
                contentDescription = null,
                tint = color,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onSettingsClick)
                    .testTag(LandingScreenTags.NO_PROVIDER_SETTINGS),
            )
        },
    )
    Text(
        text = text,
        inlineContent = inlineContent,
        color = color,
        fontSize = 15.sp,
        modifier = modifier
            .heightIn(min = 72.dp)
            .wrapContentHeight(Alignment.CenterVertically)
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .semantics {
                contentDescription = template.replaceFirst(PLACEHOLDER, settingsWord)
                onClick(label = selectSources) {
                    onSettingsClick()
                    true
                }
            },
    )
}

private val previewRows = listOf(
    RateRowState(
        IRate.USD, R.drawable.ic_dollar, insetIcon = true,
        cards = listOf(
            RateCardState(1, ValueType.AVG, "Altın.in", "50.2553 TL"),
            RateCardState(3, ValueType.BUY, "Bigpara Buy", "50.2494 TL"),
            RateCardState(3, ValueType.SELL, "Bigpara Sell", ""),
        ),
    ),
    RateRowState(IRate.EUR, R.drawable.ic_euro, insetIcon = true, cards = listOf(RateCardState(1, ValueType.AVG, "Altın.in", "55.7324 TL"))),
    RateRowState(IRate.ONS_TRY, R.drawable.ic_gold, insetIcon = false, cards = emptyList()),
    RateRowState(IRate.EUR_USD, R.drawable.ic_exchange_eur_usd, insetIcon = false, cards = listOf(RateCardState(1, ValueType.AVG, "Altın.in", "1.1093"))),
    RateRowState(IRate.ONS, R.drawable.ic_gold, insetIcon = false, cards = listOf(RateCardState(1, ValueType.AVG, "Altın.in", "3,947.89 $"))),
)

@Preview
@Composable
private fun LandingScreenPreview() = RatesTheme {
    LandingScreen(previewRows, "10 secs", {}, { _, _ -> }, {}, {}, {}, {})
}

@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LandingScreenDarkPreview() = RatesTheme {
    LandingScreen(previewRows, "10 secs", {}, { _, _ -> }, {}, {}, {}, {})
}
