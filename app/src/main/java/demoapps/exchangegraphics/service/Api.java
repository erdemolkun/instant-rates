package demoapps.exchangegraphics.service;

import demoapps.exchangegraphics.BigparaConverter;
import demoapps.exchangegraphics.EnparaConverter;
import demoapps.exchangegraphics.YorumlarAjaxConverter;
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


    public static Retrofit getYorumlarApi() {
        if (yorumlarApi == null) {
            yorumlarApi = new Retrofit.Builder()
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
