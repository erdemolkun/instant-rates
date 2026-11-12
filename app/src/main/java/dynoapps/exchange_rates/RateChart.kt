package dynoapps.exchange_rates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.compose.LineChart
import com.github.mikephil.charting.compose.rememberChartState
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import dynoapps.exchange_rates.ui.compose.RatesTheme
import dynoapps.exchange_rates.util.RateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * The live line chart of [ChartViewModel]'s rate type. When an entry is added, the chart refreshes
 * and scrolls to keep the newest values in view; tapping a point shows its value.
 */
@Composable
fun RateChart(viewModel: ChartViewModel, contentDescription: String, modifier: Modifier = Modifier) {
    val rateType = viewModel.rateType
    val background = RatesTheme.colors.card.toArgb()
    val textColor = Color.Black.toArgb()
    val handled = remember { HandledVersion() }
    LineChart(
        data = viewModel.lineData,
        modifier = modifier,
        state = rememberChartState(),
        contentDescription = contentDescription,
        marker = { entry, _ -> ValueMarker(RateUtils.valueToUI(entry.y, rateType)) },
        setup = {
            val hourFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            description.isEnabled = false
            xAxis.isDrawGridLinesEnabled = false
            axisRight.isDrawGridLinesEnabled = true
            xAxis.labelCount = 4
            axisLeft.isEnabled = false
            xAxis.valueFormatter = IAxisValueFormatter { value, _ ->
                val offset = value.toInt()
                if (offset <= 0) {
                    ""
                } else {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.MILLISECOND, offset)
                    hourFormatter.format(calendar.time)
                }
            }
            axisRight.valueFormatter = IAxisValueFormatter { value, _ -> RateUtils.valueToUI(value, rateType) }
            isScaleEnabled = false
            isPinchZoomEnabled = false
            extraBottomOffset = 12f
            extraTopOffset = 12f
            isHighlightPerTapEnabled = true
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            legend.apply {
                textSize = 13f
                yOffset = 6f
                form = Legend.LegendForm.CIRCLE
                isWordWrapEnabled = true
                xEntrySpace = 10f
            }
        },
        update = {
            setBackgroundColor(background)
            legend.textColor = textColor
            xAxis.textColor = textColor
            axisRight.textColor = textColor

            // Reading the version reruns this block for every new entry. Other reruns, such as a
            // theme change, leave the viewport where the user put it.
            val version = viewModel.version
            val newX = viewModel.latestX
            if (version != handled.version && newX != null) {
                handled.version = version
                notifyDataSetChanged()
                setVisibleXRangeMaximum(viewModel.visibleTimeMillis.toFloat())
                when {
                    xAxis.axisMaximum <= newX -> moveViewToX(newX)
                    visibleXRange < newX -> moveViewToX(newX + visibleXRange)
                }
            }
        },
    )
}

/** The newest chart version [RateChart] has scrolled for. */
private class HandledVersion {
    var version = -1
}

/** The tapped value, in a rounded box above the point that grows with the text. */
@Composable
private fun ValueMarker(text: String) {
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 64.dp, minHeight = 36.dp)
            .background(colorResource(R.color.colorPrimary), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Preview
@Composable
private fun ValueMarkerPreview() = RatesTheme {
    ValueMarker("50.2581 TL")
}
