package dynoapps.exchange_rates;

import android.text.TextUtils;

import java.util.ArrayList;

import dynoapps.exchange_rates.data.RateDataSource;
import dynoapps.exchange_rates.provider.BasePoolingDataProvider;
import dynoapps.exchange_rates.provider.BigparaRateProvider;
import dynoapps.exchange_rates.provider.DolarTlKurRateProvider;
import dynoapps.exchange_rates.provider.EnparaRateProvider;
import dynoapps.exchange_rates.provider.YapıKrediRateProvider;
import dynoapps.exchange_rates.provider.YorumlarRateProvider;
import dynoapps.exchange_rates.util.CollectionUtils;

/**
 * Created by erdemmac on 05/12/2016.
 */

public class DataSourcesManager {

    private static ArrayList<RateDataSource> rateDataSources = new ArrayList<>();
    private static ArrayList<BasePoolingDataProvider> providers;

    public static void init() {
        if (rateDataSources.size() > 0) return; // Already initalized
        initDataSourceSelections();
    }

    public static void updateProviders(ArrayList<BasePoolingDataProvider> providers) {
        
        DataSourcesManager.providers = providers;
        for (RateDataSource rateDataSource : rateDataSources) {
            switch (rateDataSource.getSourceType()) {
                case RateDataSource.Type.YORUMLAR:
                    rateDataSource.setPollingSource(CollectionUtils.getInstance(providers, YorumlarRateProvider.class));
                    break;
                case RateDataSource.Type.ENPARA:
                    rateDataSource.setPollingSource(CollectionUtils.getInstance(providers, EnparaRateProvider.class));
                    break;
                case RateDataSource.Type.BIGPARA:
                    rateDataSource.setPollingSource(CollectionUtils.getInstance(providers, BigparaRateProvider.class));
                    break;
                case RateDataSource.Type.TLKUR:
                    rateDataSource.setPollingSource(CollectionUtils.getInstance(providers, DolarTlKurRateProvider.class));
                    break;
                case RateDataSource.Type.YAPIKREDI:
                    rateDataSource.setPollingSource(CollectionUtils.getInstance(providers, YapıKrediRateProvider.class));
                    break;
            }
        }
    }

    public static ArrayList<RateDataSource> getRateDataSources() {
        return rateDataSources;
    }

    private static void initDataSourceSelections() {

        rateDataSources.add(new RateDataSource("Yorumlar", RateDataSource.Type.YORUMLAR));
        rateDataSources.add(new RateDataSource("Enpara", RateDataSource.Type.ENPARA));
        rateDataSources.add(new RateDataSource("Bigpara", RateDataSource.Type.BIGPARA));
        rateDataSources.add(new RateDataSource("TlKur", RateDataSource.Type.TLKUR));
        rateDataSources.add(new RateDataSource("Yapı Kredi", RateDataSource.Type.YAPIKREDI));

        updateSourceStatesFromPrefs();
    }

    private static void updateSourceStatesFromPrefs() {
        String sources = Prefs.getSources(App.context());
        if (!TextUtils.isEmpty(sources)) {
            String[] splits = sources.split(";");
            for (String str : splits) {
                if (TextUtils.isEmpty(str)) continue;
                int sourceType;
                try {
                    sourceType = Integer.parseInt(str);
                    for (RateDataSource rateDataSource : rateDataSources) {
                        if (rateDataSource.getSourceType() == sourceType) {
                            rateDataSource.setEnabled(true);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        } else {
            for (RateDataSource rateDataSource : rateDataSources) {
                rateDataSource.setEnabled(true);
            }
        }
    }

}
