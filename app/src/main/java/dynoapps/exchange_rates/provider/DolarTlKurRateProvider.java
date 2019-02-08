package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.rates.DolarTlKurRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.DolarTlKurService;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

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
    public int getSourceType() {
        return CurrencyType.TLKUR;
    }

    @Override
    public void run() {
        run(false);
    }

    @Override
    public void run(final boolean is_single_run) {

        compositeDisposable.add(dolarTlKurService.rates("" + System.currentTimeMillis())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableObserver<List<DolarTlKurRate>>() {
                    @Override
                    public void onNext(List<DolarTlKurRate> rates) {
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
