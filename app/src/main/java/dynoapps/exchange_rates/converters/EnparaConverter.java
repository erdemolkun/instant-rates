package dynoapps.exchange_rates.converters;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import dynoapps.exchange_rates.model.rates.BaseRate;
import dynoapps.exchange_rates.model.rates.EnparaRate;
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
 *
 * Created by erdemmac on 24/11/2016.
 */

public class EnparaConverter implements Converter<ResponseBody, List<BaseRate>> {

    private static final EnparaConverter INSTANCE = new EnparaConverter();
    private static final String HOST = "https://www.qnbfinansbank.enpara.com";

    private EnparaConverter() {
    }

    @Override
    public List<BaseRate> convert(@NonNull ResponseBody value) throws IOException {

        ArrayList<BaseRate> rates = new ArrayList<>();
        String responseBody = value.string();

        final Elements shotElements = Jsoup.parse(responseBody, HOST).select("#pnlContent span dl");
        for (Element element : shotElements) {
            BaseRate rate = parseRate(element);
            if (rate != null) {
                rates.add(rate);
            }
        }
        return rates;
    }

    private EnparaRate parseRate(Element element) {
        final Elements divElements = element.select("div");
        EnparaRate rate = new EnparaRate();
        if (divElements.size() > 2) {
            rate.type = divElements.get(0).text();
            rate.value_buy = divElements.get(1).text();
            rate.value_sell = divElements.get(2).text();
        }
        rate.toRateType();
        rate.setRealValues();

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