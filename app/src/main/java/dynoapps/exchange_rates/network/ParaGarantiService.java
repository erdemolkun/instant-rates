package dynoapps.exchange_rates.network;


import dynoapps.exchange_rates.model.ParagarantiResponse;
import retrofit2.Call;
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
    Call<ParagarantiResponse> rates();

}
