package dynoapps.exchange_rates.data;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class BuySellRate extends Rate {
    public Float value_sell_real, value_buy_real;
    public String value_sell, value_buy;

    @Override
    public String toString() {
        return type.split("_")[0] + " -> " + value_sell_real + " : " + value_buy_real;
    }

    @Override
    public int toRateType() {
        int rateType = RateTypes.UNKNOWN;
        String plain_type = type.replace("\n","");
        if (plain_type.equals("USD")){
            rateType = RateTypes.USD;
        }
        else if (plain_type.equals("EUR")){
            rateType = RateTypes.EUR;
        }
        else if (plain_type.contains("altın")){
            rateType = RateTypes.ONS_TRY;
        }
        else if (plain_type.contains("parite")){
            rateType = RateTypes.EUR_USD;
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
