package dynoapps.exchange_rates.converters

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/** Retrofit factory that decodes every response body with [converter]. */
class SingleConverterFactory(
    private val converter: Converter<ResponseBody, *>,
) : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *> = converter
}

/** Splits like Java's [java.lang.String.split]: trailing empty strings are dropped. */
internal fun String.splitDroppingTrailingEmpty(regex: Regex): List<String> =
    split(regex).dropLastWhile { it.isEmpty() }
