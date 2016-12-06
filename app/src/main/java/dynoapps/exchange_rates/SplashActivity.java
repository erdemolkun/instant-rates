package dynoapps.exchange_rates;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;

/**
 * Created by erdemmac on 03/12/2016.
 */

public class SplashActivity extends AppCompatActivity {
    Handler handler;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            gotoNextIntent();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(runnable, 600);
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
        if (handler != null) {
            handler.removeCallbacks(runnable);
            handler = null;
        }
    }

    private void gotoNextIntent() {
        Intent i = new Intent(this, LandingActivity.class);
        startActivity(i);
        finish();
    }
}
