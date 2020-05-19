package dynoapps.exchange_rates.model.rates;

import androidx.annotation.NonNull;
import dynoapps.exchange_rates.interfaces.ValueType;

/**
 * Created by erdemmac on 24/11/2016.
 */

public abstract class BuySellRate extends BaseRate {
    public Float valueSellReal, valueBuyReal;
    public String valueSell, valueBuy;


    @Override
    public void toRateType() {
        int rateType = UNKNOWN;
        String plain_type = type.replace("\n", "");
        if (plain_type.equals("USD")) {
            rateType = USD;
        } else if (plain_type.equals("EUR")) {
            rateType = EUR;
        } else if (plain_type.contains("altın")) {
            rateType = ONS_TRY;
        } else if (plain_type.contains("parite")) {
            rateType = EUR_USD;
        }
        this.rateType = rateType;
    }

    @Override
    public void setRealValues() {
        String val = valueSell.replace(" TL", "").replace(",", ".").trim();
        valueSellReal = Float.valueOf(val);

        val = valueBuy.replace(" TL", "").replace(",", ".").trim();
        valueBuyReal = Float.valueOf(val);
    }


    @Override
    public float getValue(int valueType) {
        if (valueType == ValueType.BUY) {
            return valueBuyReal;
        } else if (valueType == ValueType.SELL) {
            return valueSellReal;
        }
        return 0.0f;
    }

    @Override
    @NonNull
    public String toString() {
        return type.split("_")[0] + " -> " + valueSellReal + " : " + valueBuyReal;
    }
}
