package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.rates.BigparaRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.BigparaService;
import io.reactivex.Observable;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class BigparaRateProvider extends BasePoolingProvider<List<BigparaRate>> {

    private BigparaService bigparaService;

    public BigparaRateProvider(SourceCallback<List<BigparaRate>> callback) {
        super(callback);

    }

    @Override
    protected Observable<List<BigparaRate>> getObservable() {
        if (bigparaService == null) {
            bigparaService = Api.getBigparaApi().create(BigparaService.class);
        }
        return bigparaService.rates();
    }

    @Override
    public int getSourceType() {
        return CurrencyType.BIGPARA;
    }

}
