package dynoapps.exchange_rates.model.rates;

import dynoapps.exchange_rates.util.RateUtils;

/**
 * Created by erdemmac on 24/11/2016.
 */

public abstract class BaseRate implements IConvertable, IRate {

    public String type;

    @RateDef
    protected int rateType;

    BaseRate() {
    }

    @Override
    public int getRateType() {
        return rateType;
    }

    public String getFormatted(float val) {
        return RateUtils.valueToUI(val, rateType);
    }
}
