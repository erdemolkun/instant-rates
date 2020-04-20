package dynoapps.exchange_rates.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.EventLog;

import org.greenrobot.eventbus.EventBus;

import dynoapps.exchange_rates.util.L;

public class ServiceStopActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        EventBus.getDefault().post(new StopAction());
        try {
            context.stopService(new Intent(context, RatePollingService.class));
        } catch (Exception e) {
            L.ex(e, "ServiceStopActionReceiver");
        }
    }

    public static class StopAction{}
}
