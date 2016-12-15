package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.rates.YahooRate;
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
    public int getSourceType() {
        return CurrencyType.YAHOO;
    }

    @Override
    public void cancel() {
        if (lastCall != null) {
            lastCall.cancel();
        }
    }

    @Override
    public void run() {
        run(false);
    }

    @Override
    public void run(final boolean is_single_run) {
        final YahooService yahooService = Api.getYahooApi().create(YahooService.class);
        Call<List<YahooRate>> call = yahooService.rates();
        call.enqueue(new retrofit2.Callback<List<YahooRate>>() {
            @Override
            public void onResponse(Call<List<YahooRate>> call, Response<List<YahooRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<YahooRate> rates = response.body();
                    notifyValue(rates);
                    if (!is_single_run)
                        fetchAgain(false);
                } else {
                    notifyError();
                    if (!is_single_run)
                        fetchAgain(true);
                }
            }

            @Override
            public void onFailure(Call<List<YahooRate>> call, Throwable t) {
                if (!call.isCanceled()) {
                    notifyError();
                    if (!is_single_run)
                        fetchAgain(true);
                }
            }
        });
        lastCall = call;
    }
}
