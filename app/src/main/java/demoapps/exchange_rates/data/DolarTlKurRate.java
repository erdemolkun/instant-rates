package demoapps.exchange_rates.data;

import android.text.TextUtils;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class DolarTlKurRate extends Rate implements IRate {


    @Override
    public int toRateType() {
        if (TextUtils.isEmpty(type)) return RateTypes.UNKNOWN;
        int rateType = RateTypes.UNKNOWN;
        switch (type) {
            case "USDTRY":
                return RateTypes.USD;
            case "EURTRY":
                return RateTypes.EUR;
            case "EURUSD":
                return RateTypes.EUR_USD;
            case "XAUUSD":
                return RateTypes.ONS;
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
        return type.split("_")[0] + " -> : " + realValue;
    }

}
