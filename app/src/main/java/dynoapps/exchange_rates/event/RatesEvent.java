package dynoapps.exchange_rates.event;

import java.util.List;

import dynoapps.exchange_rates.model.rates.BaseRate;

/**
 * Created by erdemmac on 05/12/2016.
 */

public class RatesEvent<T extends BaseRate> {

    public int source_type;
    public long fetch_time;
    public List<T> rates;

    public RatesEvent(List<T> rates, int sourceType, long fetchTime) {
        this.rates = rates;
        this.source_type = sourceType;
        this.fetch_time = fetchTime;
    }
}
