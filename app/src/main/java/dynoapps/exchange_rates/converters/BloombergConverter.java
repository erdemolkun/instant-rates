package dynoapps.exchange_rates.converters;

import android.provider.Telephony;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import dynoapps.exchange_rates.data.RatesHolder;
import dynoapps.exchange_rates.model.rates.BaseRate;
import dynoapps.exchange_rates.model.rates.BloombergRate;
import dynoapps.exchange_rates.model.rates.EnparaRate;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class BloombergConverter implements Converter<ResponseBody, List<BaseRate>> {

    private static final BloombergConverter INSTANCE = new BloombergConverter();
    private static final String HOST = "https://www.bloomberght.com";

    private BloombergConverter() {
    }


    @Override
    public List<BaseRate> convert(@NonNull ResponseBody value)  throws IOException {
        ArrayList<BaseRate> rates = new ArrayList<>();
        String responseBody = value.string();

        final Elements shotElements = Jsoup.parse(responseBody, HOST).getElementsByClass("data-info");
        for (Element element : shotElements) {
            BaseRate rate = parseRate(element);
            rates.add(rate);
        }
        return rates;
    }


    private BloombergRate parseRate(Element element) {
        final Elements lastPriceElement = element.getElementsByClass("value LastPrice");

        BloombergRate rate = new BloombergRate();
        rate.type = lastPriceElement.attr("data-secid");
        Element elementChild = lastPriceElement.get(0);

        rate.avg_val = elementChild.textNodes().get(0).getWholeText();
        return rate;
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
