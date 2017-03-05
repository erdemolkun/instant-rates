package dynoapps.exchange_rates.provider;

import java.io.IOException;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.DynamicDataResponse;
import dynoapps.exchange_rates.model.rates.BloombergRate;
import dynoapps.exchange_rates.network.Api;
import dynoapps.exchange_rates.network.BloombergService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by @Erdem OLKUN on 05/03/2017.
 */

public class BloombergProvider extends BasePoolingProvider<BloombergRate> {

    public BloombergProvider(SourceCallback<BloombergRate> callback) {
        super(callback);
    }

    @Override
    public void run(boolean is_single_run) {
        BloombergService bloombergService = Api.getBloombergApi().create(BloombergService.class);
        Call<DynamicDataResponse> callDynamic = bloombergService.getDynamicData();
        callDynamic.enqueue(new Callback<DynamicDataResponse>() {
            @Override
            public void onResponse(Call<DynamicDataResponse> call, Response<DynamicDataResponse> response) {
                BloombergService updsecService = Api.getBloombergUpSecApi().create(BloombergService.class);
                try {
                    Response<String> responseUpdsec = updsecService.getUpdsec().execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // TODO encryption
            }

            @Override
            public void onFailure(Call<DynamicDataResponse> call, Throwable t) {

            }
        });


    }

    @Override
    public void cancel() {

    }

    @Override
    public int getSourceType() {
        return CurrencyType.BLOOMBERG;
    }
}
