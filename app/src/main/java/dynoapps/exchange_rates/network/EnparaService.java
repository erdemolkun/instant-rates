package dynoapps.exchange_rates.network;

import java.util.List;

import dynoapps.exchange_rates.model.rates.EnparaRate;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Headers;

/**
 * Created by erdemmac on 24/10/2016.
 */

public interface EnparaService {

    @Headers({
            "Content-Type:text/html; charset=utf-8"
    })
    @GET("doviz-kur-bilgileri/doviz-altin-kurlari.aspx")
    Observable<List<EnparaRate>> rates();

}
