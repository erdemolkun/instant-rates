package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.rates.ParaGarantiRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.ParaGarantiService;
import io.reactivex.Observable;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class ParaGarantiRateProvider extends BasePoolingProvider<List<ParaGarantiRate>> {
    private ParaGarantiService paraGarantiService;

    public ParaGarantiRateProvider(SourceCallback<List<ParaGarantiRate>> callback) {
        super(callback);

    }

    @Override
    protected Observable<List<ParaGarantiRate>> getObservable() {
        if (paraGarantiService == null) {
            paraGarantiService = Api.getParaGarantiApi().create(ParaGarantiService.class);
        }
        return paraGarantiService.rates().map(rates -> {
            for (ParaGarantiRate rate : rates) {
                rate.toRateType();
                rate.setRealValues();
            }
            return rates;
        });
    }

    @Override
    public int getSourceType() {
        return CurrencyType.PARAGARANTI;
    }

}
