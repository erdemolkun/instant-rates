package dynoapps.exchange_rates.converters;

import android.text.TextUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import dynoapps.exchange_rates.data.DolarTlKurRate;
import dynoapps.exchange_rates.data.Rate;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class DolarTlKurAjaxConverter implements Converter<ResponseBody, List<Rate>> {

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

    private DolarTlKurAjaxConverter() {
    }

    static final DolarTlKurAjaxConverter INSTANCE = new DolarTlKurAjaxConverter();


    /**
     * Sample response body
     *
     * <p> USDTRY:3.4560: EURTRY:3.6623: EURUSD:1.0595: XAUUSD:1187.34: GBPTRY:4.2978: CHFTRY:3.4122: SARTRY:0.9216: 16:33:31
     * </p>
     **/
    @Override
    public List<Rate> convert(ResponseBody value) throws IOException {

        ArrayList<Rate> rates = new ArrayList<>();
        String responseBody = value != null ? value.string() : null;
        if (!TextUtils.isEmpty(responseBody)) {
            String[] splitsMoney = responseBody.split("\n");
            if (splitsMoney.length > 0) {
                for (String singleSplit : splitsMoney) {
                    String[] splits = singleSplit.split(":"); // ysi Type not supported
                    if (splits.length ==2) {
                        DolarTlKurRate rate = new DolarTlKurRate();
                        rate.type = splits[0];
                        rate.value = splits[1];
                        rate.rateType = rate.toRateType();
                        rate.setRealValues();
                        rates.add(rate);
                    }
                }
            }
        }
        return rates;
    }


}