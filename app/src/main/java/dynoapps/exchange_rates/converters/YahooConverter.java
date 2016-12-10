package dynoapps.exchange_rates.converters;

import android.text.TextUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import dynoapps.exchange_rates.model.rates.BaseRate;
import dynoapps.exchange_rates.model.rates.YahooRate;
import dynoapps.exchange_rates.model.rates.YorumlarRate;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class YahooConverter implements Converter<ResponseBody, List<BaseRate>> {

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

    private YahooConverter() {
    }

    static final YahooConverter INSTANCE = new YahooConverter();


    /**
     * Sample response body
     *
     * <p> "USDTRY=X",3.4837,"12/9/2016","11:45pm"
           "EURTRY=X",3.6767,"12/9/2016","10:30pm"
     * </p>
     **/
    @Override
    public List<BaseRate> convert(ResponseBody value) throws IOException {

        ArrayList<BaseRate> rates = new ArrayList<>();
        String responseBody = value != null ? value.string() : null;
        if (!TextUtils.isEmpty(responseBody)) {
            String[] splitsMoney = responseBody.split("\n");
            if (splitsMoney.length > 0) {
                for (String singleSplit : splitsMoney) {
                    String[] splits = singleSplit.split(","); // ysi Type not supported
                    if (splits.length > 2) {
                        YahooRate rate = new YahooRate();
                        rate.type = splits[0].replace("\"","");
                        rate.avg_val = splits[1];
                        //rate.time = splits[2];
                        rate.toRateType();
                        rate.setRealValues();
                        rates.add(rate);
                    }
                }
            }
        }
        return rates;
    }


}