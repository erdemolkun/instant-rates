package demoapps.exchangegraphics.provider;

import java.util.List;

import demoapps.exchangegraphics.data.YorumlarRate;
import demoapps.exchangegraphics.service.Api;
import demoapps.exchangegraphics.service.YorumlarService;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class YorumlarRateProvider extends PoolingDataProvider<List<YorumlarRate>> implements IRateProvider {

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            fetch();
        }
    };

    public YorumlarRateProvider(Callback callback) {
        super(callback);
    }

    private void fetch() {
        final YorumlarService yorumlarService = Api.getYorumlarApi().create(YorumlarService.class);
        Call<List<YorumlarRate>> call = yorumlarService.getWithType("ons");
        call.enqueue(new retrofit2.Callback<List<YorumlarRate>>() {
            @Override
            public void onResponse(Call<List<YorumlarRate>> call, Response<List<YorumlarRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<YorumlarRate> rates = response.body();
                    notifyValue(rates);
                    fetchAgain(false);
                } else {
                    fetchAgain(true);
                    notifyError();
                }
            }

            @Override
            public void onFailure(Call<List<YorumlarRate>> call, Throwable t) {
                fetchAgain(true);
                notifyError();
            }
        });
    }


    @Override
    Runnable getWork() {
        return runnable;
    }
}
