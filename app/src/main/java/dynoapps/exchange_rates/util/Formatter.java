package dynoapps.exchange_rates.util;

import java.text.DecimalFormat;

/**
 * Created by erdemmac on 06/12/2016.
 */

public class Formatter {
    DecimalFormat mFormat;

    public Formatter(int digits) {

        StringBuffer b = new StringBuffer();
        for (int i = 0; i < digits; i++) {
            if (i == 0)
                b.append(".");
            b.append("0");
        }

        mFormat = new DecimalFormat("###,###,###,##0" + b.toString());
    }

    public String format(float value) {
        return mFormat.format(value);
    }
}
