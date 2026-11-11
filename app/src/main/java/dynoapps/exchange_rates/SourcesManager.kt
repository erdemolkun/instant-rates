package dynoapps.exchange_rates

import android.app.Activity
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import dynoapps.exchange_rates.data.CurrencySource
import dynoapps.exchange_rates.data.CurrencyType
import dynoapps.exchange_rates.interfaces.ValueType
import dynoapps.exchange_rates.model.rates.IRate
import io.reactivex.rxjava3.subjects.PublishSubject

/** The rate sources the app knows about and which of them the user enabled. */
object SourcesManager {

    /** Emits whenever the user changes the enabled sources. */
    val sourceUpdates: PublishSubject<Boolean> = PublishSubject.create()

    private val sources: List<CurrencySource> by lazy { createSources() }

    fun getCurrencySources(): List<CurrencySource> = sources

    fun getSource(sourceType: Int): CurrencySource? = sources.firstOrNull { it.type == sourceType }

    fun getSourceName(type: Int): String = getSource(type)?.name.orEmpty()

    fun getSourceName(type: Int, valueType: Int): String {
        val postFix = when (valueType) {
            ValueType.SELL -> " " + App.context().getString(R.string.sell)
            ValueType.BUY -> " " + App.context().getString(R.string.buy)
            else -> ""
        }
        return getSourceName(type) + postFix
    }

    fun isAvgType(type: Int): Boolean = type == CurrencyType.ALTININ || type == CurrencyType.BLOOMBERGHT

    fun selectSources(activity: Activity) {
        val checked = BooleanArray(sources.size) { sources[it].isEnabled }
        val names = Array(sources.size) { sources[it].name }

        val dialog = AlertDialog.Builder(activity, R.style.AppTheme_Alert)
            .setMultiChoiceItems(names, checked) { _, which, isChecked -> checked[which] = isChecked }
            .setCancelable(true)
            .setTitle(R.string.select_sources)
            .setPositiveButton(R.string.apply, null)
            .setNegativeButton(R.string.dismiss, null)
            .create()
        dialog.setOnShowListener {
            // Handle the click ourselves so the dialog stays open when nothing is selected.
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (checked.none { it }) {
                    Toast.makeText(activity, R.string.select_at_least_one_source, Toast.LENGTH_SHORT).show()
                } else {
                    sources.forEachIndexed { index, source -> source.isEnabled = checked[index] }
                    sourceUpdates.onNext(true)
                    saveSources()
                    dialog.dismiss()
                }
            }
        }
        dialog.window?.setWindowAnimations(R.style.DialogAnimationFade)
        dialog.show()
    }

    private fun saveSources() {
        Prefs.saveSources(sources.filter { it.isEnabled }.joinToString(separator = "") { "${it.type};" })
    }

    private fun color(@ColorRes colorRes: Int): Int = ContextCompat.getColor(App.context(), colorRes)

    private fun createSources(): List<CurrencySource> {
        val onlyUsdTry = intArrayOf(IRate.USD)
        val altinInSupported = intArrayOf(IRate.USD, IRate.EUR, IRate.EUR_USD, IRate.ONS)
        val yapikrediSupported = intArrayOf(IRate.USD, IRate.EUR, IRate.ONS_TRY)
        val bloombergSupported = intArrayOf(IRate.USD, IRate.EUR, IRate.EUR_USD, IRate.ONS)

        val sources = listOf(
            CurrencySource("Altın.in", CurrencyType.ALTININ, color(R.color.colorYorumlar), true, altinInSupported),
            CurrencySource("Bigpara", CurrencyType.BIGPARA, color(R.color.colorBigPara), false, onlyUsdTry),
            CurrencySource("Yapı Kredi", CurrencyType.YAPIKREDI, color(R.color.colorYapiKredi), false, yapikrediSupported),
            CurrencySource("Bloomberg HT", CurrencyType.BLOOMBERGHT, color(R.color.colorBloomberg), true, bloombergSupported),
        )

        // Average sources take one chart line, buy/sell sources take two.
        var index = 0
        for (source in sources) {
            source.chartIndex = index
            index += if (isAvgType(source.type)) 1 else 2
        }

        applySavedSelection(sources)
        return sources
    }

    private fun applySavedSelection(sources: List<CurrencySource>) {
        val saved = Prefs.getSources()
        if (saved.isNullOrEmpty()) return
        val enabledTypes = saved.split(";").mapNotNull { it.toIntOrNull() }.toSet()
        // A saved selection made only of removed sources would leave nothing enabled: keep the
        // defaults instead.
        if (sources.none { it.type in enabledTypes }) return
        sources.forEach { it.isEnabled = it.type in enabledTypes }
    }
}
