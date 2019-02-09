package dynoapps.exchange_rates.model.rates;

import android.text.TextUtils;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import dynoapps.exchange_rates.util.RateUtils;

/**
 * Created by erdemmac on 24/11/2016.
 */

@Root(name = "STOCK")
public class ParaGarantiRate extends AvgRate implements IConvertable {

    @Element(name = "SYMBOL", required = false)
    public String symbol;
    @Element(name = "LAST", required = false)
    public String last;
    @Element(name = "DESC", required = false)
    public String desc;
    @Element(name = "PERNC", required = false)
    public String pernc;
    @Element(name = "LAST_MOD", required = false)
    public String last_mod;
    @Element(name = "PERNC_NUMBER", required = false)
    public String pern_number;

    @Override
    public void toRateType() {
        if (TextUtils.isEmpty(symbol)) rateType = UNKNOWN;
        int rateType = UNKNOWN;
        switch (symbol) {
            case "KUSD":
                rateType = USD;
                break;
            case "KEUR":
                rateType = EUR;
                break;
            case "EUR":
                rateType = EUR_USD;
                break;
        }
        this.rateType = rateType;
    }

    @Override
    public void setRealValues() {
        if (rateType == UNKNOWN) return;
        last=last.replace(',','.');
        String val = last.replace("\'", "").replace("$", "").trim();
        Float real_val = RateUtils.toFloat(val);
        val_real_avg = real_val != null ? real_val : 0.0f;
    }

    @Override
    public String toString() {
        return "SYMBOL : " + symbol + " Value : " + last;
    }

}
