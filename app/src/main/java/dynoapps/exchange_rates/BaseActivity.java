package dynoapps.exchange_rates;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import dynoapps.exchange_rates.util.L;

/**
 * Created by erdemmac on 06/12/2016.
 */

public abstract class BaseActivity extends AppCompatActivity {

    private Toolbar mActionBarToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        applySystemBarInsets();
    }

    /**
     * Since targetSdk 35 activities are drawn edge-to-edge. Pad the content view by the system bar
     * and display cutout insets so the existing layouts keep clear of the status/navigation bars.
     */
    private void applySystemBarInsets() {
        View content = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
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
