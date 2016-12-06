package dynoapps.exchange_rates;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import dynoapps.exchange_rates.event.RatesEvent;
import dynoapps.exchange_rates.model.BaseRate;
import dynoapps.exchange_rates.model.BigparaRate;
import dynoapps.exchange_rates.model.DolarTlKurRate;
import dynoapps.exchange_rates.model.EnparaRate;
import dynoapps.exchange_rates.model.YapıKrediRate;
import dynoapps.exchange_rates.model.YorumlarRate;
import dynoapps.exchange_rates.service.RatePollingService;
import dynoapps.exchange_rates.util.RateUtils;

/**
 * Created by erdemmac on 06/12/2016.
 */

public class LandingActivity extends BaseActivity {

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    ActionBarDrawerToggle toggle;

    @BindView(R.id.v_drawer_item_usd)
    View vDrawerItemUsd;

    @BindView(R.id.v_card_enpara_sell_usd)
    View cardEnparaSellUsd;


    @BindView(R.id.v_card_enpara_usd_buy)
    View cardEnparaBuyUsd;

    @BindView(R.id.v_card_yorumlar_usd)
    View cardYorumlarUsd;

    @BindView(R.id.v_card_enpara_sell_eur)
    View cardEnparaSellEur;


    @BindView(R.id.v_card_enpara_buy_eur)
    View cardEnparaBuyEur;

    @BindView(R.id.v_card_yorumlar_eur)
    View cardYorumlarEur;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBarToolbar() != null) {
            getActionBarToolbar().setTitle(getTitle());
        }
        setupNavDrawer();

        if (!isMyServiceRunning(RatePollingService.class)) {
            Intent intent = new Intent(this, RatePollingService.class);
            bindService(intent, rateServiceConnection, Context.BIND_AUTO_CREATE);
            startService(new Intent(this, RatePollingService.class));
        }

        ((TextView) cardEnparaSellUsd.findViewById(R.id.tv_type)).setText("Enpara Satış");
        ((TextView) cardEnparaBuyUsd.findViewById(R.id.tv_type)).setText("Enpara Alış");
        ((TextView) cardYorumlarUsd.findViewById(R.id.tv_type)).setText("Yorumlar");

        ((TextView) cardEnparaSellEur.findViewById(R.id.tv_type)).setText("Enpara Satış");
        ((TextView) cardEnparaBuyEur.findViewById(R.id.tv_type)).setText("Enpara Alış");
        ((TextView) cardYorumlarEur.findViewById(R.id.tv_type)).setText("Yorumlar");

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_landing;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        vDrawerItemUsd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LandingActivity.this, RatesActivity.class);
                startActivity(i);
            }
        });
    }

    /**
     * Sets up the navigation drawer as appropriate. Note that the nav drawer will be different
     * depending on whether the attendee indicated that they are attending the event on-site vs.
     * attending remotely.
     */
    private void setupNavDrawer() {

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout == null) return;


//        mDrawerLayout.getParent().requestDisallowInterceptTouchEvent(true);
        findViewById(R.id.v_main_content).getParent().requestDisallowInterceptTouchEvent(true);


        mDrawerLayout.setScrimColor(Color.TRANSPARENT);
//        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        toggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
//                invalidateOptionsMenu();
                syncState();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
//                invalidateOptionsMenu();
                syncState();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toggle.syncState();

        if (mDrawerLayout == null) {
            return;
        }
        mDrawerLayout.addDrawerListener(toggle);
        mDrawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));


        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNavDrawerOpen())
                    mDrawerLayout.openDrawer(GravityCompat.START);
                else
                    mDrawerLayout.closeDrawer(GravityCompat.START);
            }
        });
    }

    protected boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (isMyServiceRunning(RatePollingService.class)) {
            stopService(new Intent(this, RatePollingService.class));
            unbindService(rateServiceConnection);
        }
        super.onDestroy();
    }

    RatePollingService ratePollingService;
    private ServiceConnection rateServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            ratePollingService = ((RatePollingService.SimpleBinder) binder).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            ratePollingService = null;
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RatesEvent ratesEvent) {
        List<BaseRate> rates = ratesEvent.rates;
        BaseRate rateUsd = RateUtils.getRate(rates, BaseRate.RateTypes.USD);
        BaseRate rateEur = RateUtils.getRate(rates, BaseRate.RateTypes.EUR);
        if (rateUsd != null) {
            if (rateUsd instanceof YapıKrediRate) {
            } else if (rateUsd instanceof DolarTlKurRate) {
            } else if (rateUsd instanceof YorumlarRate) {
                ((TextView) cardYorumlarUsd.findViewById(R.id.tv_rate_value)).
                        setText(getString(R.string.placeholder_tl, rateUsd.realValue));

                ((TextView) cardYorumlarEur.findViewById(R.id.tv_rate_value)).
                        setText(getString(R.string.placeholder_tl, rateEur.realValue));

            } else if (rateUsd instanceof EnparaRate) {
                ((TextView) cardEnparaBuyUsd.findViewById(R.id.tv_rate_value)).
                        setText(getString(R.string.placeholder_tl, ((EnparaRate) rateUsd).value_buy_real));
                ((TextView) cardEnparaSellUsd.findViewById(R.id.tv_rate_value)).
                        setText(getString(R.string.placeholder_tl, ((EnparaRate) rateUsd).value_sell_real));

                ((TextView) cardEnparaBuyEur.findViewById(R.id.tv_rate_value)).
                        setText(getString(R.string.placeholder_tl, ((EnparaRate) rateEur).value_buy_real));
                ((TextView) cardEnparaSellEur.findViewById(R.id.tv_rate_value)).
                        setText(getString(R.string.placeholder_tl, ((EnparaRate) rateEur).value_sell_real));
            } else if (rateUsd instanceof BigparaRate) {
            }
        }
    }

}
