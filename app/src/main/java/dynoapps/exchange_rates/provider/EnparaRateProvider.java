package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.rates.EnparaRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.EnparaService;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class EnparaRateProvider extends BasePoolingProvider<List<EnparaRate>> {

    private CompositeDisposable compositeDisposable;

    public EnparaRateProvider(SourceCallback<List<EnparaRate>> callback) {
        super(callback);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public int getSourceType() {
        return CurrencyType.ENPARA;
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

        compositeDisposable.add(Api.getEnparaApi().create(EnparaService.class).rates()
                .subscribeOn(Schedulers.newThread())
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
