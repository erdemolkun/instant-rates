package dynoapps.exchange_rates.data;

import android.util.SparseArray;

import java.util.List;

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


    private SparseArray<List<T>> ratesArray;

    public List<T> getRates(int rateType) {
        if (ratesArray != null && ratesArray.get(rateType, null) != null) {
            return ratesArray.get(rateType);
        }
        return null;
    }

    public SparseArray<List<T>> getAllRates() {
        return ratesArray;
    }

    public void addRate(List<T> rates, int type) {
        if (ratesArray == null) {
            ratesArray = new SparseArray<>();
        }
        ratesArray.put(type, rates);
    }
}
