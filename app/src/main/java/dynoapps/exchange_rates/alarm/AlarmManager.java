package dynoapps.exchange_rates.alarm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
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

import com.google.gson.GsonBuilder;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import dynoapps.exchange_rates.App;
import dynoapps.exchange_rates.AppDatabase;
import dynoapps.exchange_rates.AppExecutors;
import dynoapps.exchange_rates.Prefs;
import dynoapps.exchange_rates.R;
import dynoapps.exchange_rates.SourcesManager;
import dynoapps.exchange_rates.data.CurrencySource;
import dynoapps.exchange_rates.event.AlarmUpdateEvent;
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

    private static final int MAX_ALARM_COUNT = 10;

    private static AlarmsHolder alarmsHolder;

    private static boolean addAlarm(Alarm alarm) {
        alarmsHolder = getAlarmsHolder();
        alarmsHolder.alarms.add(alarm);

        if (!getAlarmsHolder().is_enabled) {
            getAlarmsHolder().is_enabled = true;
        }
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

    private static AppExecutors appExecutors;

    private static void saveToDao(final Context context, final Alarm alarm) {
        if (appExecutors == null) appExecutors = new AppExecutors();
        appExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final long id = AppDatabase.getInstance(App.context()).alarm().insert(alarm);
                // notify on the main thread
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                      Toast.makeText(context,"Save With ID : "+id,Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
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
        addAlarmDialog(context, -1, -1, ValueType.NONE, null);
    }

    public static void addAlarmDialog(@NonNull final Context context, int source_type, final int rate_type, final int value_type, Float default_value) {
        if (CollectionUtils.size(getAlarmsHolder().alarms) >= AlarmManager.MAX_ALARM_COUNT) {
            Toast.makeText(context, context.getString(R.string.max_alarm_message, AlarmManager.MAX_ALARM_COUNT), Toast.LENGTH_SHORT).show();
            return;
        }
        @SuppressLint("InflateParams") final View v = LayoutInflater.from(context).inflate(R.layout.layout_alarm_selection, null);
        final EditText etAlarm = (EditText) v.findViewById(R.id.et_alarm_value);
        final TextInputLayout tilValue = (TextInputLayout) v.findViewById(R.id.til_alarm_value);
        if (default_value != null) {
            String val = RateUtils.formatValue(default_value, rate_type);
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
                if (source.getType() == source_type) {
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
                            if (rate_type == val) {
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

        final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.AppTheme_AlertDialog).setIcon(R.drawable.ic_orco_512).
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
                        String str = etAlarm.getText().toString();
                        Float val = null;
                        try {
                            val = RateUtils.toFloat(str);
                            if (val != null) {
                                Alarm alarm = new Alarm();
                                alarm.val = val;
                                alarm.is_above = rgAlarm.getCheckedRadioButtonId() == R.id.rb_above;
                                alarm.source_type = ((CurrencySource) spn_sources.getSelectedItem()).getType();
                                alarm.rate_type = ((RateValuePair) spn_rate_types.getSelectedItem()).rate_type;
                                alarm.value_type = value_type;
                                AlarmManager.addAlarm(alarm);
                                saveToDao(context,alarm);
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
                    if (source.isEnabled() && source.getType() == alarm.source_type) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
