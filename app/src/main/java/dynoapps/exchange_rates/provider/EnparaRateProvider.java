package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.rates.EnparaRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.EnparaService;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class EnparaRateProvider extends BasePoolingDataProvider<List<EnparaRate>> {

    private Call<List<EnparaRate>> lastCall;

    public EnparaRateProvider(SourceCallback<List<EnparaRate>> callback) {
        super(callback);
    }

    @Override
    public int getSourceType() {
        return CurrencyType.ENPARA;
    }

    @Override
    public void cancel() {
        if (lastCall != null)
            lastCall.cancel();
    }

    @Override
    public void run() {
        run(false);
    }

    @Override
    public void run(final boolean is_single_run) {
        final EnparaService enparaService = Api.getEnparaApi().create(EnparaService.class);
        Call<List<EnparaRate>> call = enparaService.rates();
        call.enqueue(new retrofit2.Callback<List<EnparaRate>>() {
            @Override
            public void onResponse(Call<List<EnparaRate>> call, Response<List<EnparaRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<EnparaRate> rates = response.body();
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
            public void onFailure(Call<List<EnparaRate>> call, Throwable t) {
                notifyError();
                if (!is_single_run)
                    fetchAgain(true);
            }
        });
        lastCall = call;
    }
}
