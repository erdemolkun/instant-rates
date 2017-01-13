package dynoapps.exchange_rates.provider;

import java.util.List;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.ParagarantiResponse;
import dynoapps.exchange_rates.model.rates.ParaGarantiRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.ParaGarantiService;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class ParaGarantiRateProvider extends BasePoolingProvider<List<ParaGarantiRate>> {

    private Call<ParagarantiResponse> lastCall;

    public ParaGarantiRateProvider(SourceCallback<List<ParaGarantiRate>> callback) {
        super(callback);
    }

    @Override
    public int getSourceType() {
        return CurrencyType.PARAGARANTI;
    }

    @Override
    public void cancel() {
        if (lastCall != null) {
            lastCall.cancel();
        }
    }

    private void job(final boolean is_single_run) {
        final ParaGarantiService paraGarantiService = Api.getParaGarantiApi().create(ParaGarantiService.class);
        Call<ParagarantiResponse> call = paraGarantiService.rates();
        call.enqueue(new retrofit2.Callback<ParagarantiResponse>() {
            @Override
            public void onResponse(Call<ParagarantiResponse> call, Response<ParagarantiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ParagarantiResponse paragarantiResponse = response.body();
                    List<ParaGarantiRate> rates = paragarantiResponse.rates;
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
            public void onFailure(Call<ParagarantiResponse> call, Throwable t) {
                notifyError();
                if (!is_single_run)
                    fetchAgain(true);
            }
        });
        if (!is_single_run) {
            lastCall = call;
        }
    }

    @Override
    public void run(boolean is_single_run) {
        job(is_single_run);
    }

    @Override
    public void run() {
        job(false);
    }
}
