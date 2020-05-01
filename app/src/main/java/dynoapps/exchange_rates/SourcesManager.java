package dynoapps.exchange_rates;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import dynoapps.exchange_rates.data.CurrencySource;
import dynoapps.exchange_rates.data.CurrencyType;
import dynoapps.exchange_rates.interfaces.ValueType;
import dynoapps.exchange_rates.model.rates.IRate;
import dynoapps.exchange_rates.provider.BasePoolingProvider;
import dynoapps.exchange_rates.provider.BigparaRateProvider;
import dynoapps.exchange_rates.provider.BloombergRateProvider;
import dynoapps.exchange_rates.provider.DolarTlKurRateProvider;
import dynoapps.exchange_rates.provider.EnparaRateProvider;
import dynoapps.exchange_rates.provider.ParaGarantiRateProvider;
import dynoapps.exchange_rates.provider.YahooRateProvider;
import dynoapps.exchange_rates.provider.YapıKrediRateProvider;
import dynoapps.exchange_rates.provider.YorumlarRateProvider;
import dynoapps.exchange_rates.util.CollectionUtils;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by erdemmac on 05/12/2016.
 */

public class SourcesManager {

    private static ArrayList<CurrencySource> currencySources = null;
    // Boolean array for initial enabled items
    private static boolean[] temp_data_source_states;

    private static PublishSubject<Boolean> sourceUpdates = PublishSubject.create();

    private static void init() {
        if (CollectionUtils.size(currencySources) > 0) return; // Already initialized
        initDataSourceSelections();
    }

    public static PublishSubject<Boolean> getSourceUpdates() {
        return sourceUpdates;
    }

    public static void updateProviders(List<BasePoolingProvider<?>> providers) {

        for (CurrencySource source : getCurrencySources()) {
            switch (source.getType()) {
                case CurrencyType.PARAGARANTI:
                    source.setPollingSource(CollectionUtils.getInstance(providers, ParaGarantiRateProvider.class));
                    break;
                case CurrencyType.ALTININ:
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
                case CurrencyType.BLOOMBERGHT:
                    source.setPollingSource(CollectionUtils.getInstance(providers, BloombergRateProvider.class));
                    break;
            }
        }
    }

    public static void selectSources(final Activity activity) {
        final ArrayList<CurrencySource> currencySources = getCurrencySources();
        temp_data_source_states = new boolean[currencySources.size()];
        for (int i = 0; i < temp_data_source_states.length; i++) {
            temp_data_source_states[i] = currencySources.get(i).isEnabled();
        }
        String[] data_set_names = new String[currencySources.size()];
        for (int i = 0; i < currencySources.size(); i++) {
            data_set_names[i] = currencySources.get(i).getName();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppTheme_Alert);

        builder.setMultiChoiceItems(data_set_names, temp_data_source_states, (dialog, which, isChecked) -> temp_data_source_states[which] = isChecked);

        builder.setCancelable(true);
        builder.setTitle(R.string.select_sources);
        builder.setPositiveButton(R.string.apply, null);
        builder.setNegativeButton(R.string.dismiss, null);

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {

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
                    sourceUpdates.onNext(true);
                    saveSources(currencySources);
                    dialog.dismiss();
                }
            });
        });
        dialog.getWindow().setWindowAnimations(R.style.DialogAnimationFade);
        dialog.show();
    }

    private static void saveSources(List<CurrencySource> currencySources) {
        StringBuilder sources = new StringBuilder();
        for (int i = 0; i < currencySources.size(); i++) {
            CurrencySource currencySource = currencySources.get(i);
            if (currencySource.isEnabled()) {
                sources.append(currencySource.getType()).append(";");
            }
        }
        Prefs.saveSources(sources.toString());
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
        for (CurrencySource source : getCurrencySources()) {
            if (type == source.getType()) {
                return source.getName();
            }
        }
        return "";
    }

    public static ArrayList<CurrencySource> getCurrencySources() {
        if (currencySources == null) {
            init();
        }
        return currencySources;
    }

    public static CurrencySource getSource(int source_type) {
        for (CurrencySource currencySource : getCurrencySources()) {
            if (currencySource.getType() == source_type) return currencySource;
        }
        return null;
    }

    @ColorInt
    private static int color(@ColorRes int colorRes) {
        return ContextCompat.getColor(App.context(), colorRes);
    }

    private static void initDataSourceSelections() {

        if (currencySources != null) {
            return;
        }
        /*
         * Initialize once.
         **/

        int[] only_usd_try = {IRate.USD};
        int[] altinInSupported = {IRate.USD, IRate.EUR, IRate.EUR_USD, IRate.ONS};
        int[] paragarantiSupported = {IRate.USD, IRate.EUR, IRate.EUR_USD};
        int[] enparaSupported = {IRate.USD, IRate.EUR, IRate.EUR_USD, IRate.ONS_TRY};
        int[] yapikrediSupported = {IRate.USD, IRate.EUR, IRate.ONS_TRY};
        int[] bloombergSupported = {IRate.USD, IRate.EUR, IRate.EUR_USD};
        currencySources = new ArrayList<>();
        currencySources.add(new CurrencySource("Altın.in", CurrencyType.ALTININ, color(R.color.colorYorumlar), true, altinInSupported));
        currencySources.add(new CurrencySource("Enpara", CurrencyType.ENPARA, color(R.color.colorEnpara), false, enparaSupported));
        currencySources.add(new CurrencySource("Bigpara", CurrencyType.BIGPARA, color(R.color.colorBigPara), false, only_usd_try));
        currencySources.add(new CurrencySource("TlKur", CurrencyType.TLKUR, color(R.color.colorDolarTlKur), false, altinInSupported));
        currencySources.add(new CurrencySource("Yapı Kredi", CurrencyType.YAPIKREDI, color(R.color.colorYapiKredi), false, yapikrediSupported));
        currencySources.add(new CurrencySource("Yahoo", CurrencyType.YAHOO, color(R.color.colorYahoo), false, altinInSupported));
        currencySources.add(new CurrencySource("Paragaranti", CurrencyType.PARAGARANTI, color(R.color.colorParagaranti), false, paragarantiSupported)); // update supported ones
        currencySources.add(new CurrencySource("Bloomberg HT", CurrencyType.BLOOMBERGHT, color(R.color.colorBloomberg), true, bloombergSupported));
        int index = 0;
        for (CurrencySource source : currencySources) {
            source.setChartIndex(index);
            index += source.isAvgType() ? 1 : 2;
        }
        updateSourceStatesFromPrefs();
    }

    public static void update() {
        initDataSourceSelections();
        updateSourceStatesFromPrefs();
    }

    private static void updateSourceStatesFromPrefs() {
        String sources = Prefs.getSources();
        if (!TextUtils.isEmpty(sources)) {
            String[] splits = sources.split(";");
            for (CurrencySource currencySource : getCurrencySources()) {
                currencySource.setEnabled(false);
            }
            for (String str : splits) {
                if (TextUtils.isEmpty(str)) continue;
                int source_type_temp;
                try {
                    source_type_temp = Integer.parseInt(str);
                    for (CurrencySource currencySource : getCurrencySources()) {
                        if (currencySource.getType() == source_type_temp) {
                            currencySource.setEnabled(true);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }
}
