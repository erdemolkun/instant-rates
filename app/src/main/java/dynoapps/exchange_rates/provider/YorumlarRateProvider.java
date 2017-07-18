package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.rates.YorumlarRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.YorumlarService;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class YorumlarRateProvider extends BasePoolingProvider<List<YorumlarRate>> {

    private CompositeDisposable compositeDisposable;

    public YorumlarRateProvider(SourceCallback<List<YorumlarRate>> callback) {
        super(callback);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public int getSourceType() {
        return CurrencyType.ALTININ;
    }

    @Override
    public void cancel() {
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
    }

    private void job(final boolean is_single_run) {
        compositeDisposable.add(Api.getYorumlarApi().create(YorumlarService.class).rates("ons")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableObserver<List<YorumlarRate>>() {
                    @Override
                    public void onNext(List<YorumlarRate> rates) {
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

    @Override
    public void run(boolean is_single_run) {
        job(is_single_run);
    }

    @Override
    public void run() {
        job(false);
    }
}
