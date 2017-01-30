package dynoapps.exchange_rates;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.transition.TransitionManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dynoapps.exchange_rates.alarm.AlarmManager;
import dynoapps.exchange_rates.alarm.AlarmsActivity;
import dynoapps.exchange_rates.data.CurrencySource;
import dynoapps.exchange_rates.data.RatesHolder;
import dynoapps.exchange_rates.event.DataSourceUpdate;
import dynoapps.exchange_rates.event.IntervalUpdate;
import dynoapps.exchange_rates.event.RatesEvent;
import dynoapps.exchange_rates.event.UpdateTriggerEvent;
import dynoapps.exchange_rates.interfaces.ValueType;
import dynoapps.exchange_rates.model.rates.AvgRate;
import dynoapps.exchange_rates.model.rates.BaseRate;
import dynoapps.exchange_rates.model.rates.BuySellRate;
import dynoapps.exchange_rates.model.rates.IRate;
import dynoapps.exchange_rates.provider.BasePoolingProvider;
import dynoapps.exchange_rates.service.RatePollingService;
import dynoapps.exchange_rates.time.TimeIntervalManager;
import dynoapps.exchange_rates.util.AnimationHelper;
import dynoapps.exchange_rates.util.AppUtils;
import dynoapps.exchange_rates.util.RateUtils;
import dynoapps.exchange_rates.util.ViewUtils;

/**
 * Created by erdemmac on 06/12/2016.
 */

public class LandingActivity extends BaseActivity {

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    ActionBarDrawerToggle toggle;

    @BindView(R.id.v_drawer_item_usd)
    TextView tvDrawerItemUsd;

    @BindView(R.id.v_drawer_item_eur)
    TextView tvDrawerItemEur;

    @BindView(R.id.v_drawer_item_eur_usd)
    TextView tvDrawerItemEurUsd;

    @BindView(R.id.v_drawer_item_ons)
    TextView tvDrawerItemOns;

    @BindView(R.id.v_drawer_item_alarms)
    TextView tvDrawerItemAlarms;

    @BindView(R.id.v_navdrawer_version)
    TextView tvVersion;

    @BindView(R.id.tv_interval_hint)
    TextView tvIntervalHint;

