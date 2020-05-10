package dynoapps.exchange_rates.model.rates;

import android.text.TextUtils;

public class BloombergRate extends AvgRate implements IConvertable {

    @Override
    public void toRateType() {
        if (TextUtils.isEmpty(type)) rateType = UNKNOWN;
        int rateType = UNKNOWN;
        switch (type) {
            case "USDTRY Curncy":
                rateType = USD;
                break;
            case "EURTRY Curncy":
                rateType = EUR;
                break;
            case "EURUSD Curncy":
                rateType = EUR_USD;
                break;
            case "XAU Curncy":
                rateType = ONS;
                break;
        }
        this.rateType = rateType;
    }

    @Override
    public void setRealValues() {
        if (rateType == UNKNOWN) return;
        avg_val = avg_val.replace(".", "").replace(',', '.');
        val_real_avg = Float.parseFloat(avg_val);
    }
}
