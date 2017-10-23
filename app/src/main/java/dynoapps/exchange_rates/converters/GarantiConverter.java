package dynoapps.exchange_rates.converters;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import dynoapps.exchange_rates.model.rates.BaseRate;
import dynoapps.exchange_rates.model.rates.GarantiRate;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class GarantiConverter implements Converter<ResponseBody, List<BaseRate>> {

    static final GarantiConverter INSTANCE = new GarantiConverter();
    private static final String HOST = "https://www.garanti.com.tr/";

    private GarantiConverter() {
    }

    @Override
    public List<BaseRate> convert(ResponseBody value) throws IOException {

        ArrayList<BaseRate> rates = new ArrayList<>();
        String responseBody = value != null ? value.string() : null;

        ArrayList<Element> elements = Jsoup.parse(responseBody, HOST).select(".rightSideContainer").select("#tab10").select("tbody").select("tr");

        for (Element element : elements) {

            GarantiRate rate = new GarantiRate();
            rate.avg_val = element.child(2).text();
            rate.type = element.child(0).text();
            rate.toRateType();
            rate.setRealValues();
            rates.add(rate);
        }

        return rates;
    }

    /**
     * Factory for creating converter. We only care about decoding responses.
     **/
    public static final class Factory extends Converter.Factory {

        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type,
                                                                Annotation[] annotations,
                                                                Retrofit retrofit) {
            return INSTANCE;
        }

    }


}