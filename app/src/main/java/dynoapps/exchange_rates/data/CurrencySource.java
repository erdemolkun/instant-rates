package dynoapps.exchange_rates.data;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import dynoapps.exchange_rates.App;
import dynoapps.exchange_rates.provider.IPollingSource;

/**
 * Created by erdemmac on 01/12/2016.
 */

public class CurrencySource {
    private IPollingSource iPollingSource;
    private String name;
    private boolean enabled;
    private int type;
    private int color;
    private int[] supported_rates = new int[0];
    private int chartIndex = 0;

    public CurrencySource(String name, int type, @ColorInt int colorInt, boolean enabled, int[] supported_rates) {
        this.name = name;
        this.type = type;
        this.enabled = enabled;
        this.color = colorInt;
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

    public int getType() {
        return type;
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

    @Override
    @NonNull
    public String toString() {
        return getName();
    }

    public IPollingSource getPollingSource() {
        return iPollingSource;
    }

    public void setPollingSource(IPollingSource iPollingSource) {
        this.iPollingSource = iPollingSource;
    }

    public boolean isAvgType() {
        return type == CurrencyType.ALTININ || type == CurrencyType.PARAGARANTI ||
                type == CurrencyType.TLKUR || type == CurrencyType.YAHOO | type == CurrencyType.BLOOMBERGHT;
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