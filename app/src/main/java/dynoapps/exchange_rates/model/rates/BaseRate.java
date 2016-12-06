package dynoapps.exchange_rates.model.rates;

/**
 * Created by erdemmac on 24/11/2016.
 */

public abstract class BaseRate implements IConvertable, IRate {

    BaseRate() {
        fetchMilis = System.currentTimeMillis();
    }

    public long fetchMilis;
    protected int rateType;
    public float realValue;
    public String type;
    public String value;

    public boolean isValidDiffTime(BaseRate rate) {
        return Math.abs(rate.fetchMilis - fetchMilis) < 5000;
    }

    @Override
    public int getRateType() {
        return rateType;
    }
}
