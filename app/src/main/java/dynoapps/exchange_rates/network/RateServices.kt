package dynoapps.exchange_rates.network

import dynoapps.exchange_rates.model.rates.BigparaRate
import dynoapps.exchange_rates.model.rates.BloombergRate
import dynoapps.exchange_rates.model.rates.YapıKrediRate
import dynoapps.exchange_rates.model.rates.YorumlarRate
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface BigparaService {
    /** [symbols] is a comma-separated list of Bigpara symbol codes, such as "USDTRY,EURTRY". */
    @GET("api/trade/current")
    fun rates(@Query("Symbols") symbols: String): Observable<List<BigparaRate>>
}

interface BloombergService {
    /** The JSON behind the site's market slider: USD, EUR, EUR/USD and gold, among others. */
    @GET("finance-slider-data")
    fun rates(): Observable<List<BloombergRate>>
}

interface YapiKrediService {
    @GET("yatirimci-kosesi/doviz-kurlari/")
    fun rates(): Observable<List<YapıKrediRate>>
}

interface YorumlarService {
    @Headers("Content-Type:text/html; Charset=iso-8859-9")
    @GET("guncel.asp")
    fun rates(@Query("ajax") type: String): Observable<List<YorumlarRate>>
}
