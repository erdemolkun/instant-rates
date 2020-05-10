package dynoapps.exchange_rates;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import butterknife.BindView;
import butterknife.ButterKnife;
import dynoapps.exchange_rates.alarm.AlarmManager;
import dynoapps.exchange_rates.alarm.AlarmsRepository;
import dynoapps.exchange_rates.data.CurrencySource;
import dynoapps.exchange_rates.data.RatesHolder;
import dynoapps.exchange_rates.event.RatesEvent;
import dynoapps.exchange_rates.interfaces.ValueType;
import dynoapps.exchange_rates.model.rates.AvgRate;
import dynoapps.exchange_rates.model.rates.BaseRate;
import dynoapps.exchange_rates.model.rates.BuySellRate;
import dynoapps.exchange_rates.model.rates.IRate;
import dynoapps.exchange_rates.provider.BasePoolingProvider;
import dynoapps.exchange_rates.service.RatePollingService;
import dynoapps.exchange_rates.time.TimeIntervalManager;
import dynoapps.exchange_rates.util.RateUtils;
import dynoapps.exchange_rates.util.ServiceUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by erdemmac on 06/12/2016.
 */

public class LandingActivity extends BaseServiceActivity {

    private static final String TAG_BOTTOM_SHEET = "TAG_BOTTOM_SHEET";

    @BindView(R.id.v_fab_add_alarm)
    View vFab;

    @BindView(R.id.tv_interval_hint)
    TextView tvIntervalHint;

    @BindView(R.id.bottomAppBar)
    BottomAppBar bottomAppBar;

    AlarmsRepository alarmsRepository;
    List<CardViewItemParent> parentItems = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmsRepository = App.getInstance().provideAlarmsRepository();

        setUpBottomAppBar();

        vFab.setOnClickListener(v -> AlarmManager.addAlarmDialog(this));

        TimeIntervalManager.setAlarmMode(false);
        setUpRateCardViews();
        refreshCardItemViews();

        /*
         * Update with cached rates.
         **/

        for (CurrencySource currencySource : SourcesManager.getCurrencySources()) {
            if (currencySource != null && currencySource.isEnabled()) {
                RatesEvent ratesEvent = RatesHolder.getInstance().getLatestEvent(currencySource.getType());
                if (ratesEvent != null) {
                    updateCards(ratesEvent.rates, ratesEvent.source_type, false);
                }
            }
        }

        for (CardViewItemParent parent : parentItems) {
            for (CardViewItem item : parent.items) {
                item.tvType.setText(SourcesManager.getSourceName(item.source_type, item.value_type));
            }
        }


        updateHint();

        Disposable disposable = TimeIntervalManager.getIntervalUpdates().observeOn(AndroidSchedulers.mainThread()).subscribe(__ -> updateHint());
        compositeDisposable.add(disposable);

        compositeDisposable.add(SourcesManager.getSourceUpdates().observeOn(AndroidSchedulers.mainThread()).subscribe(__ -> {
            refreshCardItemViews();
        }));

