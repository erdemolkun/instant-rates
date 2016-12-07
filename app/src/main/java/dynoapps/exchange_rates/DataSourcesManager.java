package dynoapps.exchange_rates;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import dynoapps.exchange_rates.data.RateDataSource;
import dynoapps.exchange_rates.event.DataSourceUpdate;
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


    interface SelectionCallback {
        void onDone();
    }

    private static ArrayList<RateDataSource> rateDataSources = new ArrayList<>();
    private static ArrayList<BasePoolingDataProvider> providers;

    public static void init() {
        if (rateDataSources.size() > 0) return; // Already initialized
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

    // Boolean array for initial enabled items
    private static boolean[] temp_data_source_states;

    public static void selectSources(final Activity activity) {
        final ArrayList<RateDataSource> rateDataSources = DataSourcesManager.getRateDataSources();
        temp_data_source_states = new boolean[rateDataSources.size()];
        for (int i = 0; i < temp_data_source_states.length; i++) {
            temp_data_source_states[i] = rateDataSources.get(i).isEnabled();
        }
        String[] data_set_names = new String[rateDataSources.size()];
        for (int i = 0; i < rateDataSources.size(); i++) {
            data_set_names[i] = rateDataSources.get(i).getName();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMultiChoiceItems(data_set_names, temp_data_source_states, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                temp_data_source_states[which] = isChecked;
            }
        });

        builder.setCancelable(true);
        builder.setTitle(R.string.select_sources);
        builder.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < rateDataSources.size(); i++) {
                    rateDataSources.get(i).setEnabled(temp_data_source_states[i]);
                }
                EventBus.getDefault().post(new DataSourceUpdate());
                saveSources(rateDataSources);
                if (activity instanceof SelectionCallback) {
                    ((SelectionCallback) activity).onDone();
                }
            }
        });

        builder.setNegativeButton(R.string.dismiss, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static void saveSources(List<RateDataSource> rateDataSources) {
        String sources = "";
        for (int i = 0; i < rateDataSources.size(); i++) {
            RateDataSource rateDataSource = rateDataSources.get(i);
            if (rateDataSource.isEnabled()) {
                sources += rateDataSource.getSourceType() + ";";
            }
        }
        Prefs.saveSources(sources);
    }


    public static String getSourceName(int type) {
        for (RateDataSource dataSource : rateDataSources) {
            if (type == dataSource.getSourceType()) {
                return dataSource.getName();
            }
        }
        return "";
    }

    public static ArrayList<RateDataSource> getRateDataSources() {
        return rateDataSources;
    }

    private static void initDataSourceSelections() {

        if (rateDataSources != null && rateDataSources.size() > 0) return;

        rateDataSources.add(new RateDataSource("Yorumlar", RateDataSource.Type.YORUMLAR));
        rateDataSources.add(new RateDataSource("Enpara", RateDataSource.Type.ENPARA));
        rateDataSources.add(new RateDataSource("Bigpara", RateDataSource.Type.BIGPARA));
        rateDataSources.add(new RateDataSource("TlKur", RateDataSource.Type.TLKUR));
        rateDataSources.add(new RateDataSource("Yapı Kredi", RateDataSource.Type.YAPIKREDI));

        updateSourceStatesFromPrefs();
    }

    private static void updateSourceStatesFromPrefs() {
        String sources = Prefs.getSources();
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
