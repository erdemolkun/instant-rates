package dynoapps.exchange_rates.provider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.rates.YapıKrediRate;
import io.reactivex.Observable;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class YapıKrediRateProvider extends BasePoolingProvider<List<YapıKrediRate>> {

    public YapıKrediRateProvider(SourceCallback<List<YapıKrediRate>> callback) {
        super(callback);
    }

    @Override
    protected Observable<List<YapıKrediRate>> getObservable() {
        return Observable.fromCallable(new Callable<List<YapıKrediRate>>() {
            @Override
            public List<YapıKrediRate> call() throws Exception {
                Document doc = Jsoup.connect("https://www.yapikredi.com.tr/yatirimci-kosesi/doviz-bilgileri.aspx/LoadInternetCurrencies")
                        .header("Access-Control-Allow-Origin", "*")
                        .referrer("https://www.yapikredi.com.tr/yatirimci-kosesi/doviz-bilgileri.aspx?section=internet")
                        .timeout(6000)
                        .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.98 Safari/537.36")
                        .get();
                Elements elements = doc.select("#currencyResultContent").select("tr");
                ArrayList<YapıKrediRate> rates = new ArrayList<>();
                if (elements != null) {
                    try {
                        for (Element element : elements) {
                            Elements innerElements = element.select("td");
                            YapıKrediRate rate = new YapıKrediRate();
                            if (innerElements.size() > 3) {
                                rate.value_sell = innerElements.get(2).text();
                                rate.value_buy = innerElements.get(3).text();
                                rate.type = innerElements.get(0).text();
                                rate.toRateType();
                                rate.setRealValues();
                                rates.add(rate);
                            }
                        }
                    } catch (Exception ignored) {

                    }
                }
                return rates;
            }
        });
    }

    @Override
    public int getSourceType() {
        return CurrencyType.YAPIKREDI;
    }

}