        compositeDisposable.add(ProvidersManager.getInstance().getRatesEventPublishSubject()
                .observeOn(AndroidSchedulers.mainThread()).subscribe(ratesEvent -> {
                    List<BaseRate> rates = ratesEvent.rates;
                    updateCards(rates, ratesEvent.source_type, true);
                }));

    }


    private void setUpBottomAppBar() {

        setSupportActionBar(bottomAppBar);

        bottomAppBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_time_interval) {
                TimeIntervalManager.selectInterval(this);
                return true;
            } else if (id == R.id.menu_item_sources) {
                SourcesManager.selectSources(this);
                return true;
            }
            return false;
        });

        bottomAppBar.setNavigationOnClickListener(view -> {
            if (getSupportFragmentManager().findFragmentByTag(TAG_BOTTOM_SHEET) == null) {
                BottomSheetDialogFragment bottomSheetDialogFragment = BottomSheetNavigationFragment.newInstance();
                bottomSheetDialogFragment.show(getSupportFragmentManager(), TAG_BOTTOM_SHEET);
            }
        });
    }

    private void refreshCardItemViews() {
        ArrayList<CurrencySource> dataSources = SourcesManager.getCurrencySources();
        for (CurrencySource dataSource : dataSources) {
            boolean isEnabled = dataSource.isEnabled();
            for (CardViewItemParent parent : parentItems) {
                TransitionManager.beginDelayedTransition(parent.me);
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
        v.setOnClickListener(view -> {
            Intent i = new Intent(LandingActivity.this, ChartActivity.class);
            i.putExtra(ChartActivity.EXTRA_RATE_TYPE, parent.rate_type);
            startActivity(i);
        });
        v.setOnLongClickListener(view -> {
            RatesEvent ratesEvent = RatesHolder.getInstance().getLatestEvent(source_type);
            BaseRate rate = ratesEvent != null ? RateUtils.getRate(ratesEvent.rates, parent.rate_type) : null;
            if (rate != null) {
                AlarmManager.addAlarmDialog(LandingActivity.this, source_type, rate.getRateType(), value_type, rate.getValue(value_type), null);
            } else {
                AlarmManager.addAlarmDialog(LandingActivity.this, source_type, parent.rate_type, value_type, null, null);
            }
            return true;
        });
        CardViewItem item = new CardViewItem(v, source_type, value_type);
        item.tvType.setText(SourcesManager.getSourceName(source_type, value_type));
        parent.items.add(item);
    }

    private void setUpRateCardViews() {
        if (parentItems == null) parentItems = new ArrayList<>();

        //#
        CardViewItemParent parentUsd = new CardViewItemParent();
        parentUsd.me = findViewById(R.id.v_card_holder_usd);
        parentUsd.rate_type = IRate.USD;


        parentItems.add(parentUsd);

        //#
        CardViewItemParent parentEur = new CardViewItemParent();
        parentEur.rate_type = IRate.EUR;
        parentEur.me = findViewById(R.id.v_card_holder_eur);


        parentItems.add(parentEur);

        //#
        CardViewItemParent parentParity = new CardViewItemParent();
        parentParity.rate_type = IRate.EUR_USD;
        parentParity.me = findViewById(R.id.v_card_holder_parity);

        parentItems.add(parentParity);

        //#
        CardViewItemParent parentOns = new CardViewItemParent();
        parentOns.rate_type = IRate.ONS;
        parentOns.me = findViewById(R.id.v_card_holder_ons);

        parentItems.add(parentOns);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_landing;
    }

    @Override
    protected void onDestroy() {
        if (ServiceUtils.isMyServiceRunning(this, RatePollingService.class)) {
            if (!alarmsRepository.hasAnyActive()) {
                stopService(new Intent(this, RatePollingService.class));
                ProvidersManager.getInstance().stopAll();
            } else {
                TimeIntervalManager.setAlarmMode(true);
                for (CurrencySource source : SourcesManager.getCurrencySources()) {
                    BasePoolingProvider<?> provider = (BasePoolingProvider<?>) source.getPollingSource();
                    if (provider != null) {
                        provider.stopIfHasAlarm();
                    }
                }
            }
        } else {
            if (!alarmsRepository.hasAnyActive()) {
                ProvidersManager.getInstance().stopAll();
            } else {
                Intent intent = new Intent(this, RatePollingService.class);
                ContextCompat.startForegroundService(this, intent);
            }
        }
        super.onDestroy();
    }

    private void updateCards(List<BaseRate> rates, int source_type, boolean animated) {
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

    private void updateHint() {
        tvIntervalHint.setText(getString(R.string.interval_hint, TimeIntervalManager.getSelectionStr()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ServiceUtils.isMyServiceRunning(this, RatePollingService.class)) {
            stopService(new Intent(this, RatePollingService.class));
        }
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
        }
        return super.onOptionsItemSelected(item);
    }

    static class CardViewItemParent {
        ViewGroup me;
        /**
         * Refers to {@link IRate#getRateType()}
         **/
        int rate_type;

        List<CardViewItem> items = new ArrayList<>();

        @NonNull
        @Override
        public String toString() {
            StringBuilder itemsToString = new StringBuilder();
            for (CardViewItem item : items) {
                itemsToString.append("\n").append(item.toString());
            }
            return itemsToString + "\nType : " + rate_type;
        }
    }

    static class CardViewItem {

        @BindView(R.id.tv_type)
        TextView tvType;

        @BindView(R.id.tv_rate_value)
        TextView tvValue;

        View card;
        int value_type;
        /**
         * Refers to {@link CurrencySource#getType()}
         */
        int source_type;

        CardViewItem(View card, int source_type, int value_type) {
            this.card = card;
            this.source_type = source_type;
            this.value_type = value_type;
            ButterKnife.bind(this, card);
        }
    }
}
