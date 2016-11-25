package demoapps.exchangegraphics.provider;

import java.util.List;

import demoapps.exchangegraphics.data.EnparaRate;
import demoapps.exchangegraphics.service.Api;
import demoapps.exchangegraphics.service.EnparaService;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class EnparaRateProvider extends PoolingDataProvider<List<EnparaRate>> implements IRateProvider {

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            fetch();
        }
    };

    public EnparaRateProvider(Callback callback) {
        super(callback);
    }

    private void fetch() {
        final EnparaService enparaService = Api.getEnparaApi().create(EnparaService.class);
        Call<List<EnparaRate>> call = enparaService.getValues();
        call.enqueue(new retrofit2.Callback<List<EnparaRate>>() {
            @Override
            public void onResponse(Call<List<EnparaRate>> call, Response<List<EnparaRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<EnparaRate> rates = response.body();
                    notifyValue(rates);
                    getHandler().postDelayed(runnable, INTERVAL);
                } else {
                    getHandler().postDelayed(runnable, INTERVAL_ON_ERROR);
                    notifyError();
                }
            }

            @Override
            public void onFailure(Call<List<EnparaRate>> call, Throwable t) {
                getHandler().postDelayed(runnable, INTERVAL_ON_ERROR);
                notifyError();
            }
        });
    }

    @Override
    Runnable getWork() {
        return runnable;
    }
}
