package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.rates.EnparaRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.EnparaService;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class EnparaRateProvider extends BasePoolingProvider<List<EnparaRate>> {

    private EnparaService enparaService;

    public EnparaRateProvider(SourceCallback<List<EnparaRate>> callback) {
        super(callback);
        enparaService = Api.getEnparaApi().create(EnparaService.class);
    }

    @Override
    public int getSourceType() {
        return CurrencyType.ENPARA;
    }


    @Override
    public void run() {
        run(false);
    }

    @Override
    public void run(final boolean is_single_run) {

        compositeDisposable.add(enparaService.rates()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableObserver<List<EnparaRate>>() {
                    @Override
                    public void onNext(List<EnparaRate> rates) {
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
