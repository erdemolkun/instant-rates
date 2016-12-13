package dynoapps.exchange_rates.util;

import java.util.List;

import dynoapps.exchange_rates.App;
import dynoapps.exchange_rates.R;
import dynoapps.exchange_rates.model.rates.BaseRate;
import dynoapps.exchange_rates.model.rates.IRate;

/**
 * Created by erdemmac on 01/12/2016.
 */

public class RateUtils {

    public static <T extends BaseRate> T getRate(List<T> rates, int rateType) {
        if (rates == null) return null;
        for (T rate : rates) {
            if (rate.getRateType() == rateType) {
                return rate;
            }
        }
        return null;
    }

    public static String entryToUI(float entry, int rateType) {
        String val = (rateType == IRate.USD || rateType == IRate.EUR) ?
                App.context().getString(R.string.placeholder_tl, "" + entry) : "" + entry;
        return val;
    }
}
