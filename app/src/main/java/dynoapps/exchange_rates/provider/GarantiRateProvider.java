package dynoapps.exchange_rates.provider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dynoapps.exchange_rates.model.rates.GarantiRate;
import retrofit2.Call;

/**
 * Created by erdemmac on 25/11/2016.
 */

public class GarantiRateProvider extends BasePoolingDataProvider<List<GarantiRate>> {

    private Call<List<GarantiRate>> lastCall;
    ExecutorService executorService;

    public GarantiRateProvider(SourceCallback<List<GarantiRate>> callback) {
        super(callback);
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void cancel() {
        if (lastCall != null) {
            lastCall.cancel();
        }
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


//        final GarantiService garantiService = Api.getGarantiApi().create(GarantiService.class);
//        Call<List<GarantiRate>> call = garantiService.rates();
//        call.enqueue(new retrofit2.Callback<List<GarantiRate>>() {
//            @Override
//            public void onResponse(Call<List<GarantiRate>> call, Response<List<GarantiRate>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    List<GarantiRate> rates = response.body();
//                    notifyValue(rates);
//                    fetchAgain(false);
//                } else {
//                    notifyError();
//                    fetchAgain(true);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<GarantiRate>> call, Throwable t) {
//                notifyError();
//                fetchAgain(true);
//            }
//        });
//        lastCall = call;
    }

    private void jsoupCall() {
        try {
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
            notifyValue(rates);
            fetchAgain(false);
        } catch (Exception ex) {
            notifyError();
            fetchAgain(true);
        }
    }
}
