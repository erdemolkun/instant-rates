package demoapps.exchange_rates.service;


import java.util.List;

import demoapps.exchange_rates.data.GarantiRate;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

/**
 * Created by erdemmac on 24/10/2016.
 */

public interface GarantiService {

    @Headers({
            "Content-Type:text/html;charset=UTF-8",
            "Access-Control-Allow-Origin:*",
            "User-Agent:Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.98 Safari/537.36"
    })

    @GET("tr")
    Call<List<GarantiRate>> rates();

}
