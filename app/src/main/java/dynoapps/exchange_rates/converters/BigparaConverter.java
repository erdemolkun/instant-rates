package dynoapps.exchange_rates.converters;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import dynoapps.exchange_rates.model.rates.BaseRate;
import dynoapps.exchange_rates.model.rates.BigparaRate;
import dynoapps.exchange_rates.model.rates.BuySellRate;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class BigparaConverter implements Converter<ResponseBody, List<BaseRate>> {

    private static final BigparaConverter INSTANCE = new BigparaConverter();
    private static final String HOST = "http://www.bigpara.com";

    private BigparaConverter() {
    }

    @Override
    public List<BaseRate> convert(@NonNull ResponseBody value) throws IOException {

        ArrayList<BaseRate> rates = new ArrayList<>();
        String responseBody = value.string();

        ArrayList<Element> elements = Jsoup.parse(responseBody, HOST).select("#content").select(".kurdetail").select(".kurbox");

        String val_buy = elements.get(1).select(".value").text();
        String val_sell = elements.get(2).select(".value").text();

        BuySellRate buySellRate = new BigparaRate();
        buySellRate.valueSell = val_sell;
        buySellRate.valueBuy = val_buy;
        buySellRate.type = elements.get(0).text();
        buySellRate.toRateType();
        buySellRate.setRealValues();

        rates.add(buySellRate);

        return rates;
    }

    /**
     * Factory for creating converter. We only care about decoding responses.
     **/
    public static final class Factory extends Converter.Factory {

        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(@NonNull Type type,
                                                                @NonNull Annotation[] annotations,
                                                                @NonNull Retrofit retrofit) {
            return INSTANCE;
        }

    }


}