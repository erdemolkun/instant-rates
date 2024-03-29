package dynoapps.exchange_rates.alarm;

import com.google.android.material.textfield.TextInputLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import dynoapps.exchange_rates.App;
import dynoapps.exchange_rates.R;
import dynoapps.exchange_rates.SourcesManager;
import dynoapps.exchange_rates.data.CurrencySource;
import dynoapps.exchange_rates.interfaces.ValueType;
import dynoapps.exchange_rates.ui.SimpleSpinnerAdapter;
import dynoapps.exchange_rates.util.CollectionUtils;
import dynoapps.exchange_rates.util.DecimalDigitsInputFilter;
import dynoapps.exchange_rates.util.InputFilterMinMax;
import dynoapps.exchange_rates.util.L;
import dynoapps.exchange_rates.util.RateUtils;

/**
 * Created by erdemmac on 13/12/2016.
 */

public class AlarmManager {

    public static void addAlarmDialog(final Context context) {
        addAlarmDialog(context, -1, -1, ValueType.NONE, null, null);
    }

    public static void addAlarmDialog(final Context context, final AlarmsDataSource.AlarmUpdateInsertCallback alarmUpdateInsertCallback) {
        addAlarmDialog(context, -1, -1, ValueType.NONE, null, alarmUpdateInsertCallback);
    }

    public static void addAlarmDialog(@NonNull final Context context,
                                      int sourceType, final int rateType, final int valueType, Float defaultValue,
                                      final AlarmsDataSource.AlarmUpdateInsertCallback alarmUpdateInsertCallback) {
        @SuppressLint("InflateParams") final View v = LayoutInflater.from(context).inflate(R.layout.layout_alarm_selection, null);
        final EditText etAlarm = v.findViewById(R.id.et_alarm_value);
        final TextInputLayout tilValue = v.findViewById(R.id.til_alarm_value);
        if (defaultValue != null) {
            String val = RateUtils.formatValue(defaultValue, rateType);
            etAlarm.setText(val);
            etAlarm.setSelection(etAlarm.getText().length());
        }
        etAlarm.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(4), new InputFilterMinMax(1, 10000)});
        etAlarm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                tilValue.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        final RadioGroup rgAlarm = v.findViewById(R.id.rg_alarm);
        rgAlarm.check(R.id.rb_above);

        final Spinner spn_rate_types = v.findViewById(R.id.spn_rate_types);
        final View rate_types_view = v.findViewById(R.id.v_alarm_types);
        final Spinner spn_sources = v.findViewById(R.id.spn_source_types);

        ArrayList<CurrencySource> sources = new ArrayList<>();
        int selected_source_index = 0;
        int i = 0;
        for (CurrencySource source : SourcesManager.getCurrencySources()) {
            if (source.isEnabled()) {
                if (source.getType() == sourceType) {
                    selected_source_index = i;
                }
                sources.add(source);
                i++;
            }
        }
        ArrayAdapter<CurrencySource> sourceArrayAdapter = new SimpleSpinnerAdapter<>(context, sources); //new ArrayAdapter<>(context, R.layout.item_spinner_dropdown, sources);
        spn_sources.setAdapter(sourceArrayAdapter);
        spn_sources.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CurrencySource currencySource = (CurrencySource) parent.getSelectedItem();
                ArrayList<RateValuePair> rateValuePairs = new ArrayList<>();
                int selected_rate_index = -1;
                if (currencySource != null) {
                    int[] supported_ones = currencySource.getSupportedRates();

                    for (int i = 0; i < supported_ones.length; i++) {
                        int val = supported_ones[i];
                        RateValuePair rateValuePair = new RateValuePair();
                        rateValuePair.name = RateUtils.rateName(val);
                        rateValuePair.rate_type = val;
                        if (!TextUtils.isEmpty(rateValuePair.name)) {
                            rateValuePairs.add(rateValuePair);
                            if (rateType == val) {
                                selected_rate_index = i;
                            }
                        }
                    }
                }
                if (!CollectionUtils.isNullOrEmpty(rateValuePairs)) {
                    spn_rate_types.setAdapter(new SimpleSpinnerAdapter<>(context, rateValuePairs));
                    rate_types_view.setVisibility(View.VISIBLE);
                } else {
                    rate_types_view.setVisibility(View.GONE);
                }
                spn_rate_types.setSelection(Math.max(0, selected_rate_index));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spn_sources.setSelection(selected_source_index);

        Drawable drawableIcon = ContextCompat.getDrawable(context,R.drawable.ic_store_icon_24dp);
        if (drawableIcon!=null) {
            DrawableCompat.setTint(drawableIcon, ContextCompat.getColor(context, R.color.colorAccent));
        }
        final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.AppTheme_Alert).setIcon(drawableIcon).
                setTitle(R.string.add_alarm)
                .setView(v)
                .setNegativeButton(R.string.dismiss, null)
                .setPositiveButton(R.string.add, null).create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(view -> {
                String str = etAlarm.getText().toString();
                Float val = null;
                try {
                    val = RateUtils.toFloat(str);
                    if (val != null) {
                        Alarm alarm = new Alarm();
                        alarm.val = val;
                        alarm.isAbove = rgAlarm.getCheckedRadioButtonId() == R.id.rb_above;
                        alarm.sourceType = ((CurrencySource) spn_sources.getSelectedItem()).getType();
                        alarm.rateType = ((RateValuePair) spn_rate_types.getSelectedItem()).rate_type;
                        alarm.value_type = valueType;
                        App.getInstance().provideAlarmsRepository().saveAlarm(alarm, alarmUpdateInsertCallback);
                    }
                } catch (Exception ex) {
                    L.i(AlarmManager.class.getSimpleName(), "Alarm Convert Exception");
                }
                if (val == null) {
                    tilValue.setError(App.context().getString(R.string.check_value));
                    Toast.makeText(context, R.string.check_value, Toast.LENGTH_SHORT).show();
                    return;
                }
                alertDialog.dismiss();

            });
        });
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    /**
     * Used for spinner item models.
     */
    static class RateValuePair {
        public int rate_type;
        public String name;

        @Override
        @NonNull
        public String toString() {
            return name;
        }
    }


}
