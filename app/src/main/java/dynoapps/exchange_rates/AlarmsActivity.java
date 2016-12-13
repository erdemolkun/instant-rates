package dynoapps.exchange_rates;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import dynoapps.exchange_rates.alarm.Alarm;
import dynoapps.exchange_rates.alarm.AlarmManager;
import dynoapps.exchange_rates.event.AlarmUpdateEvent;
import dynoapps.exchange_rates.util.AnimationHelper;
import dynoapps.exchange_rates.util.RateUtils;

/**
 * Created by erdemmac on 13/12/2016.
 */

public class AlarmsActivity extends BaseActivity {

    @BindView(R.id.rv_alarms)
    RecyclerView rvAlarms;

    @BindView(R.id.tv_no_alarm)
    TextView tvNoAlarm;

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
        tvNoAlarm.setVisibility(alarms.size() <= 0 ? View.VISIBLE : View.GONE);
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
        adapter.notifyDataSetChanged();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_alarms;
    }

    static class AlarmsAdapter extends RecyclerView.Adapter<AlarmViewHolder> {
        ArrayList<Alarm> alarms;

        public AlarmsAdapter(ArrayList<Alarm> alarms) {
            this.alarms = alarms;
        }

        @Override
        public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm, parent, false);
            return new AlarmViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final AlarmViewHolder holder, int position) {
            Alarm alarm = alarms.get(position);
            holder.ivType.setRotation(alarm.is_above ?  90 : 270);
            holder.ivType.setColorFilter(Color.parseColor(!alarm.is_above ? "#ff0000" : "#00ff00"));
            holder.tvValue.setText(RateUtils.entryToUI(alarm.val,alarm.rate_type));
            holder.vClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = holder.getAdapterPosition();
                    AlarmManager.remove(pos);
                    notifyItemRemoved(pos);
                }
            });
        }

        @Override
        public int getItemCount() {
            return alarms != null ? alarms.size() : 0;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_alarms, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_add_alarm) {
            AlarmManager.addAlarm(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_alarm_type)
        ImageView ivType;
        @BindView(R.id.tv_alarm_val)
        TextView tvValue;

        @BindView(R.id.v_alarm_close)
        View vClose;

        public AlarmViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
