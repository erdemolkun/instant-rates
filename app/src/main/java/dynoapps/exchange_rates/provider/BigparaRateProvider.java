package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.data.CurrencySource;
import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.rates.BigparaRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.BigparaService;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class BigparaRateProvider extends BasePoolingDataProvider<List<BigparaRate>> implements Runnable {

    private Call<List<BigparaRate>> lastCall;

    public BigparaRateProvider(SourceCallback<List<BigparaRate>> callback) {
        super(callback);
    }

    @Override
    public int getSourceType() {
        return CurrencyType.BIGPARA;
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
        Call<List<BigparaRate>> call = bigparaService.rates();
        call.enqueue(new retrofit2.Callback<List<BigparaRate>>() {
            @Override
            public void onResponse(Call<List<BigparaRate>> call, Response<List<BigparaRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BigparaRate> rates = response.body();
                    notifyValue(rates);
                    fetchAgain(false);
                } else {
                    notifyError();
                    fetchAgain(true);
                }
            }

            @Override
            public void onFailure(Call<List<BigparaRate>> call, Throwable t) {
                notifyError();
                fetchAgain(true);
            }
        });
        lastCall = call;
    }
}
