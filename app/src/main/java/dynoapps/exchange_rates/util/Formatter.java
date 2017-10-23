package dynoapps.exchange_rates.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by erdemmac on 06/12/2016.
 */

public class Formatter {

    private NumberFormat numberFormat;

    public Formatter(int digits) {
        numberFormat = DecimalFormat.getInstance();
        numberFormat.setMaximumFractionDigits(digits);
        numberFormat.setMinimumFractionDigits(Math.max(digits - 2, 0));
    }

    public Formatter(int digits, int min_digits) {
        this(digits);
        numberFormat.setMinimumFractionDigits(min_digits);
    }

    public String format(float value) {
        return numberFormat.format(value);
    }
}
