package dynoapps.exchange_rates;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import dynoapps.exchange_rates.data.CurrencySource;
import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.event.DataSourceUpdate;
import dynoapps.exchange_rates.interfaces.ValueType;
import dynoapps.exchange_rates.provider.BasePoolingDataProvider;
import dynoapps.exchange_rates.provider.BigparaRateProvider;
import dynoapps.exchange_rates.provider.DolarTlKurRateProvider;
import dynoapps.exchange_rates.provider.EnparaRateProvider;
import dynoapps.exchange_rates.provider.YahooRateProvider;
import dynoapps.exchange_rates.provider.YapıKrediRateProvider;
import dynoapps.exchange_rates.provider.YorumlarRateProvider;
import dynoapps.exchange_rates.util.CollectionUtils;

/**
 * Created by erdemmac on 05/12/2016.
 */

public class SourcesManager {

    private static ArrayList<CurrencySource> currencySources = new ArrayList<>();

    public static void init() {
        if (currencySources.size() > 0) return; // Already initialized
        initDataSourceSelections();
    }

    public static void updateProviders(ArrayList<BasePoolingDataProvider> providers) {

        for (CurrencySource source : currencySources) {
            switch (source.getSourceType()) {
                case CurrencyType.YORUMLAR:
                    source.setPollingSource(CollectionUtils.getInstance(providers, YorumlarRateProvider.class));
                    break;
                case CurrencyType.ENPARA:
                    source.setPollingSource(CollectionUtils.getInstance(providers, EnparaRateProvider.class));
                    break;
                case CurrencyType.BIGPARA:
                    source.setPollingSource(CollectionUtils.getInstance(providers, BigparaRateProvider.class));
                    break;
                case CurrencyType.TLKUR:
                    source.setPollingSource(CollectionUtils.getInstance(providers, DolarTlKurRateProvider.class));
                    break;
                case CurrencyType.YAPIKREDI:
                    source.setPollingSource(CollectionUtils.getInstance(providers, YapıKrediRateProvider.class));
                    break;
                case CurrencyType.YAHOO:
                    source.setPollingSource(CollectionUtils.getInstance(providers, YahooRateProvider.class));
                    break;
            }
        }
    }

    // Boolean array for initial enabled items
    private static boolean[] temp_data_source_states;

    public static void selectSources(final Activity activity) {
        final ArrayList<CurrencySource> currencySources = SourcesManager.getCurrencySources();
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
        builder.setPositiveButton(R.string.apply, null);
        builder.setNegativeButton(R.string.dismiss, null);

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        int enabled_count = 0;
                        for (int i = 0; i < currencySources.size(); i++) {
                            if (temp_data_source_states[i]) enabled_count++;
                        }
                        if (enabled_count == 0) {
                            Toast.makeText(activity, R.string.select_at_least_one_source, Toast.LENGTH_SHORT).show();
                        } else {
                            for (int i = 0; i < currencySources.size(); i++) {
                                currencySources.get(i).setEnabled(temp_data_source_states[i]);
                            }
                            EventBus.getDefault().post(new DataSourceUpdate());
                            saveSources(currencySources);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
        dialog.getWindow().setWindowAnimations(R.style.DialogAnimationFade);
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


    /**
     * @param value_type is one of {@link ValueType}
     */
    public static String getSourceName(int type, int value_type) {
        String postFix = "";
        if (value_type == ValueType.SELL) {
            postFix = " " + App.context().getString(R.string.sell);
        } else if (value_type == ValueType.BUY) {
            postFix = " " + App.context().getString(R.string.buy);
        }
        return getSourceName(type) + postFix;
    }

    public static String getSourceName(int type) {
        for (CurrencySource source : currencySources) {
            if (type == source.getSourceType()) {
                return source.getName();
            }
        }
        return "";
    }

    public static ArrayList<CurrencySource> getCurrencySources() {
        return currencySources;
    }

    public static CurrencySource getSource(int source_type) {
        for (CurrencySource currencySource : currencySources) {
            if (currencySource.getSourceType() == source_type) return currencySource;
        }
        return null;
    }

    private static void initDataSourceSelections() {

        if (currencySources != null && currencySources.size() > 0) return;

        currencySources.add(new CurrencySource("Yorumlar", CurrencyType.YORUMLAR, R.color.colorYorumlar, true));
        currencySources.add(new CurrencySource("Enpara", CurrencyType.ENPARA, R.color.colorEnpara, true));
        currencySources.add(new CurrencySource("Bigpara", CurrencyType.BIGPARA, R.color.colorBigPara, false));
        currencySources.add(new CurrencySource("TlKur", CurrencyType.TLKUR, R.color.colorDolarTlKur, false));
        currencySources.add(new CurrencySource("Yapı Kredi", CurrencyType.YAPIKREDI, R.color.colorYapıKredi, false));
        currencySources.add(new CurrencySource("Yahoo", CurrencyType.YAHOO, R.color.colorYahoo, false));

        updateSourceStatesFromPrefs();
    }

    private static void updateSourceStatesFromPrefs() {
        String sources = Prefs.getSources();
        if (!TextUtils.isEmpty(sources)) {
            String[] splits = sources.split(";");
            for (CurrencySource currencySource : currencySources) {
                currencySource.setEnabled(false);
            }
            for (String str : splits) {
                if (TextUtils.isEmpty(str)) continue;
                int source_type_temp;
                try {
                    source_type_temp = Integer.parseInt(str);
                    for (CurrencySource currencySource : currencySources) {
                        if (currencySource.getSourceType() == source_type_temp) {
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
