package dynoapps.exchange_rates.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;

import dynoapps.exchange_rates.App;


/**
 * Created by eolkun on 6.2.2015.
 */
public class ViewUtils {


    public static void visibility(View v, boolean isVisible) {
        if (v == null) return;
        v.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private static int dpToPx(float dp, Resources res) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                res.getDisplayMetrics());
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into
     *           pixels
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static int dpToPx(float dp) {
        Context context = App.context();
        return dpToPx(dp, context.getResources());
    }

    public static int getActionBarHeight(Context context) {
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        return 0; // Default or fallback logic
    }


}
