package dynoapps.exchange_rates.alarm

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputLayout
import dynoapps.exchange_rates.App
import dynoapps.exchange_rates.R
import dynoapps.exchange_rates.SourcesManager
import dynoapps.exchange_rates.data.CurrencySource
import dynoapps.exchange_rates.interfaces.ValueType
import dynoapps.exchange_rates.ui.SimpleSpinnerAdapter
import dynoapps.exchange_rates.util.DecimalDigitsInputFilter
import dynoapps.exchange_rates.util.InputFilterMinMax
import dynoapps.exchange_rates.util.RateUtils

/** The "add alarm" dialog. */
object AlarmManager {

    /**
     * Shows the dialog, preselecting [sourceType], [rateType] and [defaultValue] when given.
     * [onSaved] is called with the alarm once it is stored.
     */
    fun addAlarmDialog(
        context: Context,
        sourceType: Int = -1,
        rateType: Int = -1,
        valueType: Int = ValueType.NONE,
        defaultValue: Float? = null,
        onSaved: ((Alarm) -> Unit)? = null,
    ) {
        @SuppressLint("InflateParams")
        val view = LayoutInflater.from(context).inflate(R.layout.layout_alarm_selection, null)
        val etAlarm: EditText = view.findViewById(R.id.et_alarm_value)
        val tilValue: TextInputLayout = view.findViewById(R.id.til_alarm_value)
        if (defaultValue != null) {
            etAlarm.setText(RateUtils.formatValue(defaultValue, rateType))
            etAlarm.setSelection(etAlarm.text.length)
        }
        etAlarm.filters = arrayOf(DecimalDigitsInputFilter(4), InputFilterMinMax(1.0, 10000.0))
        etAlarm.doOnTextChanged { _, _, _, _ -> tilValue.error = null }

        val rgAlarm: RadioGroup = view.findViewById(R.id.rg_alarm)
        rgAlarm.check(R.id.rb_above)

        val spnRateTypes: Spinner = view.findViewById(R.id.spn_rate_types)
        val rateTypesView: View = view.findViewById(R.id.v_alarm_types)
        val spnSources: Spinner = view.findViewById(R.id.spn_source_types)

        val sources = SourcesManager.getCurrencySources().filter { it.isEnabled }
        spnSources.adapter = SimpleSpinnerAdapter(context, sources)
        spnSources.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, selected: View?, position: Int, id: Long) {
                val source = parent.selectedItem as? CurrencySource
                val supported = source?.supportedRates ?: intArrayOf()
                val pairs = supported
                    .map { RateValuePair(it, RateUtils.rateName(it)) }
                    .filter { it.name.isNotEmpty() }
                if (pairs.isNotEmpty()) {
                    spnRateTypes.adapter = SimpleSpinnerAdapter(context, pairs)
                    rateTypesView.visibility = View.VISIBLE
                } else {
                    rateTypesView.visibility = View.GONE
                }
                spnRateTypes.setSelection(maxOf(0, pairs.indexOfFirst { it.rateType == rateType }))
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        spnSources.setSelection(maxOf(0, sources.indexOfFirst { it.type == sourceType }))

        val icon = ContextCompat.getDrawable(context, R.drawable.ic_store_icon_24dp)?.mutate()?.also {
            DrawableCompat.setTint(it, ContextCompat.getColor(context, R.color.colorAccent))
        }
        val dialog = AlertDialog.Builder(context, R.style.AppTheme_Alert)
            .setIcon(icon)
            .setTitle(R.string.add_alarm)
            .setView(view)
            .setNegativeButton(R.string.dismiss, null)
            .setPositiveButton(R.string.add, null)
            .create()
        dialog.setOnShowListener {
            // Handle the click ourselves so the dialog stays open when the value is invalid.
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val value = RateUtils.toFloat(etAlarm.text.toString())
                val source = spnSources.selectedItem as? CurrencySource
                val pair = spnRateTypes.selectedItem as? RateValuePair
                if (value == null || source == null || pair == null) {
                    tilValue.error = context.getString(R.string.check_value)
                    Toast.makeText(context, R.string.check_value, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val alarm = Alarm().apply {
                    `val` = value
                    isAbove = rgAlarm.checkedRadioButtonId == R.id.rb_above
                    this.sourceType = source.type
                    this.rateType = pair.rateType
                    value_type = valueType
                }
                App.getInstance().provideAlarmsRepository().saveAlarm(alarm, onSaved)
                dialog.dismiss()
            }
        }
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    /** Spinner item for a rate type. */
    private class RateValuePair(val rateType: Int, val name: String) {
        override fun toString(): String = name
    }
}
