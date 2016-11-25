package demoapps.exchangegraphics.data;

import android.text.TextUtils;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class YorumlarRate extends Rate implements IRate {

    @Override
    public int toRateType() {
        if (TextUtils.isEmpty(type)) return RateTypes.UNKNOWN;
        int rateType = RateTypes.UNKNOWN;
        switch (type) {
            case "dolar_guncelle":
                return RateTypes.USD;
            case "euro_guncelle":
                return RateTypes.EUR;
            case "parite_guncelle":
                return RateTypes.EUR_USD;
            case "ons_guncelle":
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
        return type.split("_")[0] + " -> : " + realValue + " : Time -> " + time.replace("\'", "");
    }

}
