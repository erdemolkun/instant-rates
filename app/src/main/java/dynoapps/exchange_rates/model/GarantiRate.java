package dynoapps.exchange_rates.model;

import android.text.TextUtils;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class GarantiRate extends BaseRate implements IConvertable {

    @Override
    public int toRateType() {
        if (TextUtils.isEmpty(type)) return UNKNOWN;
        int rateType = UNKNOWN;
        switch (type) {
            case "USD":
                return USD;
        }
        return rateType;
    }

    @Override
    public void setRealValues() {
        if (rateType == UNKNOWN) return;
        String val = value.replace("\'", "").replace("$", "").trim();
        realValue = Float.valueOf(val);
    }

    @Override
    public String toString() {
        return type.split("_")[0] + " -> : " + realValue;
    }

}
