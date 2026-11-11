package dynoapps.exchange_rates.provider;

import java.util.ArrayList;
import java.util.List;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.rates.EnparaRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.EnparaService;
import dynoapps.exchange_rates.util.L;
import io.reactivex.rxjava3.core.Observable;

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
            List<EnparaRate> parsed = new ArrayList<>();
            for (EnparaRate rate : rates) {
                try {
                    rate.toRateType();
                    rate.setRealValues();
                    parsed.add(rate);
                } catch (RuntimeException e) {
                    L.ex(e, "Skipping unparsable Enpara rate: " + rate.type);
                }
            }
            return parsed;
        });
    }

    @Override
    public int getSourceType() {
        return CurrencyType.ENPARA;
    }

}
