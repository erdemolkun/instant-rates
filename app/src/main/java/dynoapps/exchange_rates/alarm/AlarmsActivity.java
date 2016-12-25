package dynoapps.exchange_rates.alarm;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import dynoapps.exchange_rates.BaseActivity;
import dynoapps.exchange_rates.R;
import dynoapps.exchange_rates.event.AlarmUpdateEvent;
import dynoapps.exchange_rates.util.AnimationHelper;
import dynoapps.exchange_rates.util.CollectionUtils;

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

    private SwitchCompat swAlarmState;

    AlarmsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setAnimationType(AnimationHelper.FADE_IN);
        super.onCreate(savedInstanceState);

        setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ArrayList<Alarm> alarms = AlarmManager.getAlarmsHolder().alarms;
        tvNoAlarm.setVisibility(CollectionUtils.size(alarms) <= 0 ? View.VISIBLE : View.GONE);
        rvAlarms.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AlarmsAdapter(alarms);
        rvAlarms.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                tvNoAlarm.setVisibility(adapter.getItemCount() <= 0 ? View.VISIBLE : View.GONE);
                super.onChanged();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                tvNoAlarm.setVisibility(adapter.getItemCount() <= 0 ? View.VISIBLE : View.GONE);
                super.onItemRangeRemoved(positionStart, itemCount);
            }
        });

        fabAddAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlarmManager.addAlarmDialog(AlarmsActivity.this);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    @Subscribe
    public void onEvent(AlarmUpdateEvent event) {
        if (!event.is_update && event.is_added) {
            adapter.addData(event.alarm);
            if (!AlarmManager.getAlarmsHolder().is_enabled) {
                AlarmManager.getAlarmsHolder().is_enabled = true;
                swAlarmState.setChecked(true);
            }
        }
        else{
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_alarms;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_alarms, menu);
        swAlarmState = (SwitchCompat) findViewById(R.id.menu_switch);
        swAlarmState.setChecked(AlarmManager.getAlarmsHolder().is_enabled);
        swAlarmState.jumpDrawablesToCurrentState();
        swAlarmState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                AlarmManager.getAlarmsHolder().is_enabled = b;
                AlarmManager.persistAlarms();
                updateViews(b);
            }
        });
        updateViews(swAlarmState.isChecked());
        return true;
    }

    private void updateViews(boolean is_enabled_alarm) {
        rvAlarms.setAlpha(is_enabled_alarm ? 1.0f : 0.5f);
    }


    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_alarm_type)
        ImageView ivType;
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

        public AlarmViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
