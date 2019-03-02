package dynoapps.exchange_rates.converters;

import android.text.TextUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import dynoapps.exchange_rates.model.rates.BaseRate;
import dynoapps.exchange_rates.model.rates.DolarTlKurRate;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Copyright 2016 Erdem OLKUN
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

public class DolarTlKurAjaxConverter implements Converter<ResponseBody, List<BaseRate>> {

    private static final DolarTlKurAjaxConverter INSTANCE = new DolarTlKurAjaxConverter();

    private DolarTlKurAjaxConverter() {
    }

    /**
     * Sample response body <p> <p> USDTRY:3.4560: EURTRY:3.6623: EURUSD:1.0595: XAUUSD:1187.34:
     * GBPTRY:4.2978: CHFTRY:3.4122: SARTRY:0.9216: 16:33:31 </p>
     **/
    @Override
    public List<BaseRate> convert(@NonNull ResponseBody value) throws IOException {

        ArrayList<BaseRate> rates = new ArrayList<>();
        String responseBody = value.string();
        if (!TextUtils.isEmpty(responseBody)) {
            String[] splitsMoney = responseBody.split("\n");
            if (splitsMoney.length > 0) {
                for (String singleSplit : splitsMoney) {
                    String[] splits = singleSplit.split(":"); // ysi Type not supported
                    if (splits.length == 2) {
                        DolarTlKurRate rate = new DolarTlKurRate();
                        rate.type = splits[0];
                        rate.avg_val = splits[1];
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