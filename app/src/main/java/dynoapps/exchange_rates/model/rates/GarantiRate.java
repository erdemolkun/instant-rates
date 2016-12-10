package dynoapps.exchange_rates.model.rates;

import android.text.TextUtils;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class GarantiRate extends AvgRate implements IConvertable {

    @Override
    public void toRateType() {
        if (TextUtils.isEmpty(type)) rateType = UNKNOWN;
        int rateType = UNKNOWN;
        switch (type) {
            case "USD":
                rateType = USD;
        }
        this.rateType = rateType;
    }

    @Override
    public void setRealValues() {
        if (rateType == UNKNOWN) return;
        String val = avg_val.replace("\'", "").replace("$", "").trim();
        avg_val_real = Float.valueOf(val);
    }

    @Override
    public String toString() {
        return type.split("_")[0] + " -> : " + avg_val_real;
    }

}
