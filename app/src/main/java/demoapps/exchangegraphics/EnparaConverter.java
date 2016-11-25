package demoapps.exchangegraphics;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import demoapps.exchangegraphics.data.EnparaRate;
import demoapps.exchangegraphics.data.Rate;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class EnparaConverter implements Converter<ResponseBody, List<Rate>> {

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

    private static final String HOST = "http://www.qnbfinansbank.enpara.com";

    private EnparaConverter() {
    }

    static final EnparaConverter INSTANCE = new EnparaConverter();


    @Override
    public List<Rate> convert(ResponseBody value) throws IOException {

        ArrayList<Rate> rates = new ArrayList<>();
        String responseBody = value != null ? value.string() : null;

        final Elements shotElements = Jsoup.parse(responseBody, HOST).select("#pnlContent span dl");
        for (Element element : shotElements) {
            Rate rate = parseRate(element);
            if (rate != null) {
                rates.add(rate);
            }
        }
        return rates;
    }

    private static EnparaRate parseRate(Element element) {
        final Elements divElements = element.select("div");
        EnparaRate rate = new EnparaRate();
        for (int i = 0; i < divElements.size(); i++) {
            Element innerElement = divElements.get(i);
            Node childNode = innerElement.childNodes().get(0);
            if (i == 0) {
                rate.type = childNode.toString();
            }
            if (i == 1) {
                rate.value_sell = childNode.childNodes().get(0).toString();
            }
            if (i == 2) {
                rate.value_buy = childNode.childNodes().get(0).toString();
            }
        }
        rate.rateType = rate.toRateType();
        rate.setRealValues();

        return rate;
    }


}