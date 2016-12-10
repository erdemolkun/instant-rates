package dynoapps.exchange_rates.model.rates;

import android.text.TextUtils;

/**
 * Created by @Erdem OLKUN on 10/12/2016.
 */

public class YahooRate extends AvgRate {
    @Override
    public void toRateType() {
        if (TextUtils.isEmpty(type)) rateType = UNKNOWN;
        int rateType = UNKNOWN;
        if (type.contains("USDTRY")) {
            rateType = USD;
        }
        else if (type.contains("EURTRY")) {
            rateType = EUR;
        }
        else if (type.contains("EURUSD")) {
            rateType = EUR_USD;
        }
        this.rateType = rateType;
    }

    @Override
    public void setRealValues() {
        if (rateType == UNKNOWN) return;
        avg_val_real = Float.valueOf(avg_val);
    }
}
