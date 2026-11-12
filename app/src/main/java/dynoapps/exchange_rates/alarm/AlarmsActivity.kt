package dynoapps.exchange_rates.alarm

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dynoapps.exchange_rates.App
import dynoapps.exchange_rates.BaseActivity
import dynoapps.exchange_rates.ui.compose.setRatesContent

class AlarmsActivity : BaseActivity() {

    private val viewModel: AlarmsViewModel by viewModels {
        viewModelFactory { initializer { AlarmsViewModel(App.getInstance().provideAlarmsRepository()) } }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRatesContent {
            AlarmsScreen(
                items = viewModel.items,
                alarmsEnabled = viewModel.alarmsEnabled,
                onAlarmsEnabledChange = viewModel::updateAlarmsEnabled,
                onAlarmEnabledChange = viewModel::setAlarmEnabled,
                onDelete = viewModel::deleteAlarm,
                onAdd = { AlarmManager.addAlarmDialog(this) { alarm -> viewModel.onAlarmAdded(alarm) } },
                onBack = ::finish,
            )
        }
    }
}
