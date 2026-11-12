package dynoapps.exchange_rates

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.isVisible
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
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

    /**
     * Shows the cards of enabled sources, adding cards for newly enabled ones, and a "no provider
     * found" label in rows that no enabled source provides.
     */
    private fun refreshCardItemViews() {
        for (source in SourcesManager.getCurrencySources()) {
            for (parent in parentItems) {
                TransitionManager.beginDelayedTransition(parent.container)
                val cards = parent.items.filter { it.sourceType == source.type }
                cards.forEach { it.card.isVisible = source.isEnabled }
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
        for (parent in parentItems) {
            parent.noProviderView.isVisible = parent.items.none { it.card.isVisible }
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
        parentItems += createParent(R.id.v_card_holder_usd, IRate.USD)
        parentItems += createParent(R.id.v_card_holder_eur, IRate.EUR)
        parentItems += createParent(R.id.v_card_holder_parity, IRate.EUR_USD)
        parentItems += createParent(R.id.v_card_holder_ons, IRate.ONS)
        parentItems += createParent(R.id.v_card_holder_ons_try, IRate.ONS_TRY)
    }

    private fun createParent(@IdRes containerId: Int, @IRate.RateDef rateType: Int): CardViewItemParent {
        val container: ViewGroup = findViewById(containerId)
        val noProviderView =
            LayoutInflater.from(this).inflate(R.layout.layout_no_provider, container, false) as TextView
        setNoProviderText(noProviderView)
        noProviderView.movementMethod = LinkMovementMethod.getInstance()
        // Right after the row's icon; rate cards are appended after it.
        container.addView(noProviderView, 1)
        limitToVisibleWidth(noProviderView, container)
        return CardViewItemParent(container, rateType, noProviderView)
    }

    /**
     * "No supported provider found, click ⚙ to change", with the bottom bar's settings icon in the text.
     * Clicking the icon opens the source selection. Screen readers read the word "Settings" instead.
     */
    private fun setNoProviderText(label: TextView) {
        val template = getString(R.string.no_provider_found)
        val start = template.indexOf(PLACEHOLDER)
        label.contentDescription = template.replaceFirst(PLACEHOLDER, getString(R.string.no_provider_found_settings))
        // Screen readers can't reach the icon link, so the label itself opens the source selection.
        ViewCompat.replaceAccessibilityAction(
            label,
            AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK,
            getString(R.string.select_sources),
        ) { _, _ ->
            SourcesManager.selectSources(this)
            true
        }
        val icon = ContextCompat.getDrawable(this, R.drawable.ic_settings_white_24dp)?.mutate()
        if (start < 0 || icon == null) {
            label.text = label.contentDescription
            return
        }
        val size = (label.textSize * 1.3f).toInt()
        icon.setBounds(0, 0, size, size)
        icon.setTint(label.currentTextColor)
        val alignment = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            DynamicDrawableSpan.ALIGN_CENTER
        } else {
            DynamicDrawableSpan.ALIGN_BOTTOM
        }
        val settingsLink = object : ClickableSpan() {
            override fun onClick(widget: View) = SourcesManager.selectSources(this@LandingActivity)

            // The icon is the link; no underline or link colour.
            override fun updateDrawState(ds: TextPaint) = Unit
        }
        label.text = SpannableString(template.replaceFirst(PLACEHOLDER, ICON_CHAR)).apply {
            setSpan(ImageSpan(icon, alignment), start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(settingsLink, start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    /**
     * Rows scroll horizontally, so [label] would get unlimited width and run off the screen. Limits
     * it to the row's visible width, updated when that changes (on rotation), so the text wraps.
     */
    private fun limitToVisibleWidth(label: TextView, container: ViewGroup) {
        val scrollView = container.parent as View
        scrollView.addOnLayoutChangeListener { _, left, _, right, _, _, _, _, _ ->
            val icon = container.getChildAt(0)
            val usedWidth = icon.right + icon.marginEnd + label.marginStart + label.marginEnd + container.paddingEnd
            val maxWidth = (right - left - usedWidth).coerceAtLeast(0)
            // Changing the width requests a layout, which can't happen during this one.
            if (label.maxWidth != maxWidth) label.post { label.maxWidth = maxWidth }
        }
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

    private companion object {
        const val PLACEHOLDER = "%1\$s"

        // Object replacement character: the place an ImageSpan draws its image.
        const val ICON_CHAR = "\uFFFC"
    }

    /** The card container for one rate type (USD, EUR, ...). */
    private class CardViewItemParent(
        val container: ViewGroup,
        @IRate.RateDef val rateType: Int,
        val noProviderView: View,
    ) {
        val items = ArrayList<CardViewItem>()
    }

    /** One card: a source's buy, sell or average value for the parent's rate type. */
    private class CardViewItem(val card: View, val sourceType: Int, val valueType: Int) {
        val tvType: TextView = card.findViewById(R.id.tv_type)
        val tvValue: TextView = card.findViewById(R.id.tv_rate_value)
    }
}
