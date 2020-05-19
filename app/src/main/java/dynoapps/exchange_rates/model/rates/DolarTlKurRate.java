package dynoapps.exchange_rates.model.rates;

import android.text.TextUtils;

import androidx.annotation.NonNull;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class DolarTlKurRate extends AvgRate {

    @Override
    public void toRateType() {
        if (TextUtils.isEmpty(type)) rateType = UNKNOWN;
        int rateType = UNKNOWN;
        switch (type) {
            case "USDTRY":
                rateType = USD;
                break;
            case "EURTRY":
                rateType = EUR;
                break;
            case "EURUSD":
                rateType = EUR_USD;
                break;
            case "XAUUSD":
                rateType = ONS;
                break;
        }
        this.rateType = rateType;
    }

    @Override
    public void setRealValues() {
        if (rateType == UNKNOWN) return;
        String val = avg_val.replace("'", "").replace("$", "").trim();
        val_real_avg = Float.parseFloat(val);
    }

    @Override
    @NonNull
    public String toString() {
        return type.split("_")[0] + " -> : " + val_real_avg;
    }

}
