package dynoapps.exchange_rates.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by erdemmac on 06/12/2016.
 */

public class Formatter {

    NumberFormat numberFormat;

    public Formatter(int digits) {
        numberFormat = DecimalFormat.getInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(digits);

    }

    public String format(float value) {
        return numberFormat.format(value);
    }
}
