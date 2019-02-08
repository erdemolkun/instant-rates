package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.rates.BigparaRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.BigparaService;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class BigparaRateProvider extends BasePoolingProvider<List<BigparaRate>> {

    private BigparaService bigparaService;

    public BigparaRateProvider(SourceCallback<List<BigparaRate>> callback) {
        super(callback);
        bigparaService = Api.getBigparaApi().create(BigparaService.class);
    }

    @Override
    public int getSourceType() {
        return CurrencyType.BIGPARA;
    }

    @Override
    public void run() {
        run(false);
    }

    @Override
    public void run(final boolean is_single_run) {
        compositeDisposable.add(bigparaService.rates()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableObserver<List<BigparaRate>>() {
                    @Override
                    public void onNext(List<BigparaRate> rates) {
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
