package dynoapps.exchange_rates.network;


import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

import dynoapps.exchange_rates.PublishSettings;
import dynoapps.exchange_rates.converters.BigparaConverter;
import dynoapps.exchange_rates.converters.DolarTlKurAjaxConverter;
import dynoapps.exchange_rates.converters.EnparaConverter;
import dynoapps.exchange_rates.converters.GarantiConverter;
import dynoapps.exchange_rates.converters.YahooConverter;
import dynoapps.exchange_rates.converters.YorumlarConverter;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

/**
 * Created by erdemmac on 21/10/2016.
 */

public class Api {

    private static Retrofit yorumlarApi;
    private static Retrofit enparaApi;
    private static Retrofit bloombergApi;
    private static Retrofit bigparaApi;
    private static Retrofit dolarTlKurApi;
    private static Retrofit garantiApi;
    private static Retrofit yahooApi;
    private static Retrofit paraGarantiApi;


    public static Retrofit getParaGarantiApi() {
        if (paraGarantiApi == null) {
            final OkHttpClient client = builder().build();
            paraGarantiApi = new Retrofit.Builder()
                    .client(client)
                    .baseUrl("https://realtime.paragaranti.com/")
                    .addConverterFactory(SimpleXmlConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return paraGarantiApi;

    }

    public static Retrofit getYorumlarApi() {
        if (yorumlarApi == null) {
            final OkHttpClient client = builder().build();
            yorumlarApi = new Retrofit.Builder()
                    .client(client)
                    .baseUrl("https://yorumlar.altin.in/")
                    .addConverterFactory(new YorumlarConverter.Factory())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return yorumlarApi;

    }

    public static Retrofit getEnparaApi() {
        if (enparaApi == null) {
            final OkHttpClient client = builder().build();
            enparaApi = new Retrofit.Builder()
                    .client(client)
                    .baseUrl("http://www.qnbfinansbank.enpara.com/")
                    .addConverterFactory(new EnparaConverter.Factory())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return enparaApi;
    }

    public static Retrofit getBigparaApi() {
        if (bigparaApi == null) {
            final OkHttpClient client = builder().build();
            bigparaApi = new Retrofit.Builder()
                    .baseUrl("http://www.bigpara.com/")
                    .client(client)
                    .addConverterFactory(new BigparaConverter.Factory())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return bigparaApi;
    }

    public static Retrofit getDolarTlKurApi() {
        if (dolarTlKurApi == null) {
            dolarTlKurApi = new Retrofit.Builder()
                    .baseUrl("http://dolar.tlkur.com/")
                    .addConverterFactory(new DolarTlKurAjaxConverter.Factory())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return dolarTlKurApi;
    }

    public static Retrofit getYahooApi() {
        if (yahooApi == null) {

            final OkHttpClient client = builder().build();
            yahooApi = new Retrofit.Builder().
                    client(client).
                    addConverterFactory(new YahooConverter.Factory()).
                    addCallAdapterFactory(RxJava2CallAdapterFactory.create()).
                    baseUrl("http://finance.yahoo.com/").build();
        }
        return yahooApi;
    }

    public static Retrofit getGarantiApi() {
        if (garantiApi == null) {
            garantiApi = new Retrofit.Builder()
                    .baseUrl("https://www.garanti.com.tr/")
                    .addConverterFactory(new GarantiConverter.Factory())
                    .build();
        }
        return garantiApi;
    }

    public static Retrofit getBloombergApi() {
        if (bloombergApi == null) {
            final OkHttpClient client = new OkHttpClient.Builder()
                    .build();
            bloombergApi = new Retrofit.Builder()
                    .client(client)
                    .baseUrl("http://www.bloomberght.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return bloombergApi;
    }

    private static OkHttpClient.Builder builder() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS);
        if (PublishSettings.isAlphaOrDeveloper()) {
            builder.addNetworkInterceptor(new StethoInterceptor());
        }
        return builder;
    }

}
