package dynoapps.exchange_rates.converters;

import android.text.TextUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import dynoapps.exchange_rates.data.Rate;
import dynoapps.exchange_rates.data.YorumlarRate;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class YorumlarAjaxConverter implements Converter<ResponseBody, List<Rate>> {

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

    private YorumlarAjaxConverter() {
    }

    static final YorumlarAjaxConverter INSTANCE = new YorumlarAjaxConverter();


    /**
     * Sample response body
     *
     * <p> dolar_guncelle('3.4047','13:01:36');euro_guncelle('3.6012','13:01:36');sterlin_guncelle('4.2431','13:01:36');gumus_guncelle('1.7921','1.7941','13:01:36');parite_guncelle('1.0570','13:01:36');ons_guncelle('$1190.0000','13:01:36');ySi('[5164806,5388042,5387395,5387090]');
     * </p>
     **/
    @Override
    public List<Rate> convert(ResponseBody value) throws IOException {

        ArrayList<Rate> rates = new ArrayList<>();
        String responseBody = value != null ? value.string() : null;
        if (!TextUtils.isEmpty(responseBody)) {
            String[] splitsMoney = responseBody.split(";");
            if (splitsMoney.length > 0) {
                for (String singleSplit : splitsMoney) {
                    String[] splits = singleSplit.split("\\(|\\)|,"); // ysi Type not supported
                    if (splits.length > 2) {
                        YorumlarRate rate = new YorumlarRate();
                        rate.type = splits[0];
                        rate.value = splits[1];
                        rate.time = splits[2];
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