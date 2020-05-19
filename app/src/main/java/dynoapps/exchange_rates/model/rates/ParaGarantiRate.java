package dynoapps.exchange_rates.model.rates;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import dynoapps.exchange_rates.util.RateUtils;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class ParaGarantiRate extends AvgRate {

    public String symbol;
    public String last;

    @Override
    public void toRateType() {
        if (TextUtils.isEmpty(symbol)) rateType = UNKNOWN;
        int rateType = UNKNOWN;
        switch (symbol) {
            case "KUSD":
                rateType = USD;
                break;
            case "KEUR":
                rateType = EUR;
                break;
            case "EUR":
                rateType = EUR_USD;
                break;
        }
        this.rateType = rateType;
    }

    @Override
    public void setRealValues() {
        if (rateType == UNKNOWN) return;
        last = last.replace(',', '.');
        String val = last.replace("'", "").replace("$", "").trim();
        Float real_val = RateUtils.toFloat(val);
        val_real_avg = real_val != null ? real_val : 0.0f;
    }

    @Override
    @NonNull
    public String toString() {
        return "SYMBOL : " + symbol + " Value : " + last;
    }

}
