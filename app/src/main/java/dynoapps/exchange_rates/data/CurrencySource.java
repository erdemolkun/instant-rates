package dynoapps.exchange_rates.data;

import androidx.annotation.ColorInt;

/**
 * Created by erdemmac on 01/12/2016.
 */

public class CurrencySource {
    private final String name;
    private boolean enabled;
    private final int type;
    private final int color;
    private final int[] supportedRates;
    private int chartIndex = 0;

    public CurrencySource(String name, int type, @ColorInt int colorInt, boolean enabled, int[] supportedRates) {
        this.name = name;
        this.type = type;
        this.enabled = enabled;
        this.color = colorInt;
        this.supportedRates = supportedRates;
    }

    public int getChartIndex() {
        return chartIndex;
    }

    public void setChartIndex(int chartIndex) {
        this.chartIndex = chartIndex;
    }

    public int[] getSupportedRates() {
        return supportedRates;
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
    public String toString() {
        return getName();
    }

    public boolean isRateSupported(int rateType) {
        if (supportedRates == null) return true;
        if (supportedRates.length <= 0) return true;
        for (int i : supportedRates) {
            if (rateType == i) {
                return true;
            }
        }
        return false;
    }
}