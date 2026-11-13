package dynoapps.exchange_rates

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dynoapps.exchange_rates.ui.compose.BarAction
import dynoapps.exchange_rates.ui.compose.RatesTheme
import dynoapps.exchange_rates.ui.compose.RatesTopAppBar

/** Test tags for [ChartScreen]. */
object ChartScreenTags {
    const val CHART = "rate_chart"
}

/**
 * The live chart of one rate type, under its title. Pulling down asks every source for new rates.
 * [chart] draws the chart in the given modifier's bounds.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    title: String,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onIntervalClick: () -> Unit,
    onSourcesClick: () -> Unit,
    chart: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        topBar = {
            RatesTopAppBar(title = stringResource(R.string.graphic), onBack = onBack) {
                BarAction(R.drawable.ic_update_white_24dp, stringResource(R.string.time_interval), onIntervalClick)
                BarAction(R.drawable.ic_settings_white_24dp, stringResource(R.string.sources), onSourcesClick)
            }
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
                chart(
                    Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(R.dimen.chart_height))
                        .padding(4.dp)
                        .testTag(ChartScreenTags.CHART),
                )
            }
        }
    }
}

@Preview
@Composable
private fun ChartScreenPreview() = RatesTheme {
    ChartScreen(
        title = "Dollar / TL Graph",
        isRefreshing = false,
        onRefresh = {},
        onBack = {},
        onIntervalClick = {},
        onSourcesClick = {},
        chart = { modifier ->
            Box(modifier, contentAlignment = Alignment.Center) { Text("Chart") }
        },
    )
}
