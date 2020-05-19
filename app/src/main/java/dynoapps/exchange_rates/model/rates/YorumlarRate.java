package dynoapps.exchange_rates.model.rates;

import android.text.TextUtils;

import androidx.annotation.NonNull;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class YorumlarRate extends AvgRate {

    public String time;

    @Override
    public void toRateType() {
        if (TextUtils.isEmpty(type)) rateType = UNKNOWN;
        int rateType = UNKNOWN;
        switch (type) {
            case "dolar_guncelle":
                rateType = USD;
                break;
            case "euro_guncelle":
                rateType = EUR;
                break;
            case "parite_guncelle":
                rateType = EUR_USD;
                break;
            case "ons_guncelle":
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
        return type.split("_")[0] + " -> : " + val_real_avg + " : Time -> " + time.replace("'", "");
    }

}
