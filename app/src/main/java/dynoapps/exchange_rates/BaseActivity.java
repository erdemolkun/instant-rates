package dynoapps.exchange_rates;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.ButterKnife;
import dynoapps.exchange_rates.util.AnimationHelper;
import dynoapps.exchange_rates.util.L;

/**
 * Created by erdemmac on 06/12/2016.
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected
    @AnimationHelper.AnimationType
    int animationTypeExit = AnimationHelper.NONE;
    @AnimationHelper.AnimationType
    int animationTypeEnter = AnimationHelper.NONE;
    private Handler handler;
    private Toolbar mActionBarToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AnimationHelper.doAnimation(this, animationTypeEnter, true);
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        ButterKnife.bind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            AnimationHelper.doAnimation(this, animationTypeExit, false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        App.getInstance().sendAnalyticsScreenName(this);
    }

    protected Handler getHandler() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        return handler;
    }

    protected void setAnimationType(@AnimationHelper.AnimationType int animationType) {
        this.animationTypeEnter = animationType;
        this.animationTypeExit = animationType;
    }

    protected void setAnimationType(@AnimationHelper.AnimationType int animationTypeEnter, @AnimationHelper.AnimationType int animationTypeExit) {
        this.animationTypeEnter = animationTypeEnter;
        this.animationTypeExit = animationTypeExit;
    }

    protected Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    @LayoutRes
    public abstract int getLayoutId();

    protected void setNavigationIcon(@DrawableRes Integer resId) {
        Drawable drawable = ContextCompat.getDrawable(this, resId);
        setNavigationIcon(drawable);
    }

    protected void setNavigationIcon(Drawable drawable) {
        Integer color = null;

        try {
            TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.actionBarIconColor});
            color = a.getColor(0, Color.WHITE);
        } catch (Exception e) {
            L.ex(e);
        }
        if (color != null) {
            drawable.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
        getActionBarToolbar().setNavigationIcon(drawable);
    }


}
