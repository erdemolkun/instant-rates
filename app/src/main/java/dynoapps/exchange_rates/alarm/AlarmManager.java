package dynoapps.exchange_rates.alarm;

import com.google.gson.GsonBuilder;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import dynoapps.exchange_rates.Prefs;
import dynoapps.exchange_rates.R;
import dynoapps.exchange_rates.event.AlarmUpdateEvent;
import dynoapps.exchange_rates.model.rates.IRate;

/**
 * Created by erdemmac on 13/12/2016.
 */

public class AlarmManager {

    public static final int MAX_ALARM_COUNT = 12;

    public static AlarmsHolder alarmsHolder;

    public static boolean addAlarm(Alarm alarm) {
        alarmsHolder = getAlarmsHolder();
        if (alarmsHolder.alarms == null)
            alarmsHolder.alarms = new ArrayList<>();
        if (alarmsHolder.alarms.size() >= MAX_ALARM_COUNT)
            return false;
        alarmsHolder.alarms.add(alarm);
        EventBus.getDefault().post(new AlarmUpdateEvent());
        saveAlarms();
        return true;
    }

    public static void remove(int index) {
        getAlarmsHolder().alarms.remove(index);
        EventBus.getDefault().post(new AlarmUpdateEvent());
        saveAlarms();
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
            }

        }
        return alarmsHolder;
    }


    public static void saveAlarms() {
        String alarms_json = new GsonBuilder().create().toJson(alarmsHolder);
        Prefs.saveAlarms(alarms_json);
    }

    public static void addAlarm(final Context context) {
        final View v = LayoutInflater.from(context).inflate(R.layout.layout_alarm_selection, null);
        final Spinner spinner = (Spinner) v.findViewById(R.id.spn_above_below);
        ArrayList<String> values = new ArrayList<>();
        values.add(context.getString(R.string.if_below));
        values.add(context.getString(R.string.if_above));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, values);
        spinner.setAdapter(arrayAdapter);
        spinner.setSelection(0);
        final AlertDialog alertDialog = new AlertDialog.Builder(context).setIcon(R.drawable.ic_splash).
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
                        EditText etValue = (EditText) v.findViewById(R.id.et_value);
                        String str = etValue.getText().toString();
                        Float val = null;
                        try {
                            val = Float.valueOf(str);
                            Alarm alarm = new Alarm();
                            alarm.val = val;
                            alarm.rate_type = IRate.USD;
                            alarm.is_above = spinner.getSelectedItemPosition() == 1;
                            AlarmManager.addAlarm(alarm);
                        } catch (Exception ex) {

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

}
