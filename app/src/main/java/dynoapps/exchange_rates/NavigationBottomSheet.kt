package dynoapps.exchange_rates

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dynoapps.exchange_rates.alarm.AlarmsActivity
import dynoapps.exchange_rates.model.rates.IRate
import dynoapps.exchange_rates.ui.compose.RatesTheme
import dynoapps.exchange_rates.ui.compose.showComposeDialog
import dynoapps.exchange_rates.util.AppUtils
import kotlinx.coroutines.launch

private const val TAG_NAVIGATION_SHEET = "navigation_sheet"

/** Shows the navigation bottom sheet, unless it is already showing. */
fun ComponentActivity.showNavigationSheet() {
    val version = AppUtils.getPlainVersion()
    showComposeDialog(tag = TAG_NAVIGATION_SHEET) { dismiss ->
        NavigationBottomSheet(
            version = version,
            onChartSelected = { rateType ->
                startActivity(Intent(this, ChartActivity::class.java).putExtra(ChartActivity.EXTRA_RATE_TYPE, rateType))
            },
            onAlarmsSelected = { startActivity(Intent(this, AlarmsActivity::class.java)) },
            onDismiss = dismiss,
        )
    }
}

/** The bottom sheet with the chart screens and other destinations, opened fully expanded. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigationBottomSheet(
    version: String,
    onChartSelected: (rateType: Int) -> Unit,
    onAlarmsSelected: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    // Navigate right away and close the sheet with its hide animation.
    val navigate: (() -> Unit) -> Unit = { action ->
        action()
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        NavigationSheetContent(
            version = version,
            onChartSelected = { rateType -> navigate { onChartSelected(rateType) } },
            onAlarmsSelected = { navigate(onAlarmsSelected) },
        )
    }
}

@Composable
private fun NavigationSheetContent(
    version: String,
    onChartSelected: (rateType: Int) -> Unit,
    onAlarmsSelected: () -> Unit,
) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_store_icon_24dp),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(44.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = stringResource(R.string.version_placeholder, version),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
        HorizontalDivider()

        SectionTitle(stringResource(R.string.graph_types))
        NavigationItem(R.drawable.ic_dollar, "USD") { onChartSelected(IRate.USD) }
        NavigationItem(R.drawable.ic_euro, "EURO") { onChartSelected(IRate.EUR) }
        NavigationItem(R.drawable.ic_exchange_eur_usd, "EURO / USD") { onChartSelected(IRate.EUR_USD) }
        NavigationItem(R.drawable.ic_gold, "ONS") { onChartSelected(IRate.ONS) }
        HorizontalDivider()

        SectionTitle(stringResource(R.string.others))
        NavigationItem(R.drawable.ic_add_alarm_white_24dp, stringResource(R.string.alarms), onAlarmsSelected)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 6.dp),
    )
}

@Composable
private fun NavigationItem(@DrawableRes icon: Int, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(start = 24.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.width(16.dp))
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
private fun NavigationSheetContentPreview() = RatesTheme {
    NavigationSheetContent(version = "0.4.10", onChartSelected = {}, onAlarmsSelected = {})
}
