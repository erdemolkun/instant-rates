package dynoapps.exchange_rates.data;

import java.util.HashMap;
import java.util.List;

/**
 * Created by erdemmac on 05/12/2016.
 */

public class RatesHolder {

    private static RatesHolder instance;

    public static RatesHolder getInstance() {
        if (instance == null)
            instance = new RatesHolder();
        return instance;
    }


    private HashMap<Class, List> ratesHash;

    public <T> List<T> getRates(Class clazz) {
        if (ratesHash != null && ratesHash.containsKey(clazz)) {
            return ratesHash.get(clazz);
        }
        return null;
    }

    public void addRate(List rates, Class clazz) {
        if (ratesHash == null) {
            ratesHash = new HashMap<>();
        }
        ratesHash.put(clazz, rates);
    }
}
