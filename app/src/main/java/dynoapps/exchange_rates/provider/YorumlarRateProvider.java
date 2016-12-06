package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.model.rates.YorumlarRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.YorumlarService;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class YorumlarRateProvider extends BasePoolingDataProvider<List<YorumlarRate>> {

    private Call<List<YorumlarRate>> lastCall;

    public YorumlarRateProvider(SourceCallback<List<YorumlarRate>> callback) {
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
        final YorumlarService yorumlarService = Api.getYorumlarApi().create(YorumlarService.class);
        Call<List<YorumlarRate>> call = yorumlarService.rates("ons");
        call.enqueue(new retrofit2.Callback<List<YorumlarRate>>() {
            @Override
            public void onResponse(Call<List<YorumlarRate>> call, Response<List<YorumlarRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<YorumlarRate> rates = response.body();
                    notifyValue(rates);
                    fetchAgain(false);
                } else {
                    notifyError();
                    fetchAgain(true);
                }
            }

            @Override
            public void onFailure(Call<List<YorumlarRate>> call, Throwable t) {
                notifyError();
                fetchAgain(true);
            }
        });
        lastCall = call;
    }
}
