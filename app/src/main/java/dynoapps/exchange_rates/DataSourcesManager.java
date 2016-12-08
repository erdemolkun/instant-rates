package dynoapps.exchange_rates;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import dynoapps.exchange_rates.data.CurrencySource;
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


    private static ArrayList<CurrencySource> currencySources = new ArrayList<>();
    private static ArrayList<BasePoolingDataProvider> providers;

    public static void init() {
        if (currencySources.size() > 0) return; // Already initialized
        initDataSourceSelections();
    }

    public static void updateProviders(ArrayList<BasePoolingDataProvider> providers) {

        DataSourcesManager.providers = providers;
        for (CurrencySource currencySource : currencySources) {
            switch (currencySource.getSourceType()) {
                case CurrencySource.Type.YORUMLAR:
                    currencySource.setPollingSource(CollectionUtils.getInstance(providers, YorumlarRateProvider.class));
                    break;
                case CurrencySource.Type.ENPARA:
                    currencySource.setPollingSource(CollectionUtils.getInstance(providers, EnparaRateProvider.class));
                    break;
                case CurrencySource.Type.BIGPARA:
                    currencySource.setPollingSource(CollectionUtils.getInstance(providers, BigparaRateProvider.class));
                    break;
                case CurrencySource.Type.TLKUR:
                    currencySource.setPollingSource(CollectionUtils.getInstance(providers, DolarTlKurRateProvider.class));
                    break;
                case CurrencySource.Type.YAPIKREDI:
                    currencySource.setPollingSource(CollectionUtils.getInstance(providers, YapıKrediRateProvider.class));
                    break;
            }
        }
    }

    // Boolean array for initial enabled items
    private static boolean[] temp_data_source_states;

    public static void selectSources(final Activity activity) {
        final ArrayList<CurrencySource> currencySources = DataSourcesManager.getCurrencySources();
        temp_data_source_states = new boolean[currencySources.size()];
        for (int i = 0; i < temp_data_source_states.length; i++) {
            temp_data_source_states[i] = currencySources.get(i).isEnabled();
        }
        String[] data_set_names = new String[currencySources.size()];
        for (int i = 0; i < currencySources.size(); i++) {
            data_set_names[i] = currencySources.get(i).getName();
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
                for (int i = 0; i < currencySources.size(); i++) {
                    currencySources.get(i).setEnabled(temp_data_source_states[i]);
                }
                EventBus.getDefault().post(new DataSourceUpdate());
                saveSources(currencySources);
            }
        });

        builder.setNegativeButton(R.string.dismiss, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static void saveSources(List<CurrencySource> currencySources) {
        String sources = "";
        for (int i = 0; i < currencySources.size(); i++) {
            CurrencySource currencySource = currencySources.get(i);
            if (currencySource.isEnabled()) {
                sources += currencySource.getSourceType() + ";";
            }
        }
        Prefs.saveSources(sources);
    }


    public static String getSourceName(int type) {
        for (CurrencySource dataSource : currencySources) {
            if (type == dataSource.getSourceType()) {
                return dataSource.getName();
            }
        }
        return "";
    }

    public static ArrayList<CurrencySource> getCurrencySources() {
        return currencySources;
    }

    public static CurrencySource getSource(int source_type){
        for (CurrencySource currencySource : currencySources){
            if (currencySource.getSourceType()==source_type)return currencySource;
        }
        return null;
    }

    private static void initDataSourceSelections() {

        if (currencySources != null && currencySources.size() > 0) return;

        currencySources.add(new CurrencySource("Yorumlar", CurrencySource.Type.YORUMLAR));
        currencySources.add(new CurrencySource("Enpara", CurrencySource.Type.ENPARA));
        currencySources.add(new CurrencySource("Bigpara", CurrencySource.Type.BIGPARA));
        currencySources.add(new CurrencySource("TlKur", CurrencySource.Type.TLKUR));
        currencySources.add(new CurrencySource("Yapı Kredi", CurrencySource.Type.YAPIKREDI));

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
                    for (CurrencySource currencySource : currencySources) {
                        if (currencySource.getSourceType() == sourceType) {
                            currencySource.setEnabled(true);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        } else {
            for (CurrencySource currencySource : currencySources) {
                currencySource.setEnabled(true);
            }
        }
    }

}