    @BindView(R.id.swipe_to_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private Handler mHandler;
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setAnimationType(AnimationHelper.FADE_IN);
        super.onCreate(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        if (getActionBarToolbar() != null) {
            getActionBarToolbar().setTitle(getTitle());
        }

        setupNavDrawer();

        ImageView ivLogo = (ImageView) findViewById(R.id.iv_drawer_header_logo);
        Drawable drawable = ivLogo.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }

        TimeIntervalManager.setAlarmMode(false);
        setUpRateCardViews();
        refreshCardItemViews();

        /**
         * Update with cached rates.
         * */

        for (CurrencySource currencySource : SourcesManager.getCurrencySources()) {
            if (currencySource != null && currencySource.isEnabled()) {
                RatesEvent ratesEvent = RatesHolder.getInstance().getLatestEvent(currencySource.getType());
                if (ratesEvent!=null) {
                    update(ratesEvent.rates, ratesEvent.source_type, false);
                }
            }
        }

        for (CardViewItemParent parent : parentItems) {
            for (CardViewItem item : parent.items) {
                item.tvType.setText(SourcesManager.getSourceName(item.source_type, item.value_type));
            }
        }

        tvVersion.setText(getString(R.string.version_placeholder, AppUtils.getPlainVersion()));
        updateHint();

        swipeRefreshLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3);
        swipeRefreshLayout.setEnabled(true);
        int top = ViewUtils.calculateActionBarSize(this);
        int progressBarStartMargin = getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_start_margin);
        int progressBarEndMargin = getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_end_margin);
        swipeRefreshLayout.setProgressViewOffset(true, top + progressBarStartMargin, top + progressBarEndMargin);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                EventBus.getDefault().post(new UpdateTriggerEvent());
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void refreshCardItemViews() {
        ArrayList<CurrencySource> dataSources = SourcesManager.getCurrencySources();
        for (CurrencySource dataSource : dataSources) {
            boolean isEnabled = dataSource.isEnabled();
            for (CardViewItemParent parent : parentItems) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    TransitionManager.beginDelayedTransition(parent.me);
                }
                boolean foundCard = false;
                for (CardViewItem item : parent.items) {
                    if (item.source_type == dataSource.getType()) {
                        item.card.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
                        foundCard = true;
                    }
                }
                if (!foundCard && isEnabled) {
                    if (dataSource.isRateSupported(parent.rate_type)) {
                        if (dataSource.isAvgType()) {
                            addCardToParent(parent, ValueType.AVG, dataSource.getType());
                        } else {
                            addCardToParent(parent, ValueType.BUY, dataSource.getType());
                            addCardToParent(parent, ValueType.SELL, dataSource.getType());
                        }
                    }
                }
            }
        }
    }

    private void addCardToParent(final CardViewItemParent parent, final int value_type, final int source_type) {
        LayoutInflater.from(this).inflate(R.layout.layout_simple_rate_card, parent.me, true);
        View v = parent.me.getChildAt(parent.me.getChildCount() - 1);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LandingActivity.this, ChartActivity.class);
                i.putExtra(ChartActivity.EXTRA_RATE_TYPE, parent.rate_type);
                startActivity(i);
            }
        });
        v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                RatesEvent ratesEvent = RatesHolder.getInstance().getLatestEvent(source_type);
                BaseRate rate = ratesEvent != null ? RateUtils.getRate(ratesEvent.rates, parent.rate_type) : null;
                if (rate != null) {
                    AlarmManager.addAlarmDialog(LandingActivity.this, source_type, rate.getRateType(), value_type, rate.getValue(value_type));
                } else {
                    AlarmManager.addAlarmDialog(LandingActivity.this, source_type, parent.rate_type, value_type, null);
                }
                return true;
            }
        });
        CardViewItem item = new CardViewItem(v, source_type, value_type);
        item.tvType.setText(SourcesManager.getSourceName(source_type, value_type));
        parent.items.add(item);
    }

    List<CardViewItemParent> parentItems = new ArrayList<>();

    static class CardViewItemParent {
        ViewGroup me;
        /**
         * Refers to {@link IRate#getRateType()}
         **/
        int rate_type;

        List<CardViewItem> items = new ArrayList<>();

        @Override
        public String toString() {
            String itemsToString = "";
            for (CardViewItem item : items) {
                itemsToString += "\n" + item.toString();
            }
            return itemsToString + "\nType : " + rate_type;
        }
    }

    static class CardViewItem {

        @BindView(R.id.tv_type)
        TextView tvType;

        @BindView(R.id.tv_rate_value)
        TextView tvValue;

        CardViewItem(View card, int source_type, int value_type) {
            this.card = card;
            this.source_type = source_type;
            this.value_type = value_type;
            ButterKnife.bind(this, card);
        }

        View card;

        int value_type;

        /**
         * Refers to {@link CurrencySource#getType()}
         */
        int source_type;


    }

    private void setUpRateCardViews() {
        if (parentItems == null) parentItems = new ArrayList<>();

        //#
        CardViewItemParent parentUsd = new CardViewItemParent();
        parentUsd.me = (ViewGroup) findViewById(R.id.v_card_holder_usd);
        parentUsd.rate_type = IRate.USD;


        parentItems.add(parentUsd);

        //#
        CardViewItemParent parentEur = new CardViewItemParent();
        parentEur.rate_type = IRate.EUR;
        parentEur.me = (ViewGroup) findViewById(R.id.v_card_holder_eur);


        parentItems.add(parentEur);

        //#
        CardViewItemParent parentParity = new CardViewItemParent();
        parentParity.rate_type = IRate.EUR_USD;
        parentParity.me = (ViewGroup) findViewById(R.id.v_card_holder_parity);

        parentItems.add(parentParity);

        //#
        CardViewItemParent parentOns = new CardViewItemParent();
        parentOns.rate_type = IRate.ONS;
        parentOns.me = (ViewGroup) findViewById(R.id.v_card_holder_ons);

        parentItems.add(parentOns);

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
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        tvDrawerItemUsd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doLeftMenuWork(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(LandingActivity.this, ChartActivity.class);
                        i.putExtra(ChartActivity.EXTRA_RATE_TYPE, IRate.USD);
                        startActivity(i);
                    }
                });

            }
        });

        tvDrawerItemEur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doLeftMenuWork(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(LandingActivity.this, ChartActivity.class);
                        i.putExtra(ChartActivity.EXTRA_RATE_TYPE, IRate.EUR);
                        startActivity(i);
                    }
                });
            }
        });

        tvDrawerItemEurUsd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doLeftMenuWork(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(LandingActivity.this, ChartActivity.class);
                        i.putExtra(ChartActivity.EXTRA_RATE_TYPE, IRate.EUR_USD);
                        startActivity(i);
                    }
                });
            }
        });

        tvDrawerItemOns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doLeftMenuWork(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(LandingActivity.this, ChartActivity.class);
                        i.putExtra(ChartActivity.EXTRA_RATE_TYPE, IRate.ONS);
                        startActivity(i);
                    }
                });
            }
        });

        tvDrawerItemAlarms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doLeftMenuWork(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(LandingActivity.this, AlarmsActivity.class);
                        startActivity(i);
                    }
                });
            }
        });
        ViewUtils.tint(tvDrawerItemUsd, R.color.colorPrimary);
        ViewUtils.tint(tvDrawerItemEur, R.color.colorPrimary);
        ViewUtils.tint(tvDrawerItemEurUsd, R.color.colorPrimary);
        ViewUtils.tint(tvDrawerItemOns, R.color.colorPrimary);
        ViewUtils.tint(tvDrawerItemAlarms, R.color.colorPrimary);

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
//        mDrawerLayout.setScrimColor(Color.parseColor("#66000000"));
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
            if (!AlarmManager.hasAnyActive()) {
                stopService(new Intent(this, RatePollingService.class));
            } else {
                TimeIntervalManager.setAlarmMode(true);
                for (CurrencySource source : SourcesManager.getCurrencySources()) {
                    BasePoolingProvider provider = (BasePoolingProvider) source.getPollingSource();
                    if (provider != null) {
                        provider.stopIfHasAlarm();
                    }
                }
            }
        }
        super.onDestroy();
    }

    private void update(List<BaseRate> rates, int source_type, boolean animated) {
        for (CardViewItemParent parent : parentItems) {
            for (CardViewItem item : parent.items) {
                if (item.source_type == source_type) {
                    BaseRate baseRate = RateUtils.getRate(rates, parent.rate_type);
                    if (baseRate != null) {
                        String val = "";
                        if (baseRate instanceof BuySellRate) {
                            if (item.value_type == ValueType.SELL) {
                                val = baseRate.getFormatted(((BuySellRate) baseRate).value_sell_real);
                            } else if (item.value_type == ValueType.BUY) {
                                val = baseRate.getFormatted(((BuySellRate) baseRate).value_buy_real);
                            }
                        } else if (baseRate instanceof AvgRate) {
                            val = baseRate.getFormatted(((AvgRate) baseRate).val_real_avg);
                        }
                        item.tvValue.setText(val);
                        if (animated)
                            playFadeOutInAnim(item.tvValue);
                    }
                }
            }
        }
    }

    private void playFadeOutInAnim(View v) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(v, "alpha", 1f, .3f);
        fadeOut.setDuration(450);
        fadeOut.setInterpolator(new FastOutSlowInInterpolator());
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(v, "alpha", .3f, 1f);
        fadeIn.setDuration(450);
        fadeOut.setInterpolator(new LinearOutSlowInInterpolator());

        final AnimatorSet mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeIn).after(fadeOut);
        mAnimationSet.start();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RatesEvent ratesEvent) {
        List<BaseRate> rates = ratesEvent.rates;
        update(rates, ratesEvent.source_type, true);
    }

    @Subscribe
    public void onEvent(DataSourceUpdate event) {
        refreshCardItemViews();
    }

    @Subscribe
    public void onEvent(IntervalUpdate event) {
        updateHint();
    }

    private void updateHint() {
        tvIntervalHint.setText(getString(R.string.interval_hint, TimeIntervalManager.getSelectionStr()));
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
        } else if (id == R.id.menu_item_sources) {
            SourcesManager.selectSources(this);
            return true;
        } else if (id == R.id.menu_add_alarm) {
            AlarmManager.addAlarmDialog(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
