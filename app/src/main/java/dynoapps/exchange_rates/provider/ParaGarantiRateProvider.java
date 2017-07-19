package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.ParagarantiResponse;
import dynoapps.exchange_rates.model.rates.ParaGarantiRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.ParaGarantiService;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class ParaGarantiRateProvider extends BasePoolingProvider<List<ParaGarantiRate>> {
    private ParaGarantiService paraGarantiService;

    public ParaGarantiRateProvider(SourceCallback<List<ParaGarantiRate>> callback) {
        super(callback);
        paraGarantiService = Api.getParaGarantiApi().create(ParaGarantiService.class);
    }

    @Override
    public int getSourceType() {
        return CurrencyType.PARAGARANTI;
    }

    private void job(final boolean is_single_run) {

        compositeDisposable.add(paraGarantiService.rates()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableObserver<ParagarantiResponse>() {
                    @Override
                    public void onNext(ParagarantiResponse paragarantiResponse) {

                        List<ParaGarantiRate> rates = paragarantiResponse.rates;
                        for (ParaGarantiRate rate : rates) {
                            rate.toRateType();
                            rate.setRealValues();
                        }
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
