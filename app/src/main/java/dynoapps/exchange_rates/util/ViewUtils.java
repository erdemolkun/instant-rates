package dynoapps.exchange_rates.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.TypedValue;
import android.view.View;

import dynoapps.exchange_rates.App;
import dynoapps.exchange_rates.R;


/**
 * Created by eolkun on 6.2.2015.
 */
public class ViewUtils {

    private static final int[] RES_IDS_ACTION_BAR_SIZE = {R.attr.actionBarSize};

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

    /**
     * Calculates the Action Bar height in pixels.
     */
    public static int calculateActionBarSize(Context context) {
        if (context == null) {
            return 0;
        }

        Resources.Theme curTheme = context.getTheme();
        if (curTheme == null) {
            return 0;
        }

        TypedArray attr = curTheme.obtainStyledAttributes(RES_IDS_ACTION_BAR_SIZE);

        float size = attr.getDimension(0, 0);
        attr.recycle();
        return (int) size;
    }
}
