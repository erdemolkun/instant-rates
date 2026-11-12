package dynoapps.exchange_rates

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import dynoapps.exchange_rates.alarm.AlarmManager
import dynoapps.exchange_rates.data.RatesHolder
import dynoapps.exchange_rates.time.TimeIntervalManager
import dynoapps.exchange_rates.ui.compose.RatesTheme
import dynoapps.exchange_rates.util.RateUtils

class LandingActivity : BaseServiceActivity() {

    private val viewModel: LandingViewModel by viewModels()

    // Alarm notifications are simply not shown when the permission is denied.
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RatesTheme {
                LandingScreen(
                    rows = viewModel.rows,
                    intervalSelection = viewModel.intervalSelection,
                    onCardClick = ::openChart,
                    onCardLongClick = ::offerAlarm,
                    onMenuClick = ::showNavigationSheet,
                    onIntervalClick = { TimeIntervalManager.selectInterval(this) },
                    onSourcesClick = { SourcesManager.selectSources(this) },
                    onAddAlarmClick = { AlarmManager.addAlarmDialog(this) },
                )
            }
        }
        requestNotificationPermissionIfNeeded()
    }

    private fun openChart(rateType: Int) {
        startActivity(Intent(this, ChartActivity::class.java).putExtra(ChartActivity.EXTRA_RATE_TYPE, rateType))
    }

    /** Opens the add-alarm dialog prefilled with the card's source, rate and current value. */
    private fun offerAlarm(rateType: Int, card: RateCardState) {
        val rate = RateUtils.getRate(RatesHolder.getLatestEvent(card.sourceType)?.rates, rateType)
        AlarmManager.addAlarmDialog(
            activity = this,
            sourceType = card.sourceType,
            rateType = rate?.rateType ?: rateType,
            valueType = card.valueType,
            defaultValue = rate?.getValue(card.valueType),
        )
    }

    /** Alarm and background polling notifications need the runtime permission on Android 13+. */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
