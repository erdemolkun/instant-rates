package dynoapps.exchange_rates.model.rates;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

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

    @RateDef
    int getRateType();

    float getValue(int valueType);

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            UNKNOWN,
            USD,
            EUR,
            ONS,
            ONS_TRY,
            EUR_USD
    })
    @interface RateDef {
    }
}
