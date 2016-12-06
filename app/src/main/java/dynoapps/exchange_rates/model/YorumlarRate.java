package dynoapps.exchange_rates.model;

import android.text.TextUtils;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class YorumlarRate extends BaseRate implements IConvertable {

    public String time;
    @Override
    public int toRateType() {
        if (TextUtils.isEmpty(type)) return UNKNOWN;
        int rateType = UNKNOWN;
        switch (type) {
            case "dolar_guncelle":
                return USD;
            case "euro_guncelle":
                return EUR;
            case "parite_guncelle":
                return EUR_USD;
            case "ons_guncelle":
                return ONS;
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
        return type.split("_")[0] + " -> : " + realValue + " : Time -> " + time.replace("\'", "");
    }

}
