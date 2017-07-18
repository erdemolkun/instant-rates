package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.rates.YahooRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.YahooService;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class YahooRateProvider extends BasePoolingProvider<List<YahooRate>> {

    private CompositeDisposable compositeDisposable;
    private YahooService yahooService;

    public YahooRateProvider(SourceCallback<List<YahooRate>> callback) {
        super(callback);
        yahooService = Api.getYahooApi().create(YahooService.class);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public int getSourceType() {
        return CurrencyType.YAHOO;
    }

    @Override
    public void cancel() {
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
    }

    @Override
    public void run() {
        run(false);
    }

    @Override
    public void run(final boolean is_single_run) {

        compositeDisposable.add(yahooService.rates()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableObserver<List<YahooRate>>() {
                    @Override
                    public void onNext(List<YahooRate> rates) {
                        notifyValue(rates);
                        if (!is_single_run)
                            fetchAgain(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        notifyError();
                        if (!is_single_run)
                            fetchAgain(true);
                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }
}
