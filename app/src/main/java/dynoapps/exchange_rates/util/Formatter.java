package dynoapps.exchange_rates.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by erdemmac on 06/12/2016.
 */

public class Formatter {

    private NumberFormat numberFormat;

    public Formatter(int digits) {
        numberFormat = DecimalFormat.getInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(digits);
        numberFormat.setMinimumFractionDigits(digits);
        numberFormat.setMinimumIntegerDigits(1);

    }

    public Formatter(int digits, int min_digits) {
        numberFormat = DecimalFormat.getInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(digits);
        numberFormat.setMinimumFractionDigits(min_digits);
        numberFormat.setMinimumIntegerDigits(2);

    }

    public String format(float value) {
        return numberFormat.format(value);
    }
}
