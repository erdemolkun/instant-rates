package dynoapps.exchange_rates.converters;

import org.junit.Test;

import java.util.List;

import dynoapps.exchange_rates.model.rates.BaseRate;
import dynoapps.exchange_rates.model.rates.EnparaRate;
import dynoapps.exchange_rates.model.rates.IRate;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Converter;

import static org.junit.Assert.assertEquals;

public class EnparaConverterTest {

    private static final String HTML = "<html><body>"
            + "<div class=\"enpara-gold-exchange-rates__table-item\">"
            + "<div><span>USD ($)</span></div><div><span>41,8500 TL</span></div><div><span>42,1500 TL</span></div>"
            + "</div>"
            + "<div class=\"enpara-gold-exchange-rates__table-item\">"
            + "<div><span>Altın (gram)</span></div><div><span>4.512,30 TL</span></div><div><span>4.590,10 TL</span></div>"
            + "</div>"
            // Header / malformed row without values must be skipped instead of producing a null type.
            + "<div class=\"enpara-gold-exchange-rates__table-item\"><div><span>Döviz</span></div></div>"
            + "</body></html>";

    @SuppressWarnings("unchecked")
    @Test
    public void convert_parsesRowsAndSkipsMalformedOnes() throws Exception {
        Converter<ResponseBody, ?> converter = new EnparaConverter.Factory()
                .responseBodyConverter(List.class, new java.lang.annotation.Annotation[0], null);
        List<BaseRate> rates = (List<BaseRate>) converter.convert(
                ResponseBody.create(HTML, MediaType.get("text/html")));

        assertEquals(2, rates.size());

        EnparaRate usd = (EnparaRate) rates.get(0);
        usd.toRateType();
        usd.setRealValues();
        assertEquals(IRate.USD, usd.getRateType());
        assertEquals(41.85f, usd.valueBuyReal, 0.0001f);
        assertEquals(42.15f, usd.valueSellReal, 0.0001f);

        EnparaRate gold = (EnparaRate) rates.get(1);
        gold.toRateType();
        gold.setRealValues();
        assertEquals(IRate.ONS_TRY, gold.getRateType());
        assertEquals(4512.30f, gold.valueBuyReal, 0.01f);
        assertEquals(4590.10f, gold.valueSellReal, 0.01f);
    }
}
