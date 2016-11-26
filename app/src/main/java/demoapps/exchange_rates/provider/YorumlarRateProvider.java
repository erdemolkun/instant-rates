package demoapps.exchange_rates.provider;

import java.util.List;

import demoapps.exchange_rates.data.YorumlarRate;
import demoapps.exchange_rates.service.Api;
import demoapps.exchange_rates.service.YorumlarService;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class YorumlarRateProvider extends PoolingDataProvider<List<YorumlarRate>> implements IRateProvider {

    private Call lastCall;

    public YorumlarRateProvider(Callback callback) {
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
        final YorumlarService yorumlarService = Api.getYorumlarApi().create(YorumlarService.class);
        Call<List<YorumlarRate>> call = yorumlarService.getWithType("ons");
        call.enqueue(new retrofit2.Callback<List<YorumlarRate>>() {
            @Override
            public void onResponse(Call<List<YorumlarRate>> call, Response<List<YorumlarRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<YorumlarRate> rates = response.body();
                    notifyValue(rates);
                    fetchAgain(false);
                } else {
                    fetchAgain(true);
                    notifyError();
                }
            }

            @Override
            public void onFailure(Call<List<YorumlarRate>> call, Throwable t) {
                fetchAgain(true);
                notifyError();
            }
        });
        lastCall = call;
    }
}
