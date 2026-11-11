package dynoapps.exchange_rates.network

import dynoapps.exchange_rates.model.rates.BigparaRate
import dynoapps.exchange_rates.model.rates.BloombergRate
import dynoapps.exchange_rates.model.rates.YorumlarRate
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface BigparaService {
    @Headers("Content-Type:text/html; charset=utf-8")
    @GET("doviz/dolar/")
    fun rates(): Observable<List<BigparaRate>>
}

interface BloombergService {
    @Headers("Content-Type:text/html; charset=utf-8")
    @GET("/")
    fun rates(): Observable<List<BloombergRate>>
}

interface YorumlarService {
    @Headers("Content-Type:text/html; Charset=iso-8859-9")
    @GET("guncel.asp")
    fun rates(@Query("ajax") type: String): Observable<List<YorumlarRate>>
}
