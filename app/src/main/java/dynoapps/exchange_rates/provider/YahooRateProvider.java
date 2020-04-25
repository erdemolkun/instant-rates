package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.rates.YahooRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.YahooService;
import io.reactivex.Observable;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class YahooRateProvider extends BasePoolingProvider<List<YahooRate>> {

    private YahooService yahooService;

    public YahooRateProvider(SourceCallback<List<YahooRate>> callback) {
        super(callback);

    }

    @Override
    protected Observable<List<YahooRate>> getObservable() {
        if (yahooService==null){
            yahooService = Api.getYahooApi().create(YahooService.class);
        }
        return yahooService.rates();
    }

    @Override
    public int getSourceType() {
        return CurrencyType.YAHOO;
    }

}
