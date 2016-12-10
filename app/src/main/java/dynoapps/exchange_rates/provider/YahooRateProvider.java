package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.model.rates.YahooRate;
import dynoapps.exchange_rates.model.rates.YorumlarRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.YahooService;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class YahooRateProvider extends BasePoolingDataProvider<List<YahooRate>> {

    private Call<List<YahooRate>> lastCall;

    public YahooRateProvider(SourceCallback<List<YahooRate>> callback) {
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
        final YahooService yahooService = Api.getYahooApi().create(YahooService.class);
        Call<List<YahooRate>> call = yahooService.rates();
        call.enqueue(new retrofit2.Callback<List<YahooRate>>() {
            @Override
            public void onResponse(Call<List<YahooRate>> call, Response<List<YahooRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<YahooRate> rates = response.body();
                    notifyValue(rates);
                    fetchAgain(false);
                } else {
                    notifyError();
                    fetchAgain(true);
                }
            }

            @Override
            public void onFailure(Call<List<YahooRate>> call, Throwable t) {
                notifyError();
                fetchAgain(true);
            }
        });
        lastCall = call;
    }
}
