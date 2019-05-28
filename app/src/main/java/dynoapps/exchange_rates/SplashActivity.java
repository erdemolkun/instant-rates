package dynoapps.exchange_rates;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import butterknife.ButterKnife;

/**
 * Created by erdemmac on 03/12/2016.
 */

public class SplashActivity extends BaseServiceActivity {
    private static final int MIN_DURATION = 450;

    private long startMilis;
    Runnable runnable = this::gotoNextIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        startMilis = System.currentTimeMillis();

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_splash;
    }

    protected void onConnectionDone() {
        long currentMilis = System.currentTimeMillis();
        if (currentMilis - startMilis < MIN_DURATION) {
            getHandler().postDelayed(runnable, Math.abs(MIN_DURATION - (currentMilis - startMilis)));
        } else {
            gotoNextIntent();
        }
    }


    @Override
    public void onBackPressed() {
        clearEvents();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        clearEvents();
        super.onDestroy();
    }

    private void clearEvents() {
        if (getHandler() != null) {
            getHandler().removeCallbacks(runnable);
        }
    }

    private void gotoNextIntent() {
        Intent i = new Intent(this, LandingActivity.class);
        startActivity(i);
        finish();
    }
}
