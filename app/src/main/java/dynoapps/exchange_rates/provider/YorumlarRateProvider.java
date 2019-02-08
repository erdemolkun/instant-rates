package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.rates.YorumlarRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.YorumlarService;
import io.reactivex.Observable;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class YorumlarRateProvider extends BasePoolingProvider<List<YorumlarRate>> {

    private YorumlarService yorumlarService;

    public YorumlarRateProvider(SourceCallback<List<YorumlarRate>> callback) {
        super(callback);
        yorumlarService = Api.getYorumlarApi().create(YorumlarService.class);
    }

    @Override
    protected Observable<List<YorumlarRate>> getObservable() {
        return yorumlarService.rates("ons");
    }

    @Override
    public int getSourceType() {
        return CurrencyType.ALTININ;
    }

}
