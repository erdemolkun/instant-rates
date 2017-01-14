package dynoapps.exchange_rates.data;

import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

import dynoapps.exchange_rates.App;
import dynoapps.exchange_rates.provider.IPollingSource;

/**
 * Created by erdemmac on 01/12/2016.
 */

public class CurrencySource {
    private IPollingSource iPollingSource;
    private String name;
    private boolean enabled;
    private int source_type;
    private int color;
    private int[] supported_rates = new int[0];
    private int chartIndex = 0;

    public CurrencySource(String name, int source_type, @ColorRes int colorRes, boolean enabled, int[] supported_rates) {
        this.name = name;
        this.source_type = source_type;
        this.enabled = enabled;
        this.color = ContextCompat.getColor(App.context(), colorRes);
        this.supported_rates = supported_rates;
    }

    public int getChartIndex() {
        return chartIndex;
    }

    public void setChartIndex(int chartIndex) {
        this.chartIndex = chartIndex;
    }

    public int[] getSupportedRates() {
        return supported_rates;
    }

    public int getColor() {
        return color;
    }

    public int getSourceType() {
        return source_type;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setPollingSource(IPollingSource iPollingSource) {
        this.iPollingSource = iPollingSource;
    }

    @Override
    public String toString() {
        return getName();
    }

    public IPollingSource getPollingSource() {
        return iPollingSource;
    }

    public boolean isAvgType() {
        return source_type == CurrencyType.ALTININ || source_type == CurrencyType.PARAGARANTI ||
                source_type == CurrencyType.TLKUR || source_type == CurrencyType.YAHOO;
    }

    public boolean isRateSupported(int rateType) {
        if (supported_rates == null) return true;
        if (supported_rates.length <= 0) return true;
        for (int i : supported_rates) {
            if (rateType == i) {
                return true;
            }
        }
        return false;
    }
}