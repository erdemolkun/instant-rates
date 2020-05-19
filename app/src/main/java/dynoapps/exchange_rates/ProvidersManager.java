package dynoapps.exchange_rates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dynoapps.exchange_rates.alarm.Alarm;
import dynoapps.exchange_rates.alarm.AlarmsRepository;
import dynoapps.exchange_rates.data.CurrencySource;
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
import io.reactivex.subjects.PublishSubject;

public class ProvidersManager {
    private static final String TAG = "ProvidersManager";

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    List<BasePoolingProvider<?>> providers;
    private AlarmsRepository alarmsRepository;
    private static final Formatter formatter2 = new Formatter(3, 0);
    private static final Formatter formatter5 = new Formatter(5, 1);

    private final PublishSubject<RatesEvent> ratesEventPublishSubject = PublishSubject.create();

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

    public PublishSubject<RatesEvent> getRatesEventPublishSubject() {
        return ratesEventPublishSubject;
    }

    public void triggerUpdate() {
        TimeIntervalManager.setAlarmMode(false);
        for (CurrencySource currencySource : SourcesManager.getCurrencySources()) {
            if (currencySource.isEnabled()) {
                BasePoolingProvider<?> provider = SourcesManager.getProviderForSource(providers, currencySource);
                if (provider != null) {
                    provider.oneShot();
                }
            }
        }
    }

    public Disposable registerIntervalUpdates() {
        return TimeIntervalManager.getIntervalUpdates().distinctUntilChanged().observeOn(AndroidSchedulers.mainThread()).subscribe(isImmediate -> {
            TimeIntervalManager.setAlarmMode(false);
            for (BasePoolingProvider<?> provider : ProvidersManager.getInstance().getProviders()) {
                provider.refreshIntervals(isImmediate);
            }
        }, th -> L.e("RatePollingService", th.getLocalizedMessage()));
    }


    private void init() {
        if (initialized.get()) return;
        providers = new ArrayList<>();

        providers.add(new EnparaRateProvider(new ProviderSourceCallbackAdapter<List<EnparaRate>>() {
            @Override
            public void onResult(List<EnparaRate> rates, int type) {
                onProviderResult(rates, type);
            }
        }));

        providers.add(new YorumlarRateProvider(new ProviderSourceCallbackAdapter<List<YorumlarRate>>() {
            @Override
            public void onResult(List<YorumlarRate> rates, int type) {
                onProviderResult(rates, type);
            }
        }));

        providers.add(new BigparaRateProvider(new ProviderSourceCallbackAdapter<List<BigparaRate>>() {
            @Override
            public void onResult(List<BigparaRate> rates, int type) {
                onProviderResult(rates, type);
            }
        }));

        providers.add(new DolarTlKurRateProvider(new ProviderSourceCallbackAdapter<List<DolarTlKurRate>>() {
            @Override
            public void onResult(List<DolarTlKurRate> rates, int type) {
                onProviderResult(rates, type);
            }
        }));

        providers.add(new YapıKrediRateProvider(new ProviderSourceCallbackAdapter<List<YapıKrediRate>>() {
            @Override
            public void onResult(List<YapıKrediRate> rates, int type) {
                onProviderResult(rates, type);
            }
        }));
        providers.add(new YahooRateProvider(new ProviderSourceCallbackAdapter<List<YahooRate>>() {
            @Override
            public void onResult(List<YahooRate> rates, int type) {
                onProviderResult(rates, type);
            }
        }));

        providers.add(new ParaGarantiRateProvider(new ProviderSourceCallbackAdapter<List<ParaGarantiRate>>() {
            @Override
            public void onResult(List<ParaGarantiRate> rates, int type) {
                onProviderResult(rates, type);
            }
        }));

        providers.add(new BloombergRateProvider(new ProviderSourceCallbackAdapter<List<BloombergRate>>() {
            @Override
            public void onResult(List<BloombergRate> rates, int type) {
                onProviderResult(rates, type);
            }
        }));
        initialized.set(true);
    }

    public void startSources() {
        alarmsRepository = App.getInstance().provideAlarmsRepository();
        init();
        startOrStopSources();
    }

    private void onProviderResult(List<? extends BaseRate> rates, int sourceType) {
        updateAlarmsAndCheck(rates, sourceType);
        RatesHolder.getInstance().addRate(rates, sourceType);
        ratesEventPublishSubject.onNext(new RatesEvent<>(rates, sourceType, System.currentTimeMillis()));
    }

    public void stopAll() {
        L.i(TAG, "stopAll");
        if (providers != null) {
            for (IPollingSource iPollingSource : providers) {
                iPollingSource.stop();
            }
        }
    }

    public List<BasePoolingProvider<?>> getProviders() {
        return providers;
    }

    public void startOrStopSources() {
        List<CurrencySource> currencySources = SourcesManager.getCurrencySources();
        for (CurrencySource currencySource : currencySources) {
            IPollingSource iPollingSource = SourcesManager.getProviderForSource(providers, currencySource);
            if (iPollingSource == null) continue;
            if (currencySource.isEnabled()) {
                iPollingSource.start();
            } else {
                iPollingSource.stop();
            }
        }
    }

    private <T extends BaseRate> void updateAlarmsAndCheck(final List<T> rates, final int sourceType) {
        if (CollectionUtils.isNullOrEmpty(rates)) return;
        alarmsRepository.getAlarms(alarms -> checkAlarms(rates, sourceType, alarms));
    }

    private <T extends BaseRate> void checkAlarms(List<T> rates, int sourceType, List<Alarm> alarms) {
        if (CollectionUtils.isNullOrEmpty(rates)) return;
        try {

            Iterator<Alarm> iterator = alarms.iterator();
            while (iterator.hasNext()) {
                Alarm alarm = iterator.next();
                if (alarm.sourceType != sourceType || !alarm.isEnabled) continue;
                BaseRate baseRateCurrent = RateUtils.getRate(rates, alarm.rateType);
                RatesEvent ratesEvent = RatesHolder.getInstance().getLatestEvent(sourceType);
                BaseRate baseRateOld = ratesEvent != null ? RateUtils.getRate(ratesEvent.rates, alarm.rateType) : null;

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
                String val = alarm.rateType == IRate.ONS ? formatter2.format(alarm.val) : formatter5.format(alarm.val);
                if (alarm.isAbove && val_current > alarm.val && val_old <= alarm.val) {
                    iterator.remove();
                    alarmsRepository.deleteAlarm(alarm, null);
                    NotificationHelper.showAlarmNotification(App.context(), App.context().getString(R.string.is_above_val, SourcesManager.getSourceName(alarm.sourceType), RateUtils.rateName(alarm.rateType),
                            val), "increasing", Alarm.getPushId(alarm));
                } else if (!alarm.isAbove && val_current < alarm.val && val_old >= alarm.val) {
                    iterator.remove();
                    alarmsRepository.deleteAlarm(alarm, null);
                    NotificationHelper.showAlarmNotification(App.context(), App.context().getString(R.string.is_below_value, SourcesManager.getSourceName(alarm.sourceType), RateUtils.rateName(alarm.rateType),
                            val), "decreasing", Alarm.getPushId(alarm));
                }
            }

        } catch (Exception ex) {
            L.ex(ex);
        }
    }
}
