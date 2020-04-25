package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.rates.EnparaRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.EnparaService;
import io.reactivex.Observable;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class EnparaRateProvider extends BasePoolingProvider<List<EnparaRate>> {

    private EnparaService enparaService;

    public EnparaRateProvider(SourceCallback<List<EnparaRate>> callback) {
        super(callback);

    }

    @Override
    protected Observable<List<EnparaRate>> getObservable() {
        if (enparaService == null) {
            enparaService = Api.getEnparaApi().create(EnparaService.class);
        }
        return enparaService.rates().map(rates -> {
            for (EnparaRate rate : rates) {
                rate.toRateType();
                rate.setRealValues();
            }
            return rates;
        });
    }

    @Override
    public int getSourceType() {
        return CurrencyType.ENPARA;
    }

}
