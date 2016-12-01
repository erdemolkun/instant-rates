package dynoapps.exchange_rates.service;


import java.util.List;

import dynoapps.exchange_rates.model.DolarTlKurRate;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Created by erdemmac on 24/10/2016.
 */

public interface DolarTlKurService {

    @Headers({
            "Content-Type:text/html"
    })
    @GET("/refresh/header/viewHeader.php")
    Call<List<DolarTlKurRate>> rates(@Query("_") String time);

}
