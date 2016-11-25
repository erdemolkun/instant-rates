package demoapps.exchangegraphics.provider;

import java.util.List;

import demoapps.exchangegraphics.data.BuySellRate;
import demoapps.exchangegraphics.service.Api;
import demoapps.exchangegraphics.service.EnparaService;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class EnparaRateProvider extends PoolingDataProvider<List<BuySellRate>> implements IRateProvider {

    private Call lastCall;

    public EnparaRateProvider(Callback callback) {
        super(callback);
    }


    @Override
    public void cancel() {
        if (lastCall != null)
            lastCall.cancel();
    }

    @Override
    public void run() {
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
