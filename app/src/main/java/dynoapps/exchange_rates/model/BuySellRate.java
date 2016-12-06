package dynoapps.exchange_rates.model;

/**
 * Created by erdemmac on 24/11/2016.
 */

public abstract class BuySellRate extends BaseRate {
    public Float value_sell_real, value_buy_real;
    public String value_sell, value_buy;

    @Override
    public String toString() {
        return type.split("_")[0] + " -> " + value_sell_real + " : " + value_buy_real;
    }

    @Override
    public int toRateType() {
        int rateType = UNKNOWN;
        String plain_type = type.replace("\n","");
        if (plain_type.equals("USD")){
            rateType = USD;
        }
        else if (plain_type.equals("EUR")){
            rateType = EUR;
        }
        else if (plain_type.contains("altın")){
            rateType = ONS_TRY;
        }
        else if (plain_type.contains("parite")){
            rateType = EUR_USD;
        }
        return rateType;
    }

    @Override
    public void setRealValues() {
        String val = value_sell.replace(" TL","").replace(",",".").trim();
        value_sell_real = Float.valueOf(val);

        val = value_buy.replace(" TL","").replace(",",".").trim();
        value_buy_real = Float.valueOf(val);
    }


}
