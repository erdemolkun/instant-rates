package dynoapps.exchange_rates.ui.compose

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import dynoapps.exchange_rates.R

/** The app's top bar: [title], a back button and optional [actions], in the bar colour. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatesTopAppBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = RatesTheme.colors
    TopAppBar(
        title = { Text(title, fontSize = 20.sp, fontWeight = FontWeight.Medium) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(painterResource(R.drawable.ic_arrow_back_black_24dp), stringResource(R.string.back))
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.bar,
            titleContentColor = colors.onBar,
            navigationIconContentColor = colors.onBar,
            actionIconContentColor = colors.onBar,
        ),
    )
}

/** An icon button for a top or bottom bar action. */
@Composable
fun BarAction(icon: Int, label: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(painterResource(icon), contentDescription = label)
    }
}

@Preview
@Composable
private fun RatesTopAppBarPreview() = RatesTheme {
    RatesTopAppBar(title = "Graph", onBack = {}) {
        BarAction(R.drawable.ic_settings_white_24dp, "Sources") {}
    }
}
