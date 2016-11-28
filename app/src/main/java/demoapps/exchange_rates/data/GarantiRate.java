package demoapps.exchange_rates.data;

import android.text.TextUtils;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class GarantiRate extends Rate implements IRate {

    public String time;

    @Override
    public int toRateType() {
        if (TextUtils.isEmpty(type)) return RateTypes.UNKNOWN;
        int rateType = RateTypes.UNKNOWN;
        switch (type) {
            case "USD":
                return RateTypes.USD;
        }
        return rateType;
    }

    @Override
    public void setRealValues() {
        if (rateType == RateTypes.UNKNOWN) return;
        String val = value.replace("\'", "").replace("$", "").trim();
        realValue = Float.valueOf(val);
    }

    @Override
    public String toString() {
        return type.split("_")[0] + " -> : " + realValue + " : Time -> " + time.replace("\'", "");
    }

}
