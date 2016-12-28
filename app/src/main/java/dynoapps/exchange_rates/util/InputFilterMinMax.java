package dynoapps.exchange_rates.util;


import android.text.InputFilter;
import android.text.Spanned;

/**
 * Created by A591108 on 05/11/2014.
 */
public class InputFilterMinMax implements InputFilter {

    private double min, max;

    public InputFilterMinMax(double min, double max) {
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
    }

    @Override
    public CharSequence filter(CharSequence source, int i, int i2, Spanned spanned, int i3, int i4) {
        try {
            String str = spanned.toString() + source.toString();
            Float input = RateUtils.toFloat(str);
            if (input != null && isInRange(min, max, input)) {
                return null;
            }
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
        return "";
    }

    private boolean isInRange(double min, double max, double value) {
        return value >= min && value <= max;
    }
}