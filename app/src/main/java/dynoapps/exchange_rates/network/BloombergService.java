package dynoapps.exchange_rates.network;


import dynoapps.exchange_rates.model.DynamicDataResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

/**
 * Created by erdemmac on 24/10/2016.
 */

public interface BloombergService {

    @Headers({
            "Content-Type:text/html"
    })
    @GET("dynamics/updateDynamicData")
    Call<DynamicDataResponse> getDynamicData();

}
