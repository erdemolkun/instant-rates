package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.data.BuySellRate;
import dynoapps.exchange_rates.service.Api;
import dynoapps.exchange_rates.service.EnparaService;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class EnparaRateProvider extends BasePoolingDataProvider<List<BuySellRate>> implements IPollingSource {

    private Call<List<BuySellRate>> lastCall;

    public EnparaRateProvider(SourceCallback<List<BuySellRate>> callback) {
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
        final EnparaService enparaService = Api.getEnparaApi().create(EnparaService.class);
        Call<List<BuySellRate>> call = enparaService.getValues();
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
