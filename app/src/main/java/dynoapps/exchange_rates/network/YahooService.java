package dynoapps.exchange_rates.network;

import java.util.List;

import dynoapps.exchange_rates.model.rates.YahooRate;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by @Erdem OLKUN on 10/12/2016.
 */

public interface YahooService {

    @GET("d/quotes.csv?e=.csv&f=sl1d1t1&s=USDTRY=X,EURTRY=X,EURUSD=X,GC=F")
    Call<List<YahooRate>> rates();
}
