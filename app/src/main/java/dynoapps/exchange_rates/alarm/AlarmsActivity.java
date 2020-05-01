package dynoapps.exchange_rates.alarm;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import dynoapps.exchange_rates.App;
import dynoapps.exchange_rates.BaseActivity;
import dynoapps.exchange_rates.R;
import dynoapps.exchange_rates.ui.SlideInItemAnimator;
import dynoapps.exchange_rates.util.ViewUtils;

/**
 * Created by erdemmac on 13/12/2016.
 */

public class AlarmsActivity extends BaseActivity {

    @BindView(R.id.rv_alarms)
    RecyclerView rvAlarms;

    @BindView(R.id.tv_no_alarm)
    TextView tvNoAlarm;

    @BindView(R.id.fab_add_alarm)
    FloatingActionButton fabAddAlarm;

    SwitchCompat swAlarmState;

    AlarmsAdapter adapter;

    AlarmsRepository alarmRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmRepository = App.getInstance().provideAlarmsRepository();
        setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        getActionBarToolbar().setNavigationOnClickListener(v -> finish());
        getActionBarToolbar().setTitle(R.string.alarms);

        rvAlarms.setLayoutManager(new LinearLayoutManager(this));
        rvAlarms.setItemAnimator(new SlideInItemAnimator());
        adapter = new AlarmsAdapter(alarmRepository, new ArrayList<>());
        rvAlarms.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                ViewUtils.visibility(tvNoAlarm, adapter.getItemCount() <= 0);
                super.onChanged();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                ViewUtils.visibility(tvNoAlarm, adapter.getItemCount() <= 0);
                super.onItemRangeRemoved(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                ViewUtils.visibility(tvNoAlarm, adapter.getItemCount() <= 0);
                super.onItemRangeInserted(positionStart, itemCount);
            }
        });

        fabAddAlarm.setOnClickListener(view -> AlarmManager.addAlarmDialog(AlarmsActivity.this, alarm -> {
            adapter.addData(alarm);
            // TODO refresh
        }));
        alarmRepository.refreshAlarms();
        alarmRepository.getAlarms(alarms -> {
            Collections.sort(alarms, Alarm.COMPARATOR);
            adapter.addData(alarms);
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_alarms;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_alarms, menu);
        swAlarmState = findViewById(R.id.menu_switch);
        swAlarmState.setChecked(alarmRepository.isEnabled());
        swAlarmState.jumpDrawablesToCurrentState();
        swAlarmState.setOnCheckedChangeListener((compoundButton, b) -> {
            alarmRepository.updateEnabled(b);
            updateViews();
        });
        updateViews();
        return true;
    }

    private void updateViews() {
        rvAlarms.setAlpha(alarmRepository.isEnabled() ? 1.0f : 0.4f);
    }


    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_alarm_type)
        ImageView ivType;

        @BindView(R.id.tv_alarm_type_hint)
        TextView tvTypeHint;

        @BindView(R.id.tv_alarm_val)
        TextView tvValue;

        @BindView(R.id.iv_alarm_rate_type)
        ImageView ivRateType;

        @BindView(R.id.tv_alarm_source)
        TextView tvSource;

        @BindView(R.id.v_alarm_close)
        View vClose;

        @BindView(R.id.sw_alarm)
        SwitchCompat swAlarm;

        AlarmViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
