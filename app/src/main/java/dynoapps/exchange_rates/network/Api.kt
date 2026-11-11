package dynoapps.exchange_rates.network

import dynoapps.exchange_rates.converters.BigparaConverter
import dynoapps.exchange_rates.converters.BloombergConverter
import dynoapps.exchange_rates.converters.DolarTlKurAjaxConverter
import dynoapps.exchange_rates.converters.EnparaConverter
import dynoapps.exchange_rates.converters.ParagarantiConverter
import dynoapps.exchange_rates.converters.SingleConverterFactory
import dynoapps.exchange_rates.converters.YahooConverter
import dynoapps.exchange_rates.converters.YorumlarConverter
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import java.util.concurrent.TimeUnit

object Api {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    val paraGarantiService: ParaGarantiService by lazy {
        create("https://realtime.paragaranti.com/", ParagarantiConverter)
    }

    val yorumlarService: YorumlarService by lazy {
        create("https://yorum.altin.in/", YorumlarConverter)
    }

    val enparaService: EnparaService by lazy {
        create("https://www.qnbfinansbank.enpara.com/", EnparaConverter)
    }

    val bigparaService: BigparaService by lazy {
        create("http://www.bigpara.com/", BigparaConverter)
    }

    val dolarTlKurService: DolarTlKurService by lazy {
        create("http://dolar.tlkur.com/", DolarTlKurAjaxConverter)
    }

    val yahooService: YahooService by lazy {
        create("http://finance.yahoo.com/", YahooConverter)
    }

    val bloombergService: BloombergService by lazy {
        create("https://www.bloomberght.com", BloombergConverter)
    }

    private inline fun <reified S> create(baseUrl: String, converter: Converter<okhttp3.ResponseBody, *>): S =
        Retrofit.Builder()
            .client(client)
            .baseUrl(baseUrl)
            .addConverterFactory(SingleConverterFactory(converter))
            // Synchronous calls keep the previous RxJava 2 adapter behaviour; the providers
            // subscribe on the IO scheduler themselves.
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createSynchronous())
            .build()
            .create(S::class.java)
}
