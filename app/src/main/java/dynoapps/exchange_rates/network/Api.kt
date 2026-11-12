package dynoapps.exchange_rates.network

import dynoapps.exchange_rates.converters.BigparaConverter
import dynoapps.exchange_rates.converters.BloombergConverter
import dynoapps.exchange_rates.converters.SingleConverterFactory
import dynoapps.exchange_rates.converters.YapiKrediConverter
import dynoapps.exchange_rates.converters.YorumlarConverter
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object Api {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    val yorumlarService: YorumlarService by lazy {
        create("https://yorum.altin.in/", YorumlarConverter)
    }

    val bigparaService: BigparaService by lazy {
        create("https://api-bigpara.hurriyet.com.tr/", BigparaConverter)
    }

    val bloombergService: BloombergService by lazy {
        create("https://www.bloomberght.com/", BloombergConverter)
    }

    val yapiKrediService: YapiKrediService by lazy {
        create("https://www.yapikredi.com.tr/", YapiKrediConverter)
    }

    private inline fun <reified S> create(baseUrl: String, converter: Converter<okhttp3.ResponseBody, *>): S =
        Retrofit.Builder()
            .client(client)
            .baseUrl(baseUrl)
            // The services are suspend functions: Retrofit runs each call, including the
            // conversion, on OkHttp's threads, so they are safe to call from the main thread.
            .addConverterFactory(SingleConverterFactory(converter))
            .build()
            .create(S::class.java)
}
