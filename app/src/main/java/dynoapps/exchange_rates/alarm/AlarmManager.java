package dynoapps.exchange_rates.alarm;

import com.google.gson.GsonBuilder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import dynoapps.exchange_rates.Prefs;
import dynoapps.exchange_rates.R;
import dynoapps.exchange_rates.SourcesManager;
import dynoapps.exchange_rates.data.CurrencySource;
import dynoapps.exchange_rates.event.AlarmUpdateEvent;
import dynoapps.exchange_rates.util.CollectionUtils;
import dynoapps.exchange_rates.util.DecimalDigitsInputFilter;
import dynoapps.exchange_rates.util.InputFilterMinMax;
import dynoapps.exchange_rates.util.RateUtils;

/**
 * Created by erdemmac on 13/12/2016.
 */

public class AlarmManager {

    public static final int MAX_ALARM_COUNT = 10;

    private static AlarmsHolder alarmsHolder;

    private static boolean addAlarm(Alarm alarm) {
        alarmsHolder = getAlarmsHolder();
        alarmsHolder.alarms.add(alarm);
        EventBus.getDefault().post(new AlarmUpdateEvent(alarm, true, false));
        persistAlarms();
        return true;
    }

    public static void remove(int index) {
        getAlarmsHolder().alarms.remove(index);
        EventBus.getDefault().post(new AlarmUpdateEvent(null, false, false));
        persistAlarms();
    }

    public static AlarmsHolder getAlarmsHolder() {
        if (alarmsHolder == null) {
            String alarm_json = Prefs.getAlarms();
            if (!TextUtils.isEmpty(alarm_json)) {
                try {
                    alarmsHolder = new GsonBuilder().create().fromJson(alarm_json, AlarmsHolder.class);
                } catch (Exception ignored) {
                }
            }
            if (alarmsHolder == null) {
                alarmsHolder = new AlarmsHolder(new ArrayList<Alarm>());
                alarmsHolder.alarms = new ArrayList<>();
                alarmsHolder.is_enabled = true;
            }

        }
        return alarmsHolder;
    }


    public static void persistAlarms() {
        String alarms_json = new GsonBuilder().create().toJson(alarmsHolder);
        Prefs.saveAlarms(alarms_json);
    }

    /**
     * Used for spinner item models.
     */
    static class RateValuePair {
        public int rate_type;
        public String name;

        @Override
        public String toString() {
            return name;
        }
    }

    public static void addAlarmDialog(final Context context) {
        addAlarmDialog(context, -1, -1, null);
    }

    public static void addAlarmDialog(@NonNull final Context context, int source_type, final int rate_type, Float default_value) {
        if (CollectionUtils.size(getAlarmsHolder().alarms) >= AlarmManager.MAX_ALARM_COUNT) {
            Toast.makeText(context, context.getString(R.string.max_alarm_message, AlarmManager.MAX_ALARM_COUNT), Toast.LENGTH_SHORT).show();
            return;
        }
        @SuppressLint("InflateParams") final View v = LayoutInflater.from(context).inflate(R.layout.layout_alarm_selection, null);
        EditText etAlarm = (EditText) v.findViewById(R.id.et_alarm_value);

        if (default_value != null) {
            String val = RateUtils.formatValue(default_value, rate_type);
            etAlarm.setText(val);
            etAlarm.setSelection(etAlarm.getText().length());
        }
        etAlarm.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(4), new InputFilterMinMax(1, 2000)});
        final RadioGroup rgAlarm = (RadioGroup) v.findViewById(R.id.rg_alarm);
        rgAlarm.check(R.id.rb_above);


        final Spinner spn_rate_types = (Spinner) v.findViewById(R.id.spn_rate_types);
        final View rate_types_view = v.findViewById(R.id.v_alarm_types);
        final Spinner spn_sources = (Spinner) v.findViewById(R.id.spn_source_types);

        ArrayList<CurrencySource> sources = new ArrayList<>();
        int selected_source_index = 0;
        int i = 0;
        for (CurrencySource source : SourcesManager.getCurrencySources()) {
            if (source.isEnabled()) {
                if (source.getSourceType() == source_type) {
                    selected_source_index = i;
                }
                sources.add(source);
                i++;
            }
        }
        ArrayAdapter<CurrencySource> sourceArrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, sources);
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
                            if (rate_type == val) {
                                selected_rate_index = i;
                            }
                        }
                    }
                }
                if (!CollectionUtils.isNullOrEmpty(rateValuePairs)) {
                    spn_rate_types.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, rateValuePairs));
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

        final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.AppTheme_AlertDialog).setIcon(R.drawable.ic_logo_64dp).
                setTitle(R.string.add_alarm)
                .setView(v)
                .setNegativeButton(R.string.dismiss, null)
                .setPositiveButton(R.string.add, null).create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        EditText etValue = (EditText) v.findViewById(R.id.et_alarm_value);
                        String str = etValue.getText().toString();
                        Float val = null;
                        try {
                            val = Float.valueOf(str);
                            Alarm alarm = new Alarm();
                            alarm.val = val;
                            alarm.is_above = rgAlarm.getCheckedRadioButtonId() == R.id.rb_above;
                            alarm.source_type = ((CurrencySource) spn_sources.getSelectedItem()).getSourceType();
                            alarm.rate_type = ((RateValuePair) spn_rate_types.getSelectedItem()).rate_type;
                            AlarmManager.addAlarm(alarm);
                        } catch (Exception ex) {
                            // todo any error is blocked here
                        }
                        if (val == null) {
                            Toast.makeText(context, R.string.check_value, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        alertDialog.dismiss();

                    }
                });
            }
        });
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public static boolean hasAnyActive() {
        if (!getAlarmsHolder().is_enabled) return false;
        if (getAlarmsHolder().alarms.size() <= 0) return false;
        for (Alarm alarm : getAlarmsHolder().alarms) {
            if (alarm.is_enabled) {
                for (CurrencySource source : SourcesManager.getCurrencySources()) {
                    if (source.isEnabled() && source.getSourceType() == alarm.source_type) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
