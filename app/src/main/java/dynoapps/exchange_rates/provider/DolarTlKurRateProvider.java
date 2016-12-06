package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.model.rates.DolarTlKurRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.DolarTlKurService;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class DolarTlKurRateProvider extends BasePoolingDataProvider<List<DolarTlKurRate>> {

    private Call lastCall;

    public DolarTlKurRateProvider(SourceCallback<List<DolarTlKurRate>> callback) {
        super(callback);
    }

    @Override
    public void cancel() {
        if (lastCall != null) {
            lastCall.cancel();
        }
    }

    @Override
    public void run() {
        super.run();
        final DolarTlKurService dolarTlKurService = Api.getDolarTlKurApi().create(DolarTlKurService.class);
        Call<List<DolarTlKurRate>> call = dolarTlKurService.rates("" + System.currentTimeMillis());
        call.enqueue(new retrofit2.Callback<List<DolarTlKurRate>>() {
            @Override
            public void onResponse(Call<List<DolarTlKurRate>> call, Response<List<DolarTlKurRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DolarTlKurRate> rates = response.body();
                    notifyValue(rates);
                    fetchAgain(false);
                } else {
                    notifyError();
                    fetchAgain(true);
                }
            }

            @Override
            public void onFailure(Call<List<DolarTlKurRate>> call, Throwable t) {
                notifyError();
                fetchAgain(true);
            }
        });
        lastCall = call;
    }
}
