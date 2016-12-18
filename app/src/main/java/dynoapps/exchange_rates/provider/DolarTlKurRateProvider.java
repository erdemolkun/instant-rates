package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.rates.DolarTlKurRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.DolarTlKurService;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class DolarTlKurRateProvider extends BasePoolingProvider<List<DolarTlKurRate>> {

    private Call lastCall;

    public DolarTlKurRateProvider(SourceCallback<List<DolarTlKurRate>> callback) {
        super(callback);
    }

    @Override
    public int getSourceType() {
        return CurrencyType.TLKUR;
    }

    @Override
    public void cancel() {
        if (lastCall != null) {
            lastCall.cancel();
        }
    }

    @Override
    public void run() {
        run(false);
    }

    @Override
    public void run(final boolean is_single_run) {
        final DolarTlKurService dolarTlKurService = Api.getDolarTlKurApi().create(DolarTlKurService.class);
        Call<List<DolarTlKurRate>> call = dolarTlKurService.rates("" + System.currentTimeMillis());
        call.enqueue(new retrofit2.Callback<List<DolarTlKurRate>>() {
            @Override
            public void onResponse(Call<List<DolarTlKurRate>> call, Response<List<DolarTlKurRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DolarTlKurRate> rates = response.body();
                    notifyValue(rates);
                    if (!is_single_run)
                        fetchAgain(false);
                } else {
                    notifyError();
                    if (!is_single_run)
                        fetchAgain(true);
                }
            }

            @Override
            public void onFailure(Call<List<DolarTlKurRate>> call, Throwable t) {
                notifyError();
                if (!is_single_run)
                    fetchAgain(true);
            }
        });
        if (!is_single_run) {
            lastCall = call;
        }
    }
}
