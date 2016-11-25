package demoapps.exchangegraphics.provider;

import java.util.List;

import demoapps.exchangegraphics.data.DolarTlKurRate;
import demoapps.exchangegraphics.service.Api;
import demoapps.exchangegraphics.service.DolarTlKurService;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class DolarTlKurRateProvider extends PoolingDataProvider<List<DolarTlKurRate>> implements IRateProvider {

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            fetch();
        }
    };

    public DolarTlKurRateProvider(Callback callback) {
        super(callback);
    }

    private void fetch() {
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
                    fetchAgain(true);
                    notifyError();
                }
            }

            @Override
            public void onFailure(Call<List<DolarTlKurRate>> call, Throwable t) {
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
