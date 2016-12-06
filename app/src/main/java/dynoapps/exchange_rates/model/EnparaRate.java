package dynoapps.exchange_rates.model;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class EnparaRate extends BuySellRate {
    @Override
    public int toRateType() {
        int rateType = UNKNOWN;
        String plain_type = type.replace("\n", "");
        plain_type = plain_type.toLowerCase();
        if (plain_type.equals("usd")) {
            rateType = USD;
        } else if (plain_type.equals("eur")) {
            rateType = EUR;
        } else if (plain_type.contains("altın")) {
            rateType = ONS_TRY;
        } else if (plain_type.contains("parite")) {
            rateType = EUR_USD;
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
