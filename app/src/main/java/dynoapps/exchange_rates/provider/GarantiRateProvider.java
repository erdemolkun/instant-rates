package dynoapps.exchange_rates.provider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.model.rates.GarantiRate;
import io.reactivex.Observable;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class GarantiRateProvider extends BasePoolingProvider<List<GarantiRate>> {

    public GarantiRateProvider(SourceCallback<List<GarantiRate>> callback) {
        super(callback);
    }

    @Override
    protected Observable<List<GarantiRate>> getObservable() {
        return Observable.fromCallable(new Callable<List<GarantiRate>>() {
            @Override
            public List<GarantiRate> call() throws Exception {
                Document doc = Jsoup.connect("https://www.garanti.com.tr/tr")
                        .header("Access-Control-Allow-Origin", "*")
                        .referrer("http://www.google.com")
                        .timeout(6000)
                        .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.98 Safari/537.36")
                        .get();
                Elements elements = doc.select(".rightSideContainer").select("#tab10").select("tbody").select("tr");
                ArrayList<GarantiRate> rates = new ArrayList<>();
                if (elements != null) {
                    for (Element element : elements) {
                        GarantiRate rate = new GarantiRate();
                        rate.avg_val = element.child(2).text();
                        rate.type = element.child(0).text();
                        rate.toRateType();
                        rate.setRealValues();
                        rates.add(rate);
                    }
                }
                return rates;
            }
        });
    }

    @Override
    public int getSourceType() {
        return CurrencyType.NOT_SET;//TODO
    }

}
