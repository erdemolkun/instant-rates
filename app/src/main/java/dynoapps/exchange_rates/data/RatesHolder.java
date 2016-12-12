package dynoapps.exchange_rates.data;

import android.util.SparseArray;

import java.util.List;

import dynoapps.exchange_rates.event.RatesEvent;
import dynoapps.exchange_rates.model.rates.BaseRate;

/**
 * Created by erdemmac on 05/12/2016.
 */

public class RatesHolder<T extends BaseRate> {

    private static RatesHolder instance;

    public static RatesHolder getInstance() {
        if (instance == null)
            instance = new RatesHolder();
        return instance;
    }

    private SparseArray<RatesEvent<T>> ratesArray;

    public RatesEvent<T> getRates(int source_type) {
        if (ratesArray != null && ratesArray.get(source_type, null) != null) {
            return ratesArray.get(source_type);
        }
        return null;
    }

    public SparseArray<RatesEvent<T>> getAllRates() {
        return ratesArray;
    }

    public void addRate(List<T> rates, long fetchMilis, int type) {
        if (ratesArray == null) {
            ratesArray = new SparseArray<>();
        }

        ratesArray.put(type, new RatesEvent<>(rates, type, fetchMilis));
    }

    public void addRate(List<T> rates, int type) {
        if (ratesArray == null) {
            ratesArray = new SparseArray<>();
        }

        ratesArray.put(type, new RatesEvent<>(rates, type, System.currentTimeMillis()));
    }
}
