package dynoapps.exchange_rates.model.rates;

/**
 * Created by @Erdem OLKUN on 10/12/2016.
 */

public abstract class AvgRate extends BaseRate {
    public String avg_val;
    public float val_real_avg;

    @Override
    public float getValue(int value_type) {
        return val_real_avg;
    }

    @Override
    public String toString() {
        return "AvgRate{" +
                "avg_val='" + avg_val + '\'' +
                ", val_real_avg=" + val_real_avg +
                '}';
    }
}


