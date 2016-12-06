package dynoapps.exchange_rates.model;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class EnparaRate extends BuySellRate {
    @Override
    public int toRateType() {
        int rateType = RateTypes.UNKNOWN;
        String plain_type = type.replace("\n", "");
        plain_type = plain_type.toLowerCase();
        if (plain_type.equals("usd")) {
            rateType = RateTypes.USD;
        } else if (plain_type.equals("eur")) {
            rateType = RateTypes.EUR;
        } else if (plain_type.contains("altın")) {
            rateType = RateTypes.ONS_TRY;
        } else if (plain_type.contains("parite")) {
            rateType = RateTypes.EUR_USD;
        }
        return rateType;
    }

    @Override
    public void setRealValues() {
        String val = value_sell.replace(" TL", "").replace(",", ".").trim();
        value_sell_real = Float.valueOf(val);

        val = value_buy.replace(" TL", "").replace(",", ".").trim();
        value_buy_real = Float.valueOf(val);
    }


}
