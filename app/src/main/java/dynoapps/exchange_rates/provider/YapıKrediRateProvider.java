package dynoapps.exchange_rates.provider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dynoapps.exchange_rates.data.YapıKrediRate;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class YapıKrediRateProvider extends BasePoolingDataProvider<List<YapıKrediRate>> implements IPollingSource {

    ExecutorService executorService;

    public YapıKrediRateProvider(SourceCallback<List<YapıKrediRate>> callback) {
        super(callback);
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void cancel() {

    }

    @Override
    public void run() {
        super.run();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                jsoupCall();
            }
        });
    }

    private void jsoupCall() {
        try {
            Document doc = Jsoup.connect("https://www.yapikredi.com.tr/yatirimci-kosesi/doviz-bilgileri.aspx/LoadInternetCurrencies")
                    .header("Access-Control-Allow-Origin", "*")
                    .referrer("https://www.yapikredi.com.tr/yatirimci-kosesi/doviz-bilgileri.aspx?section=internet")
                    .timeout(6000)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.98 Safari/537.36")
                    .get();
            Elements elements = doc.select("#currencyResultContent").select("tr");
            ArrayList<YapıKrediRate> rates = new ArrayList<>();
            if (elements != null) {
                for (Element element : elements) {
                    Elements innerElements = element.select("td");
                    YapıKrediRate rate = new YapıKrediRate();
                    rate.value_sell = innerElements.get(2).text();
                    rate.value_buy = innerElements.get(3).text();
                    rate.type = innerElements.get(0).text();
                    rate.rateType = rate.toRateType();
                    rate.setRealValues();
                    rates.add(rate);
                }
            }
            notifyValue(rates);
            fetchAgain(false);
        } catch (Exception ex) {
            notifyError();
            fetchAgain(true);
        }
    }
}
