package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.rates.AvgRate;
import dynoapps.exchange_rates.model.rates.BloombergRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.BloombergService;
import io.reactivex.Observable;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class BloombergRateProvider extends BasePoolingProvider<List<BloombergRate>> {
    private BloombergService bloombergService;

    public BloombergRateProvider(SourceCallback<List<BloombergRate>> callback) {
        super(callback);

    }

    @Override
    protected Observable<List<BloombergRate>> getObservable() {
        if (bloombergService == null) {
            bloombergService = Api.getBloombergApi().create(BloombergService.class);
        }
        return bloombergService.rates().map(rates -> {
            for (AvgRate rate : rates) {
                rate.toRateType();
                rate.setRealValues();
            }
            return rates;
        });
    }

    @Override
    public int getSourceType() {
        return CurrencyType.BLOOMBERGHT;
    }

}
