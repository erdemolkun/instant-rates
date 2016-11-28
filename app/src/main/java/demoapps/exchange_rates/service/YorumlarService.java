package demoapps.exchange_rates.service;


import java.util.List;

import demoapps.exchange_rates.data.YorumlarRate;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Created by erdemmac on 24/10/2016.
 */

public interface YorumlarService {

    @Headers({
            "Content-Type:text/html; Charset=iso-8859-9"
    })
    @GET("guncel.asp")
    Call<List<YorumlarRate>> rates(@Query("ajax") String type);

}
