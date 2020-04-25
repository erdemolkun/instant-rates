package dynoapps.exchange_rates.network;


import java.util.List;

import dynoapps.exchange_rates.model.rates.BloombergRate;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Headers;

/**
 * Created by erdemmac on 24/10/2016.
 */

public interface BloombergService {

    @Headers({
            "Content-Type:text/html; charset=utf-8"
    })
    @GET("/")
    Observable<List<BloombergRate>> rates();

}
