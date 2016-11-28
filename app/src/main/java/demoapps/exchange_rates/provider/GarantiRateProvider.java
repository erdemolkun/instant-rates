package demoapps.exchange_rates.provider;

import java.util.List;

import demoapps.exchange_rates.data.GarantiRate;
import demoapps.exchange_rates.service.Api;
import demoapps.exchange_rates.service.GarantiService;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class GarantiRateProvider extends BasePoolingDataProvider<List<GarantiRate>> implements IPollingSource {

    private Call<List<GarantiRate>> lastCall;

    public GarantiRateProvider(SourceCallback<List<GarantiRate>> callback) {
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
        final GarantiService garantiService = Api.getGarantiApi().create(GarantiService.class);
        Call<List<GarantiRate>> call = garantiService.rates();
        call.enqueue(new retrofit2.Callback<List<GarantiRate>>() {
            @Override
            public void onResponse(Call<List<GarantiRate>> call, Response<List<GarantiRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GarantiRate> rates = response.body();
                    notifyValue(rates);
                    fetchAgain(false);
                } else {
                    notifyError();
                    fetchAgain(true);
                }
            }

            @Override
            public void onFailure(Call<List<GarantiRate>> call, Throwable t) {
                notifyError();
                fetchAgain(true);
            }
        });
        lastCall = call;
    }
}
