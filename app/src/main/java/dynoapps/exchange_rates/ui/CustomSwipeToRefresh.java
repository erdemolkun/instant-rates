package dynoapps.exchange_rates.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Created by Erdem OLKUN , 07/02/2017 http://stackoverflow.com/questions/23989910/horizontalscrollview-inside-swiperefreshlayout
 */

public class CustomSwipeToRefresh extends SwipeRefreshLayout {

    private int mTouchSlop;
    private float mPrevX;
    // Indicate if we've already declined the move event
    private boolean mDeclined;

    public CustomSwipeToRefresh(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPrevX = MotionEvent.obtain(event).getX();
                mDeclined = false; // New action
                break;

            case MotionEvent.ACTION_MOVE:
                final float eventX = event.getX();
                float xDiff = Math.abs(eventX - mPrevX);

                if (mDeclined || xDiff > mTouchSlop) {
                    mDeclined = true; // Memorize
                    return false;
                }
        }

        return super.onInterceptTouchEvent(event);
    }
}