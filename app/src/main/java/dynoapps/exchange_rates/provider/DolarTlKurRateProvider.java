package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.rates.DolarTlKurRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.DolarTlKurService;
import io.reactivex.Observable;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class DolarTlKurRateProvider extends BasePoolingProvider<List<DolarTlKurRate>> {

    private DolarTlKurService dolarTlKurService;

    public DolarTlKurRateProvider(SourceCallback<List<DolarTlKurRate>> callback) {
        super(callback);
        dolarTlKurService = Api.getDolarTlKurApi().create(DolarTlKurService.class);
    }

    @Override
    protected Observable<List<DolarTlKurRate>> getObservable() {
        return dolarTlKurService.rates("" + System.currentTimeMillis());
    }

    @Override
    public int getSourceType() {
        return CurrencyType.TLKUR;
    }

}
