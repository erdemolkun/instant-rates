package dynoapps.exchange_rates.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by erdemmac on 06/12/2016.
 */

public class Formatter {
    private DecimalFormat mFormat;
    NumberFormat numberFormat;

    public Formatter(int digits) {
        char groupingSeparator = DecimalFormatSymbols.getInstance(Locale.getDefault()).getGroupingSeparator();
        char decimalSeparator = DecimalFormatSymbols.getInstance(Locale.getDefault()).getDecimalSeparator();
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < digits; i++) {
            if (i == 0)
                b.append(decimalSeparator);
            b.append("0");
        }

        //mFormat = new DecimalFormat("###" + groupingSeparator + "###" + groupingSeparator + "###" + groupingSeparator + "##0" + b.toString());
//        mFormat = new DecimalFormat("###,###,###,##0" + b.toString());
        numberFormat = DecimalFormat.getInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(digits);

    }

    public String format(float value) {
        return numberFormat.format(value);
    }
}
