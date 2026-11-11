package dynoapps.exchange_rates.network

import dynoapps.exchange_rates.model.rates.BigparaRate
import dynoapps.exchange_rates.model.rates.BloombergRate
import dynoapps.exchange_rates.model.rates.DolarTlKurRate
import dynoapps.exchange_rates.model.rates.EnparaRate
import dynoapps.exchange_rates.model.rates.ParaGarantiRate
import dynoapps.exchange_rates.model.rates.YahooRate
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

interface DolarTlKurService {
    @Headers("Content-Type:text/html")
    @GET("/refresh/header/viewHeader.php")
    fun rates(@Query("_") time: String): Observable<List<DolarTlKurRate>>
}

interface EnparaService {
    @Headers("Content-Type:text/html; charset=utf-8")
    @GET("hesaplar/doviz-ve-altin-kurlari")
    fun rates(): Observable<List<EnparaRate>>
}

interface ParaGarantiService {
    @Headers("Content-Type:text/html;charset=UTF-8")
    @GET("asp/xml/icpiyasaX.xml")
    fun rates(): Observable<List<ParaGarantiRate>>
}

interface YahooService {
    @GET("d/quotes.csv?e=.csv&f=sl1d1t1&s=USDTRY=X,EURTRY=X,EURUSD=X,GC=F")
    fun rates(): Observable<List<YahooRate>>
}

interface YorumlarService {
    @Headers("Content-Type:text/html; Charset=iso-8859-9")
    @GET("guncel.asp")
    fun rates(@Query("ajax") type: String): Observable<List<YorumlarRate>>
}
