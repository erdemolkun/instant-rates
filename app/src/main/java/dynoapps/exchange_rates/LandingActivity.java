package dynoapps.exchange_rates;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.transition.TransitionManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import dynoapps.exchange_rates.data.RateDataSource;
import dynoapps.exchange_rates.data.RatesHolder;
import dynoapps.exchange_rates.event.RatesEvent;
import dynoapps.exchange_rates.model.rates.BaseRate;
import dynoapps.exchange_rates.model.rates.BigparaRate;
import dynoapps.exchange_rates.model.rates.DolarTlKurRate;
import dynoapps.exchange_rates.model.rates.EnparaRate;
import dynoapps.exchange_rates.model.rates.IRate;
import dynoapps.exchange_rates.model.rates.YapıKrediRate;
import dynoapps.exchange_rates.model.rates.YorumlarRate;
import dynoapps.exchange_rates.service.RatePollingService;
import dynoapps.exchange_rates.time.TimeIntervalManager;
import dynoapps.exchange_rates.util.Formatter;
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

    @BindView(R.id.v_card_enpara_sell_parite)
    View cardEnparaSellParite;

    @BindView(R.id.v_card_enpara_buy_parite)
    View cardEnparaBuyParite;

    @BindView(R.id.v_card_yorumlar_parite)
    View cardYorumlarParite;

    @BindView(R.id.v_landing_side_menu_hint_close)
    ImageView ivCloseHint;

    @BindView(R.id.v_landing_side_menu_hint)
    View vCloseHint;

    private Handler mHandler;
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;
    private Formatter formatter = new Formatter(4);

    interface ValueType {
        int BUY = 1;
        int SELL = 2;
        int AVG = 3;
    }

    List<CardViewItemParent> parentItems = new ArrayList<>();

    static class CardViewItemParent {
        View me;
        /**
         * Refers to {@link IRate#getRateType()}
         **/
        int type;

        List<CardViewItem> items = new ArrayList<>();

    }

    static class CardViewItem {

        CardViewItem(View card, int source_type, int valueType) {
            this.card = card;
            this.source_type = source_type;
            this.valueType = valueType;
        }

        View card;

        int valueType;

        /**
         * Refers to {@link RateDataSource#getSourceType()}
         */
        int source_type;


    }

    private void setUpDataSourceCards() {
        if (parentItems == null) parentItems = new ArrayList<>();

        //#
        CardViewItemParent parentUsd = new CardViewItemParent();
        parentUsd.me = findViewById(R.id.v_card_holder_usd);
        parentUsd.type = IRate.USD;

        parentUsd.items.add(new CardViewItem(cardEnparaSellUsd, RateDataSource.Type.ENPARA, ValueType.SELL));
        parentUsd.items.add(new CardViewItem(cardEnparaBuyUsd, RateDataSource.Type.ENPARA, ValueType.BUY));
        parentUsd.items.add(new CardViewItem(cardYorumlarUsd, RateDataSource.Type.YORUMLAR, ValueType.AVG));

        parentItems.add(parentUsd);

        //#
        CardViewItemParent parentEur = new CardViewItemParent();
        parentEur.type = IRate.EUR;
        parentEur.me = findViewById(R.id.v_card_holder_eur);

        parentEur.items.add(new CardViewItem(cardEnparaSellEur, RateDataSource.Type.ENPARA, ValueType.SELL));
        parentEur.items.add(new CardViewItem(cardEnparaBuyEur, RateDataSource.Type.ENPARA, ValueType.BUY));
        parentEur.items.add(new CardViewItem(cardYorumlarEur, RateDataSource.Type.YORUMLAR, ValueType.AVG));

        parentItems.add(parentEur);

        //#
        CardViewItemParent parentParity = new CardViewItemParent();
        parentParity.type = IRate.EUR_USD;
        parentParity.me = findViewById(R.id.v_card_holder_parity);

        parentParity.items.add(new CardViewItem(cardEnparaSellParite, RateDataSource.Type.ENPARA, ValueType.SELL));
        parentParity.items.add(new CardViewItem(cardEnparaBuyParite, RateDataSource.Type.ENPARA, ValueType.BUY));
        parentParity.items.add(new CardViewItem(cardYorumlarParite, RateDataSource.Type.YORUMLAR, ValueType.AVG));

        parentItems.add(parentParity);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        if (getActionBarToolbar() != null) {
            getActionBarToolbar().setTitle(getTitle());
        }
        setupNavDrawer();
        DataSourcesManager.init();
        setUpDataSourceCards();

        if (RatesHolder.getInstance().getAllRates() != null) {
            HashMap mp = RatesHolder.getInstance().getAllRates();
            for (Object value : mp.values()) {
                update((List<BaseRate>) value);
            }
        }

        if (!isMyServiceRunning(RatePollingService.class)) {
            Intent intent = new Intent(this, RatePollingService.class);
            bindService(intent, rateServiceConnection, Context.BIND_AUTO_CREATE);
            startService(new Intent(this, RatePollingService.class));
        }
        for (CardViewItemParent parent : parentItems) {
            for (CardViewItem item : parent.items) {
                String postFix = "";
                if (item.valueType == ValueType.SELL) {
                    postFix = " Satış";
                } else if (item.valueType == ValueType.BUY) {
                    postFix = " Alış";
                }
                ((TextView) item.card.findViewById(R.id.tv_type)).setText(DataSourcesManager.getSourceName(item.source_type) + postFix);
            }
        }
//        ((TextView) cardEnparaSellUsd.findViewById(R.id.tv_type)).setText("Enpara Satış");
//        ((TextView) cardEnparaBuyUsd.findViewById(R.id.tv_type)).setText("Enpara Alış");
//        ((TextView) cardYorumlarUsd.findViewById(R.id.tv_type)).setText("Yorumlar");
//
//        ((TextView) cardEnparaSellEur.findViewById(R.id.tv_type)).setText("Enpara Satış");
//        ((TextView) cardEnparaBuyEur.findViewById(R.id.tv_type)).setText("Enpara Alış");
//        ((TextView) cardYorumlarEur.findViewById(R.id.tv_type)).setText("Yorumlar");
//
//        ((TextView) cardEnparaSellParite.findViewById(R.id.tv_type)).setText("Enpara Satış");
//        ((TextView) cardEnparaBuyParite.findViewById(R.id.tv_type)).setText("Enpara Alış");
//        ((TextView) cardYorumlarParite.findViewById(R.id.tv_type)).setText("Yorumlar");

        boolean isHintRemoved = Prefs.isLandingHintClosed();
        vCloseHint.setVisibility(isHintRemoved ? View.GONE : View.VISIBLE);
        if (!isHintRemoved) {
            ivCloseHint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    Prefs.saveLandingHintState(true);
                    vCloseHint.animate().alpha(0).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                TransitionManager.beginDelayedTransition((ViewGroup) findViewById(R.id.v_main_content));
                            }
                            vCloseHint.setVisibility(View.GONE);
                        }
                    }).setDuration(400).start();
                }
            });
        }

    }

    private void doLeftMenuWork(final Runnable runnable) {
        closeNavDrawer();
        // launch the target Activity after a short delay, to allow the close animation to play
        mHandler.postDelayed(runnable, NAVDRAWER_LAUNCH_DELAY);
    }

    protected void closeNavDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
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
                doLeftMenuWork(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(LandingActivity.this, RatesActivity.class);
                        startActivity(i);
                    }
                });

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
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
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

    private void update(List<BaseRate> rates) {
        BaseRate rateUsd = RateUtils.getRate(rates, IRate.USD);
        BaseRate rateEur = RateUtils.getRate(rates, IRate.EUR);
        BaseRate rateParite = RateUtils.getRate(rates, IRate.EUR_USD);

        if (rateUsd != null) {
            if (rateUsd instanceof YapıKrediRate) {
            } else if (rateUsd instanceof DolarTlKurRate) {
            } else if (rateUsd instanceof YorumlarRate) {

                ((TextView) cardYorumlarUsd.findViewById(R.id.tv_rate_value)).
                        setText(getString(R.string.placeholder_tl, formatter.format(rateUsd.realValue)));

                ((TextView) cardYorumlarEur.findViewById(R.id.tv_rate_value)).
                        setText(getString(R.string.placeholder_tl, formatter.format(rateEur.realValue)));


                ((TextView) cardYorumlarParite.findViewById(R.id.tv_rate_value)).
                        setText(getString(R.string.placeholder_tl, formatter.format(rateParite.realValue)));

            } else if (rateUsd instanceof EnparaRate) {
                ((TextView) cardEnparaBuyUsd.findViewById(R.id.tv_rate_value)).
                        setText(getString(R.string.placeholder_tl, formatter.format(((EnparaRate) rateUsd).value_buy_real)));
                ((TextView) cardEnparaSellUsd.findViewById(R.id.tv_rate_value)).
                        setText(getString(R.string.placeholder_tl, formatter.format(((EnparaRate) rateUsd).value_sell_real)));

                ((TextView) cardEnparaBuyEur.findViewById(R.id.tv_rate_value)).
                        setText(getString(R.string.placeholder_tl, formatter.format(((EnparaRate) rateEur).value_buy_real)));
                ((TextView) cardEnparaSellEur.findViewById(R.id.tv_rate_value)).
                        setText(getString(R.string.placeholder_tl, formatter.format(((EnparaRate) rateEur).value_sell_real)));

                ((TextView) cardEnparaBuyParite.findViewById(R.id.tv_rate_value)).
                        setText(getString(R.string.placeholder_tl, formatter.format(((EnparaRate) rateParite).value_buy_real)));
                ((TextView) cardEnparaSellParite.findViewById(R.id.tv_rate_value)).
                        setText(getString(R.string.placeholder_tl, formatter.format(((EnparaRate) rateParite).value_sell_real)));
            } else if (rateUsd instanceof BigparaRate) {
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RatesEvent ratesEvent) {
        List<BaseRate> rates = ratesEvent.rates;
        update(rates);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_landing, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_time_interval) {
            TimeIntervalManager.selectInterval(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
