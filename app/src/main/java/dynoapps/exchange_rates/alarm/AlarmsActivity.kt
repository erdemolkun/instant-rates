package dynoapps.exchange_rates.alarm

import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dynoapps.exchange_rates.App
import dynoapps.exchange_rates.BaseActivity
import dynoapps.exchange_rates.R
import dynoapps.exchange_rates.ui.SlideInItemAnimator

class AlarmsActivity : BaseActivity() {

    private lateinit var rvAlarms: RecyclerView
    private lateinit var tvNoAlarm: TextView
    private lateinit var adapter: AlarmsAdapter
    private lateinit var alarmRepository: AlarmsRepository

    override val layoutId: Int = R.layout.activity_alarms

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rvAlarms = findViewById(R.id.rv_alarms)
        tvNoAlarm = findViewById(R.id.tv_no_alarm)
        val fabAddAlarm: FloatingActionButton = findViewById(R.id.fab_add_alarm)
        alarmRepository = App.getInstance().provideAlarmsRepository()

        setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        actionBarToolbar?.setNavigationOnClickListener { finish() }
        actionBarToolbar?.setTitle(R.string.alarms)

        adapter = AlarmsAdapter(alarmRepository)
        rvAlarms.layoutManager = LinearLayoutManager(this)
        rvAlarms.itemAnimator = SlideInItemAnimator()
        rvAlarms.adapter = adapter
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() = updateEmptyState()

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = updateEmptyState()

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = updateEmptyState()
        })

        fabAddAlarm.setOnClickListener {
            AlarmManager.addAlarmDialog(this) { alarm -> adapter.addData(alarm) }
        }
        alarmRepository.refreshAlarms()
        alarmRepository.getAlarms { alarms -> adapter.addData(alarms.sortedWith(Alarm.COMPARATOR)) }
    }

    private fun updateEmptyState() {
        tvNoAlarm.isVisible = adapter.itemCount <= 0
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val swAlarmState: SwitchCompat = findViewById(R.id.menu_switch)
        swAlarmState.isChecked = alarmRepository.isEnabled
        swAlarmState.jumpDrawablesToCurrentState()
        swAlarmState.setOnCheckedChangeListener { _, isChecked ->
            alarmRepository.updateEnabled(isChecked)
            updateViews()
        }
        updateViews()
        return true
    }

    private fun updateViews() {
        rvAlarms.alpha = if (alarmRepository.isEnabled) 1.0f else 0.4f
    }
}
