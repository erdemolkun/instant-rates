package demoapps.exchange_rates.provider;

import java.util.List;

import demoapps.exchange_rates.data.BuySellRate;
import demoapps.exchange_rates.service.Api;
import demoapps.exchange_rates.service.BigparaService;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class BigparaRateProvider extends PoolingDataProvider<List<BuySellRate>> implements Runnable {


    private Call lastCall;

    public BigparaRateProvider(Callback callback) {
        super(callback);
    }


    @Override
    public void cancel() {
        if (lastCall != null)
            lastCall.cancel();
    }

    @Override
    public void run() {
        final BigparaService bigparaService = Api.getBigparaApi().create(BigparaService.class);
        Call<List<BuySellRate>> call = bigparaService.getData();
        call.enqueue(new retrofit2.Callback<List<BuySellRate>>() {
            @Override
            public void onResponse(Call<List<BuySellRate>> call, Response<List<BuySellRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BuySellRate> rates = response.body();
                    notifyValue(rates);
                    fetchAgain(false);
                } else {
                    fetchAgain(true);
                    notifyError();
                }
            }

            @Override
            public void onFailure(Call<List<BuySellRate>> call, Throwable t) {
                fetchAgain(true);
                notifyError();
            }
        });
        lastCall = call;
    }
}
