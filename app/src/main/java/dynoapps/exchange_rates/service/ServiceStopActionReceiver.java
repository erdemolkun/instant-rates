package dynoapps.exchange_rates.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import dynoapps.exchange_rates.util.L;
import io.reactivex.subjects.PublishSubject;

public class ServiceStopActionReceiver extends BroadcastReceiver {
    private static final PublishSubject<Boolean> stopSubject = PublishSubject.create();

    public static PublishSubject<Boolean> getStopSubject() {
        return stopSubject;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        stopSubject.onNext(true);
        try {
            context.stopService(new Intent(context, RatePollingService.class));
        } catch (Exception e) {
            L.ex(e, "ServiceStopActionReceiver");
        }
    }
}
