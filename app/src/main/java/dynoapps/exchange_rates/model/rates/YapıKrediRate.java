package dynoapps.exchange_rates.model.rates;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class YapıKrediRate extends BuySellRate {
    @Override
    public void toRateType() {
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
        this.rateType = rateType;
    }

    @Override
    public void setRealValues() {
        String val = valueSell.replace(" TL", "").replace(",", ".").trim();
        valueSellReal = Float.valueOf(val);

        val = valueBuy.replace(" TL", "").replace(",", ".").trim();
        valueBuyReal = Float.valueOf(val);
    }


}
