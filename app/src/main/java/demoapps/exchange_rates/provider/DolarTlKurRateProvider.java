package demoapps.exchange_rates.provider;

import java.util.List;

import demoapps.exchange_rates.data.DolarTlKurRate;
import demoapps.exchange_rates.service.Api;
import demoapps.exchange_rates.service.DolarTlKurService;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class DolarTlKurRateProvider extends BasePoolingDataProvider<List<DolarTlKurRate>> implements IPollingSource {

    private Call lastCall;

    public DolarTlKurRateProvider(Callback callback) {
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
        final DolarTlKurService dolarTlKurService = Api.getDolarTlKurApi().create(DolarTlKurService.class);
        Call<List<DolarTlKurRate>> call = dolarTlKurService.getValues("" + System.currentTimeMillis());
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
