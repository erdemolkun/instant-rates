package dynoapps.exchange_rates.model.rates;

import dynoapps.exchange_rates.App;
import dynoapps.exchange_rates.R;
import dynoapps.exchange_rates.util.Formatter;

/**
 * Created by erdemmac on 24/11/2016.
 */

public abstract class BaseRate implements IConvertable, IRate {

    static Formatter formatter = new Formatter(5);

    BaseRate() {
        fetchMilis = System.currentTimeMillis();
    }

    public long fetchMilis;
    protected int rateType;
    public float realValue;
    public String type;
    public String value;

    @Override
    public int getRateType() {
        return rateType;
    }

    public String getFormatted(float val) {
        if (rateType == IRate.EUR_USD) {
            return formatter.format(val);
        } else {
            return App.context().getString(R.string.placeholder_tl, formatter.format(val));
        }
    }
}
