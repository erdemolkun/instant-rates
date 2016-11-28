package demoapps.exchange_rates.service;

import java.util.concurrent.TimeUnit;

import demoapps.exchange_rates.converters.BigparaConverter;
import demoapps.exchange_rates.converters.DolarTlKurAjaxConverter;
import demoapps.exchange_rates.converters.EnparaConverter;
import demoapps.exchange_rates.converters.GarantiConverter;
import demoapps.exchange_rates.converters.YorumlarAjaxConverter;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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


    public static Retrofit getYorumlarApi() {
        if (yorumlarApi == null) {
            final OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(8, TimeUnit.SECONDS)
                    .readTimeout(8, TimeUnit.SECONDS)
                    .build();
            yorumlarApi = new Retrofit.Builder()
                    .client(client)
                    .baseUrl("https://yorumlar.altin.in/")
                    .addConverterFactory(new YorumlarAjaxConverter.Factory())
                    .build();
        }
        return yorumlarApi;

    }

    public static Retrofit getEnparaApi() {
        if (enparaApi == null) {
            enparaApi = new Retrofit.Builder()
                    .baseUrl("http://www.qnbfinansbank.enpara.com/")
                    .addConverterFactory(new EnparaConverter.Factory())
                    .build();
        }
        return enparaApi;
    }

    public static Retrofit getBigparaApi() {
        if (bigparaApi == null) {
            bigparaApi = new Retrofit.Builder()
                    .baseUrl("http://www.bigpara.com/")
                    .addConverterFactory(new BigparaConverter.Factory())
                    .build();
        }
        return bigparaApi;
    }

    public static Retrofit getDolarTlKurApi() {
        if (dolarTlKurApi == null) {
            dolarTlKurApi = new Retrofit.Builder()
                    .baseUrl("http://dolar.tlkur.com/")
                    .addConverterFactory(new DolarTlKurAjaxConverter.Factory())
                    .build();
        }
        return dolarTlKurApi;
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


}
