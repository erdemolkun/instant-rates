package dynoapps.exchange_rates.model.rates;

import dynoapps.exchange_rates.App;
import dynoapps.exchange_rates.R;
import dynoapps.exchange_rates.util.Formatter;

/**
 * Created by erdemmac on 24/11/2016.
 */

public abstract class BaseRate implements IConvertable, IRate {

    private static Formatter formatter5 = new Formatter(5);
    private static Formatter formatter2 = new Formatter(2);

    BaseRate() {
        fetchMilis = System.currentTimeMillis();
    }

    public long fetchMilis;
    protected int rateType;
    public String type;

    @Override
    public int getRateType() {
        return rateType;
    }

    public String getFormatted(float val) {
        if (rateType == IRate.EUR_USD) {
            return formatter5.format(val);
        } else if (rateType == IRate.ONS) {
            return App.context().getString(R.string.placeholder_dollar, formatter2.format(val));
        } else {
            return App.context().getString(R.string.placeholder_tl, formatter5.format(val));
        }
    }
}
