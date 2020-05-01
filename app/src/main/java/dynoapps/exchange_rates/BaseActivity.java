package dynoapps.exchange_rates;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import butterknife.ButterKnife;
import dynoapps.exchange_rates.util.L;

/**
 * Created by erdemmac on 06/12/2016.
 */

public abstract class BaseActivity extends AppCompatActivity {

    private Toolbar mActionBarToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        ButterKnife.bind(this);
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
            drawable.mutate().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        }
        getActionBarToolbar().setNavigationIcon(drawable);
    }


}
