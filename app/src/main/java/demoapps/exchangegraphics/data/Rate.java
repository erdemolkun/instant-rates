package demoapps.exchangegraphics.data;

/**
 * Created by erdemmac on 24/11/2016.
 */

public abstract class Rate implements IRate {

    Rate() {
        fetchMilis = System.currentTimeMillis();
    }

    public long fetchMilis;
    public int rateType;
    public float realValue;
    public String type;
    public String value;

    public boolean isValidDiffTime(Rate rate) {
        return Math.abs(rate.fetchMilis - fetchMilis) < 5000;
    }

    public interface RateTypes {
        int UNKNOWN = 0;
        int USD = 1;
        int EUR = 2;
        int ONS = 3;
        int ONS_TRY = 4;
        int EUR_USD = 5;
    }

}
