package dynoapps.exchange_rates.event;

import java.util.List;

import dynoapps.exchange_rates.model.rates.BaseRate;

/**
 * Created by erdemmac on 05/12/2016.
 */

public class RatesEvent<T extends BaseRate> {

    public int source_type;
    public long fetch_time;

    public RatesEvent(List<T> rates, int source_type, long fetch_time) {
        this.rates = rates;
        this.source_type = source_type;
        this.fetch_time = fetch_time;
    }

    public RatesEvent(List<T> rates, int source_type) {
        this.rates = rates;
        this.source_type = source_type;
        this.fetch_time = System.currentTimeMillis();
    }

    public List<T> rates;
}
