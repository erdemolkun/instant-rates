package dynoapps.exchange_rates.network;


import java.util.List;

import dynoapps.exchange_rates.model.rates.YorumlarRate;
import io.reactivex.Observable;
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
    Observable<List<YorumlarRate>> rates(@Query("ajax") String type);

}
