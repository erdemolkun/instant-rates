package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.model.BuySellRate;
import dynoapps.exchange_rates.service.Api;
import dynoapps.exchange_rates.service.BigparaService;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class BigparaRateProvider extends BasePoolingDataProvider<List<BuySellRate>> implements Runnable {

    private Call<List<BuySellRate>> lastCall;

    public BigparaRateProvider(SourceCallback<List<BuySellRate>> callback) {
        super(callback);
    }

    @Override
    public void cancel() {
        if (lastCall != null)
            lastCall.cancel();
    }

    @Override
    public void run() {
        super.run();
        final BigparaService bigparaService = Api.getBigparaApi().create(BigparaService.class);
        Call<List<BuySellRate>> call = bigparaService.rates();
        call.enqueue(new retrofit2.Callback<List<BuySellRate>>() {
            @Override
            public void onResponse(Call<List<BuySellRate>> call, Response<List<BuySellRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BuySellRate> rates = response.body();
                    notifyValue(rates);
                    fetchAgain(false);
                } else {
                    notifyError();
                    fetchAgain(true);
                }
            }

            @Override
            public void onFailure(Call<List<BuySellRate>> call, Throwable t) {
                notifyError();
                fetchAgain(true);
            }
        });
        lastCall = call;
    }
}
