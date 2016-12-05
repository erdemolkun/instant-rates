package dynoapps.exchange_rates.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import dynoapps.exchange_rates.DataSourcesManager;
import dynoapps.exchange_rates.data.RateDataSource;
import dynoapps.exchange_rates.model.BuySellRate;
import dynoapps.exchange_rates.model.DolarTlKurRate;
import dynoapps.exchange_rates.model.YapıKrediRate;
import dynoapps.exchange_rates.model.YorumlarRate;
import dynoapps.exchange_rates.provider.BasePoolingDataProvider;
import dynoapps.exchange_rates.provider.BigparaRateProvider;
import dynoapps.exchange_rates.provider.DolarTlKurRateProvider;
import dynoapps.exchange_rates.provider.EnparaRateProvider;
import dynoapps.exchange_rates.provider.IPollingSource;
import dynoapps.exchange_rates.provider.ProviderSourceCallbackAdapter;
import dynoapps.exchange_rates.provider.YapıKrediRateProvider;
import dynoapps.exchange_rates.provider.YorumlarRateProvider;

/**
 * Created by erdemmac on 05/12/2016.
 */

public class RatePollingService extends Service {
    ArrayList<BasePoolingDataProvider> providers = new ArrayList<>();
    ArrayList<RateDataSource> rateDataSources = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (providers.size() < 1) return;
        providers.add(new YorumlarRateProvider(new ProviderSourceCallbackAdapter<List<YorumlarRate>>() {
            @Override
            public void onResult(List<YorumlarRate> rates) {
                EventBus.getDefault().post(rates);
            }
        }));
        providers.add(new EnparaRateProvider(new ProviderSourceCallbackAdapter<List<BuySellRate>>() {
            @Override
            public void onResult(List<BuySellRate> rates) {
                EventBus.getDefault().post(rates);
            }
        }));

        providers.add(
                new BigparaRateProvider(new ProviderSourceCallbackAdapter<List<BuySellRate>>() {
                    @Override
                    public void onResult(List<BuySellRate> rates) {
                        EventBus.getDefault().post(rates);
                    }
                }));

        providers.add(new DolarTlKurRateProvider(new ProviderSourceCallbackAdapter<List<DolarTlKurRate>>() {
            @Override
            public void onResult(List<DolarTlKurRate> rates) {
                EventBus.getDefault().post(rates);
            }
        }));


        providers.add(new YapıKrediRateProvider(new ProviderSourceCallbackAdapter<List<YapıKrediRate>>() {
            @Override
            public void onResult(List<YapıKrediRate> rates) {
                EventBus.getDefault().post(rates);
            }
        }));
        DataSourcesManager.init();
        DataSourcesManager.updateProviders(providers);
    }

    @Override
    public void onDestroy() {
        if (providers != null) {
            for (IPollingSource iPollingSource : providers) {
                iPollingSource.stop();
            }
        }
        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }
}
