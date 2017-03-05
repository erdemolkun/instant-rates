package dynoapps.exchange_rates.converters;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class StringResponseConverter implements Converter<ResponseBody, String> {

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

    private StringResponseConverter() {
    }

    static final StringResponseConverter INSTANCE = new StringResponseConverter();


    @Override
    public String convert(ResponseBody value) throws IOException {

        return value != null ? value.string() : null;

    }


}