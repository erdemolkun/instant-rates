package dynoapps.exchange_rates.model;

/**
 * Created by erdemmac on 06/12/2016.
 */

public interface IRate {
    int UNKNOWN = 0;
    int USD = 1;
    int EUR = 2;
    int ONS = 3;
    int ONS_TRY = 4;
    int EUR_USD = 5;

    public int getRateType();
}
