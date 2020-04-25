package dynoapps.exchange_rates.network;


import java.util.List;

import dynoapps.exchange_rates.model.rates.ParaGarantiRate;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Headers;

/**
 * Created by erdemmac on 24/10/2016.
 */

public interface ParaGarantiService {

    @Headers({
            "Content-Type:text/html;charset=UTF-8"
    })
    @GET("asp/xml/icpiyasaX.xml")
    Observable<List<ParaGarantiRate>> rates();

}
