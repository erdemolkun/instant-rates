package dynoapps.exchange_rates.alarm

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import dynoapps.exchange_rates.R
import dynoapps.exchange_rates.SourcesManager
import dynoapps.exchange_rates.util.L
import dynoapps.exchange_rates.util.RateUtils
import io.reactivex.rxjava3.schedulers.Schedulers

internal class AlarmsAdapter(
    private val alarmRepository: AlarmsRepository,
) : RecyclerView.Adapter<AlarmsAdapter.AlarmViewHolder>() {

    private val alarms = ArrayList<Alarm>()

    /** Inserts [alarm] keeping the list sorted by [Alarm.COMPARATOR]. */
    fun addData(alarm: Alarm) {
        val index = alarms.indexOfFirst { Alarm.COMPARATOR.compare(alarm, it) < 0 }
            .let { if (it < 0) alarms.size else it }
        alarms.add(index, alarm)
        notifyItemInserted(index)
    }

    fun addData(newAlarms: List<Alarm>) {
        val insertRangeStart = alarms.size
        alarms.addAll(newAlarms)
        notifyItemRangeInserted(insertRangeStart, newAlarms.size)
    }

    override fun getItemCount(): Int = alarms.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder =
        AlarmViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_alarm, parent, false))

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]
        val context = holder.itemView.context

        holder.ivType.rotation = if (alarm.isAbove) 90f else 270f
        holder.ivType.setColorFilter(
            ContextCompat.getColor(
                context,
                if (alarm.isAbove) android.R.color.holo_green_light else android.R.color.holo_red_light,
            )
        )
        holder.tvTypeHint.setText(if (alarm.isAbove) R.string.if_above else R.string.if_below)
        holder.tvValue.text = RateUtils.valueToUI(alarm.value, alarm.rateType)
        holder.ivRateType.setImageResource(RateUtils.getRateIcon(alarm.rateType))

        val source = SourcesManager.getSource(alarm.sourceType)
        if (source != null) {
            holder.tvSource.text = source.name
            holder.tvSource.setTextColor(source.color)
        } else {
            holder.tvSource.text = "-"
            holder.tvSource.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
        }

        holder.vClose.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            alarms.removeAt(pos)
            notifyItemRemoved(pos)
            alarmRepository.deleteAlarm(alarm).subscribeOn(Schedulers.io())
                .subscribe({ }, { L.ex(it, "Deleting alarm failed") })
        }

        // Clear the listener first so re-binding a recycled view doesn't update the previous alarm.
        holder.swAlarm.setOnCheckedChangeListener(null)
        holder.swAlarm.isChecked = alarm.isEnabled
        holder.swAlarm.setOnCheckedChangeListener { _, isChecked ->
            alarm.isEnabled = isChecked
            alarmRepository.updateAlarm(alarm).subscribeOn(Schedulers.io())
                .subscribe({ }, { L.ex(it, "Updating alarm failed") })
        }
    }

    class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivType: ImageView = itemView.findViewById(R.id.iv_alarm_type)
        val tvTypeHint: TextView = itemView.findViewById(R.id.tv_alarm_type_hint)
        val tvValue: TextView = itemView.findViewById(R.id.tv_alarm_val)
        val ivRateType: ImageView = itemView.findViewById(R.id.iv_alarm_rate_type)
        val tvSource: TextView = itemView.findViewById(R.id.tv_alarm_source)
        val vClose: View = itemView.findViewById(R.id.v_alarm_close)
        val swAlarm: SwitchCompat = itemView.findViewById(R.id.sw_alarm)
    }
}
