package dynoapps.exchange_rates.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import dynoapps.exchange_rates.App;
import dynoapps.exchange_rates.R;


/**
 * Created by eolkun on 6.2.2015.
 */
public class ViewUtils {

    private static final int[] RES_IDS_ACTION_BAR_SIZE = {R.attr.actionBarSize};


    public static int getStatusBarHeight(@NonNull Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void setPaddingToolbar(Toolbar toolbar, Context context) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (toolbar != null && currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Do something for lollipop and above versions
            toolbar.setPadding(0, ViewUtils.getStatusBarHeight(context), 0, 0);
        }
    }


    public static void disableEnableControls(boolean enable, View v) {
        v.setEnabled(enable);
        v.setClickable(enable);
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                disableEnableControls(enable, child);
            }
        }
    }


    public static void visibility(View v, boolean isVisible) {
        if (v == null) return;
        v.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public static void visibility_invisible(View v, boolean isVisible) {
        if (v == null) return;
        v.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
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
        if (context == null) return (int) dp;
        return dpToPx(dp, context.getResources());
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px) {
        Context context = App.context();
        if (context == null) return px;
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / (metrics.densityDpi / (float) DisplayMetrics.DENSITY_MEDIUM);
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

        TypedArray att = curTheme.obtainStyledAttributes(RES_IDS_ACTION_BAR_SIZE);
        if (att == null) {
            return 0;
        }

        float size = att.getDimension(0, 0);
        att.recycle();
        return (int) size;
    }

    public static boolean canScrollUp(View view) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView
                        .getChildAt(0).getTop() < absListView.getPaddingTop());
            } else {
                return view.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(view, -1);
        }
    }


    public static Point getScreenSize(@NonNull Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }


    public static int getScreenHeight(@NonNull Activity activity) {
        Point size = getScreenSize(activity);
        return size.y;
    }

}
