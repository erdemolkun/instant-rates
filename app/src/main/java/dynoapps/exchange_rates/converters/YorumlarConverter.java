package dynoapps.exchange_rates.converters;

import android.text.TextUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import dynoapps.exchange_rates.model.rates.BaseRate;
import dynoapps.exchange_rates.model.rates.YorumlarRate;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class YorumlarConverter implements Converter<ResponseBody, List<BaseRate>> {

    private static final YorumlarConverter INSTANCE = new YorumlarConverter();

    private YorumlarConverter() {
    }

    /**
     * Sample response body <p> <p> dolar_guncelle('3.4047','13:01:36');euro_guncelle('3.6012','13:01:36');sterlin_guncelle('4.2431','13:01:36');gumus_guncelle('1.7921','1.7941','13:01:36');parite_guncelle('1.0570','13:01:36');ons_guncelle('$1190.0000','13:01:36');ySi('[5164806,5388042,5387395,5387090]');
     * </p>
     **/
    @Override
    public List<BaseRate> convert(@NonNull ResponseBody value) throws IOException {

        ArrayList<BaseRate> rates = new ArrayList<>();
        String responseBody = value.string();
        if (!TextUtils.isEmpty(responseBody)) {
            String[] splitsMoney = responseBody.split(";");
            if (splitsMoney.length > 0) {
                for (String singleSplit : splitsMoney) {
                    String[] splits = singleSplit.split("[(),]"); // ysi Type not supported
                    if (splits.length > 2) {
                        YorumlarRate rate = new YorumlarRate();
                        rate.type = splits[0];
                        rate.avg_val = splits[1];
                        rate.time = splits[2];
                        rate.toRateType();
                        rate.setRealValues();
                        rates.add(rate);
                    }
                }
            }
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