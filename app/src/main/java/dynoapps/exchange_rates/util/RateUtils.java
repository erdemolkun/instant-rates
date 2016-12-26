package dynoapps.exchange_rates.util;

import android.support.annotation.DrawableRes;
import android.text.TextUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.List;

import dynoapps.exchange_rates.App;
import dynoapps.exchange_rates.R;
import dynoapps.exchange_rates.model.rates.BaseRate;
import dynoapps.exchange_rates.model.rates.IRate;

/**
 * Created by erdemmac on 01/12/2016.
 */

public class RateUtils {
    private static Formatter formatter2 = new Formatter(2);
    private static Formatter formatter5 = new Formatter(5);

    public static <T extends BaseRate> T getRate(List<T> rates, int rateType) {
        if (rates == null) return null;
        for (T rate : rates) {
            if (rate.getRateType() == rateType) {
                return rate;
            }
        }
        return null;
    }

    public static String valueToUI(float val, int rateType) {
        String formatted = formatValue(val, rateType);
        if (rateType == IRate.EUR_USD) {
            return formatted;
        } else if (rateType == IRate.ONS) {
            return App.context().getString(R.string.placeholder_dollar, formatted);
        } else {
            return App.context().getString(R.string.placeholder_tl, formatted);
        }
    }

    public static String formatValue(float val, int rateType) {
        if (rateType == IRate.ONS) {
            return formatter2.format(val);
        } else {
            return formatter5.format(val);
        }
    }

    public static String rateName(int rate_type) {
        if (rate_type == IRate.EUR) {
            return "EUR"; // todo refactor with side menu names.
        } else if (rate_type == IRate.USD) {
            return "USD";
        } else if (rate_type == IRate.EUR_USD) {
            return "EUR_USD";
        } else if (rate_type == IRate.ONS) {
            return "ONS";
        }
        return "";
    }

    public static
    @DrawableRes
    int getRateIcon(int rateType) {
        if (rateType == IRate.EUR) {
            return R.drawable.ic_euro;
        } else if (rateType == IRate.ONS) {
            return R.drawable.ic_gold;
        } else if (rateType == IRate.USD) {
            return R.drawable.ic_dollar;
        } else if (rateType == IRate.EUR_USD) {
            return R.drawable.ic_exchange_eur_usd;
        }
        return -1;
    }

    public static Float toFloat(String str) {
        if (TextUtils.isEmpty(str)) return null;
        str = str.trim();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(DecimalFormatSymbols.getInstance().getDecimalSeparator());
        symbols.setGroupingSeparator(DecimalFormatSymbols.getInstance().getGroupingSeparator());
        DecimalFormat format = new DecimalFormat("###,###,###,##0.####");
        format.setDecimalFormatSymbols(symbols);
        try {
            return format.parse(str).floatValue();
        } catch (ParseException e) {


        }
        return null;
    }
}
