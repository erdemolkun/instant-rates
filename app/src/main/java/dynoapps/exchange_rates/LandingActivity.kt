package dynoapps.exchange_rates

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.google.android.material.bottomappbar.BottomAppBar
import dynoapps.exchange_rates.alarm.AlarmManager
import dynoapps.exchange_rates.data.RatesHolder
import dynoapps.exchange_rates.interfaces.ValueType
import dynoapps.exchange_rates.model.rates.AvgRate
import dynoapps.exchange_rates.model.rates.BaseRate
import dynoapps.exchange_rates.model.rates.BuySellRate
import dynoapps.exchange_rates.model.rates.IRate
import dynoapps.exchange_rates.time.TimeIntervalManager
import dynoapps.exchange_rates.util.RateUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class LandingActivity : BaseServiceActivity() {

    private lateinit var tvIntervalHint: TextView
    private lateinit var bottomAppBar: BottomAppBar
    private val parentItems = ArrayList<CardViewItemParent>()

    // Alarm notifications are simply not shown when the permission is denied.
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override val layoutId: Int = R.layout.activity_landing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tvIntervalHint = findViewById(R.id.tv_interval_hint)
        bottomAppBar = findViewById(R.id.bottomAppBar)

        // Load saved alarms now: going to the background only starts the polling service if the
        // repository already knows there is an active alarm.
        App.getInstance().provideAlarmsRepository().getAlarms { }

        ProvidersManager.startSources()
        compositeDisposable.add(ProvidersManager.registerIntervalUpdates())
        compositeDisposable.add(
            SourcesManager.sourceUpdates.observeOn(AndroidSchedulers.mainThread()).subscribe {
                ProvidersManager.startOrStopSources()
                refreshCardItemViews()
            }
        )

        setUpBottomAppBar()
        requestNotificationPermissionIfNeeded()
        findViewById<View>(R.id.v_fab_add_alarm).setOnClickListener { AlarmManager.addAlarmDialog(this) }

        setUpRateCardViews()
        refreshCardItemViews()

        // Show the cached rates until the next fetch arrives.
        for (source in SourcesManager.getCurrencySources()) {
            if (!source.isEnabled) continue
            RatesHolder.getLatestEvent(source.type)?.let { updateCards(it.rates, it.sourceType, animated = false) }
        }

        updateHint()
        compositeDisposable.add(
            TimeIntervalManager.intervalUpdates.observeOn(AndroidSchedulers.mainThread()).subscribe { updateHint() }
        )
        compositeDisposable.add(
            ProvidersManager.ratesEventPublishSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { event -> updateCards(event.rates, event.sourceType, animated = true) }
        )
    }

    private fun setUpBottomAppBar() {
        setSupportActionBar(bottomAppBar)
        bottomAppBar.setOnMenuItemClickListener { item -> handleMenuItem(item.itemId) }
        bottomAppBar.setNavigationOnClickListener { showNavigationSheet() }
    }

    private fun handleMenuItem(itemId: Int): Boolean = when (itemId) {
        R.id.menu_time_interval -> {
            TimeIntervalManager.selectInterval(this)
            true
        }
        R.id.menu_item_sources -> {
            SourcesManager.selectSources(this)
            true
        }
        else -> false
    }

    /** Shows the cards of enabled sources, adding cards for newly enabled ones. */
    private fun refreshCardItemViews() {
        for (source in SourcesManager.getCurrencySources()) {
            for (parent in parentItems) {
                TransitionManager.beginDelayedTransition(parent.container)
                val cards = parent.items.filter { it.sourceType == source.type }
                cards.forEach { it.card.visibility = if (source.isEnabled) View.VISIBLE else View.GONE }
                if (cards.isEmpty() && source.isEnabled && source.isRateSupported(parent.rateType)) {
                    if (SourcesManager.isAvgType(source.type)) {
                        addCardToParent(parent, ValueType.AVG, source.type)
                    } else {
                        addCardToParent(parent, ValueType.BUY, source.type)
                        addCardToParent(parent, ValueType.SELL, source.type)
                    }
                }
            }
        }
    }

    private fun addCardToParent(parent: CardViewItemParent, valueType: Int, sourceType: Int) {
        val card = LayoutInflater.from(this).inflate(R.layout.layout_simple_rate_card, parent.container, false)
        parent.container.addView(card)
        card.setOnClickListener {
            startActivity(
                Intent(this, ChartActivity::class.java).putExtra(ChartActivity.EXTRA_RATE_TYPE, parent.rateType)
            )
        }
        card.setOnLongClickListener {
            val rate = RateUtils.getRate(RatesHolder.getLatestEvent(sourceType)?.rates, parent.rateType)
            if (rate != null) {
                AlarmManager.addAlarmDialog(this, sourceType, rate.rateType, valueType, rate.getValue(valueType))
            } else {
                AlarmManager.addAlarmDialog(this, sourceType, parent.rateType, valueType, null)
            }
            true
        }
        val item = CardViewItem(card, sourceType, valueType)
        item.tvType.text = SourcesManager.getSourceName(sourceType, valueType)
        parent.items.add(item)
    }

    private fun setUpRateCardViews() {
        parentItems += CardViewItemParent(findViewById(R.id.v_card_holder_usd), IRate.USD)
        parentItems += CardViewItemParent(findViewById(R.id.v_card_holder_eur), IRate.EUR)
        parentItems += CardViewItemParent(findViewById(R.id.v_card_holder_parity), IRate.EUR_USD)
        parentItems += CardViewItemParent(findViewById(R.id.v_card_holder_ons), IRate.ONS)
        parentItems += CardViewItemParent(findViewById(R.id.v_card_holder_ons_try), IRate.ONS_TRY)
    }

    private fun updateCards(rates: List<BaseRate>, sourceType: Int, animated: Boolean) {
        for (parent in parentItems) {
            val rate = RateUtils.getRate(rates, parent.rateType) ?: continue
            for (item in parent.items) {
                if (item.sourceType != sourceType) continue
                val value: Float? = when {
                    rate is BuySellRate && item.valueType == ValueType.SELL -> rate.valueSellReal
                    rate is BuySellRate && item.valueType == ValueType.BUY -> rate.valueBuyReal
                    rate is AvgRate -> rate.val_real_avg
                    else -> null
                }
                item.tvValue.text = value?.let(rate::getFormatted).orEmpty()
                if (animated) playFadeOutInAnim(item.tvValue)
            }
        }
    }

    private fun playFadeOutInAnim(view: View) {
        val fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, .3f).apply {
            duration = 450
            interpolator = FastOutSlowInInterpolator()
        }
        val fadeIn = ObjectAnimator.ofFloat(view, "alpha", .3f, 1f).apply {
            duration = 450
            interpolator = LinearOutSlowInInterpolator()
        }
        AnimatorSet().apply {
            play(fadeIn).after(fadeOut)
            start()
        }
    }

    /** Alarm and background polling notifications need the runtime permission on Android 13+. */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun updateHint() {
        tvIntervalHint.text = getString(R.string.interval_hint, TimeIntervalManager.getSelectionStr())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_landing, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        handleMenuItem(item.itemId) || super.onOptionsItemSelected(item)

    /** The card container for one rate type (USD, EUR, ...). */
    private class CardViewItemParent(val container: ViewGroup, @IRate.RateDef val rateType: Int) {
        val items = ArrayList<CardViewItem>()
    }

    /** One card: a source's buy, sell or average value for the parent's rate type. */
    private class CardViewItem(val card: View, val sourceType: Int, val valueType: Int) {
        val tvType: TextView = card.findViewById(R.id.tv_type)
        val tvValue: TextView = card.findViewById(R.id.tv_rate_value)
    }
}
