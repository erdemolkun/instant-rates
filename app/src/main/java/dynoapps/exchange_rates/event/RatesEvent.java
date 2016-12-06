package dynoapps.exchange_rates.event;

import java.util.List;

import dynoapps.exchange_rates.model.rates.BaseRate;

/**
 * Created by erdemmac on 05/12/2016.
 */

public class RatesEvent<T extends BaseRate> {
    public RatesEvent(List<T> rates) {
        this.rates = rates;
    }

    public List<T> rates;
}
