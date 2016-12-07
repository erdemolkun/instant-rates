package dynoapps.exchange_rates.model.rates;

/**
 * Created by erdemmac on 05/12/2016.
 */

public class BigparaRate extends BuySellRate {

    @Override
    public void toRateType() {
        int rateType = UNKNOWN;
        String plain_type = type;
        plain_type = plain_type.toLowerCase();
        if (plain_type.contains("dolar")) {
            rateType = USD;
        } else if (plain_type.contains("euro")) {
            rateType = EUR;
        } else if (plain_type.contains("altın")) {
            rateType = ONS_TRY;
        } else if (plain_type.contains("parite")) {
            rateType = EUR_USD;
        }
        this.rateType = rateType;
    }
}

