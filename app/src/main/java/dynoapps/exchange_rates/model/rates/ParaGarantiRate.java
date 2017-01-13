package dynoapps.exchange_rates.model.rates;

import android.text.TextUtils;

import org.simpleframework.xml.Element;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class ParaGarantiRate extends AvgRate implements IConvertable {
    // todo
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
        String val = avg_val.replace("\'", "").replace("$", "").trim();
        avg_val_real = Float.valueOf(val);
    }

    @Override
    public String toString() {
        return type.split("_")[0] + " -> : " + avg_val_real + " : Time -> " + time.replace("\'", "");
    }


    @Element(name = "SYMBOL", required = false)
    public String symbol;

    @Element(name = "LAST", required = false)
    public String last;

}
