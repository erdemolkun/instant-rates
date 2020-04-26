package dynoapps.exchange_rates;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import dynoapps.exchange_rates.alarm.Alarm;
import dynoapps.exchange_rates.alarm.AlarmsRepository;
import dynoapps.exchange_rates.data.CurrencySource;
import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.data.RatesHolder;
import dynoapps.exchange_rates.event.RatesEvent;
import dynoapps.exchange_rates.interfaces.ValueType;
import dynoapps.exchange_rates.model.rates.AvgRate;
import dynoapps.exchange_rates.model.rates.BaseRate;
import dynoapps.exchange_rates.model.rates.BigparaRate;
import dynoapps.exchange_rates.model.rates.BloombergRate;
import dynoapps.exchange_rates.model.rates.BuySellRate;
import dynoapps.exchange_rates.model.rates.DolarTlKurRate;
import dynoapps.exchange_rates.model.rates.EnparaRate;
import dynoapps.exchange_rates.model.rates.IRate;
import dynoapps.exchange_rates.model.rates.ParaGarantiRate;
import dynoapps.exchange_rates.model.rates.YahooRate;
import dynoapps.exchange_rates.model.rates.YapıKrediRate;
import dynoapps.exchange_rates.model.rates.YorumlarRate;
import dynoapps.exchange_rates.notification.NotificationHelper;
import dynoapps.exchange_rates.provider.BasePoolingProvider;
import dynoapps.exchange_rates.provider.BigparaRateProvider;
import dynoapps.exchange_rates.provider.BloombergRateProvider;
import dynoapps.exchange_rates.provider.DolarTlKurRateProvider;
import dynoapps.exchange_rates.provider.EnparaRateProvider;
import dynoapps.exchange_rates.provider.IPollingSource;
import dynoapps.exchange_rates.provider.ParaGarantiRateProvider;
import dynoapps.exchange_rates.provider.ProviderSourceCallbackAdapter;
import dynoapps.exchange_rates.provider.YahooRateProvider;
import dynoapps.exchange_rates.provider.YapıKrediRateProvider;
import dynoapps.exchange_rates.provider.YorumlarRateProvider;
import dynoapps.exchange_rates.time.TimeIntervalManager;
import dynoapps.exchange_rates.util.CollectionUtils;
import dynoapps.exchange_rates.util.Formatter;
import dynoapps.exchange_rates.util.L;
import dynoapps.exchange_rates.util.RateUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class ProvidersManager {
    List<BasePoolingProvider<?>> providers;
    private AlarmsRepository alarmsRepository;
    private static Formatter formatter2 = new Formatter(3, 0);
    private static Formatter formatter5 = new Formatter(5, 1);

    private static ProvidersManager instance;


    private ProvidersManager() {
        // Hide explicit instantiation
    }

    public static ProvidersManager getInstance() {
        if (instance == null) {
            synchronized (ProvidersManager.class) {
                if (instance == null) {
                    instance = new ProvidersManager();
                }
            }
        }
        return instance;
    }

    public void triggerUpdate(){
        TimeIntervalManager.setAlarmMode(false);
        for (CurrencySource currencySource : SourcesManager.getCurrencySources()) {
            if (currencySource.isEnabled()) {
                currencySource.getPollingSource().one_shot();
            }
        }
    }

    public Disposable registerIntervalUpdates(){
        return TimeIntervalManager.getIntervalUpdates().observeOn(AndroidSchedulers.mainThread()).subscribe(isImmediate -> {
            TimeIntervalManager.setAlarmMode(false);
            for (BasePoolingProvider provider : ProvidersManager.getInstance().getProviders()) {
                provider.refreshIntervals(isImmediate);
            }
        }, th -> L.e("RatePollingService", th.getLocalizedMessage()));
    }

    public void initOrRefresh() {
        alarmsRepository = App.getInstance().provideAlarmsRepository();
        if (providers == null) {
            providers = new ArrayList<>();
        }
        if (providers.size() > 0) {
            for (BasePoolingProvider<?> provider : providers) {
                provider.stop();
            }
        } else {
            providers.add(new YorumlarRateProvider(new ProviderSourceCallbackAdapter<List<YorumlarRate>>() {
                @Override
                public void onResult(List<YorumlarRate> rates) {
                    onProviderResult(rates, CurrencyType.ALTININ);
                }
            }));
            providers.add(new EnparaRateProvider(new ProviderSourceCallbackAdapter<List<EnparaRate>>() {
                @Override
                public void onResult(List<EnparaRate> rates) {
                    onProviderResult(rates, CurrencyType.ENPARA);
                }
            }));

            providers.add(new BigparaRateProvider(new ProviderSourceCallbackAdapter<List<BigparaRate>>() {
                @Override
                public void onResult(List<BigparaRate> rates) {
                    onProviderResult(rates, CurrencyType.BIGPARA);
                }
            }));

            providers.add(new DolarTlKurRateProvider(new ProviderSourceCallbackAdapter<List<DolarTlKurRate>>() {
                @Override
                public void onResult(List<DolarTlKurRate> rates) {
                    onProviderResult(rates, CurrencyType.TLKUR);
                }
            }));

            providers.add(new YapıKrediRateProvider(new ProviderSourceCallbackAdapter<List<YapıKrediRate>>() {
                @Override
                public void onResult(List<YapıKrediRate> rates) {
                    onProviderResult(rates, CurrencyType.YAPIKREDI);
                }
            }));
            providers.add(new YahooRateProvider(new ProviderSourceCallbackAdapter<List<YahooRate>>() {
                @Override
                public void onResult(List<YahooRate> rates) {
                    onProviderResult(rates, CurrencyType.YAHOO);
                }
            }));

            providers.add(new ParaGarantiRateProvider(new ProviderSourceCallbackAdapter<List<ParaGarantiRate>>() {
                @Override
                public void onResult(List<ParaGarantiRate> rates) {
                    onProviderResult(rates, CurrencyType.PARAGARANTI);
                }
            }));

            providers.add(new BloombergRateProvider(new ProviderSourceCallbackAdapter<List<BloombergRate>>() {
                @Override
                public void onResult(List<BloombergRate> rates) {
                    onProviderResult(rates, CurrencyType.BLOOMBERGHT);
                }
            }));
        }

        SourcesManager.updateProviders(providers);
        refreshSources();
    }

    private void onProviderResult(List<? extends BaseRate> rates, int sourceType) {
        alarmChecks(rates, sourceType);
        RatesHolder.getInstance().addRate(rates, sourceType);
        EventBus.getDefault().post(new RatesEvent<>(rates, sourceType, System.currentTimeMillis()));
    }

    public void stopAll() {
        if (providers != null) {
            for (IPollingSource iPollingSource : providers) {
                iPollingSource.stop();
            }
        }
    }

    public List<BasePoolingProvider<?>> getProviders() {
        return providers;
    }

    public void refreshSources() {
        ArrayList<CurrencySource> currencySources = SourcesManager.getCurrencySources();
        for (CurrencySource currencySource : currencySources) {
            IPollingSource iPollingSource = currencySource.getPollingSource();
            if (iPollingSource == null) return;
            if (currencySource.isEnabled()) {
                iPollingSource.start();
            } else {
                iPollingSource.stop();
            }
        }
    }

    private <T extends BaseRate> void alarmChecks(final List<T> rates, final int source_type) {
        if (CollectionUtils.isNullOrEmpty(rates)) return;
        alarmsRepository.getAlarms(alarms -> alarmChecks(rates, source_type, alarms));
    }

    private <T extends BaseRate> void alarmChecks(List<T> rates, int source_type, List<Alarm> alarms) {
        if (CollectionUtils.isNullOrEmpty(rates)) return;
        try {

            Iterator<Alarm> iterator = alarms.iterator();
            while (iterator.hasNext()) {
                Alarm alarm = iterator.next();
                if (alarm.source_type != source_type || !alarm.is_enabled) continue;
                BaseRate baseRateCurrent = RateUtils.getRate(rates, alarm.rate_type);
                RatesEvent ratesEvent = RatesHolder.getInstance().getLatestEvent(source_type);
                BaseRate baseRateOld = ratesEvent != null ? RateUtils.getRate(ratesEvent.rates, alarm.rate_type) : null;

                if (baseRateCurrent == null || baseRateOld == null) continue;
                float val_current = 0.0f;
                float val_old = 0.0f;
                if (baseRateCurrent instanceof AvgRate) {
                    val_current = ((AvgRate) baseRateCurrent).val_real_avg;
                    val_old = ((AvgRate) baseRateOld).val_real_avg;
                } else if (baseRateCurrent instanceof BuySellRate) {
                    if (alarm.value_type != ValueType.NONE) {
                        val_current = baseRateCurrent.getValue(alarm.value_type);
                        val_old = baseRateOld.getValue(alarm.value_type);
                    } else {
                        val_current = ((BuySellRate) baseRateCurrent).value_sell_real;
                        val_old = ((BuySellRate) baseRateOld).value_sell_real;
                    }
                }
                String val = alarm.rate_type == IRate.ONS ? formatter2.format(alarm.val) : formatter5.format(alarm.val);
                if (alarm.is_above && val_current > alarm.val && val_old <= alarm.val) {
                    iterator.remove();
                    alarmsRepository.deleteAlarm(alarm, null);
                    showAlarmNotification(App.context(), App.context().getString(R.string.is_above_val, SourcesManager.getSourceName(alarm.source_type), RateUtils.rateName(alarm.rate_type),
                            val), "increasing", Alarm.getPushId(alarm));
                } else if (!alarm.is_above && val_current < alarm.val && val_old >= alarm.val) {
                    iterator.remove();
                    alarmsRepository.deleteAlarm(alarm, null);
                    showAlarmNotification(App.context(), App.context().getString(R.string.is_below_value, SourcesManager.getSourceName(alarm.source_type), RateUtils.rateName(alarm.rate_type),
                            val), "decreasing", Alarm.getPushId(alarm));
                }
            }

        } catch (Exception ex) {
            L.ex(ex);
        }
    }

    private void showAlarmNotification(Context context, String message, String category, int id) {

        NotificationHelper.createAlarmChannelIfNotExists(context);
        Intent pushIntent = new Intent(context, LandingActivity.class);
        pushIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                pushIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID_ALARM)
                        .setSmallIcon(R.drawable.ic_add_alarm_white_24dp)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                        .setContentTitle(context.getString(R.string.app_name))
                        .setDefaults(Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_SOUND
                                | Notification.DEFAULT_VIBRATE | Notification.FLAG_SHOW_LIGHTS);


        mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(message));

        mBuilder.setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setContentIntent(pendingIntent)
                .setContentText(message);
        mBuilder.setAutoCancel(true);
        Notification notification = mBuilder.build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(category, id, notification);
    }

}
