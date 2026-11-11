package dynoapps.exchange_rates.provider

import android.os.Handler
import android.os.Looper
import androidx.annotation.VisibleForTesting
import dynoapps.exchange_rates.App
import dynoapps.exchange_rates.SourcesManager
import dynoapps.exchange_rates.model.rates.BaseRate
import dynoapps.exchange_rates.time.TimeIntervalManager
import dynoapps.exchange_rates.util.L
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.ln

/**
 * Polls one rate source on the main looper at the interval chosen in [TimeIntervalManager] and
 * reports each successful fetch to [onResult].
 */
abstract class BasePoolingProvider<T : BaseRate>(
    private val onResult: (rates: List<T>, sourceType: Int) -> Unit,
) : IPollingSource {

    private val disposables = CompositeDisposable()
    private val handler = Handler(Looper.getMainLooper())
    private val fetchRunnable = Runnable { job(singleRun = false) }

    private var errorCount = 0
    private var successCount = 0

    /** True while a fetch is scheduled or running. */
    private val isWorking = AtomicBoolean(false)

    /** True between [start] and [stop]. */
    private val isStarted = AtomicBoolean(false)

    private val tag: String = javaClass.simpleName

    protected abstract fun getObservable(): Observable<List<T>>

    /** Fetches once on the calling thread. Used by device tests to check each source. */
    @VisibleForTesting
    fun fetchBlocking(): List<T> = getObservable().blockingFirst()

    private val isEnabled: Boolean
        get() = SourcesManager.getSource(sourceType)?.isEnabled == true

    override fun oneShot() {
        job(singleRun = true)
    }

    override fun cancel() {
        disposables.clear()
    }

    override fun start() {
        if (!isEnabled || isWorking.get() || isStarted.get()) return
        L.i(TAG, "$tag Started")
        postWork(0)
        isStarted.set(true)
    }

    override fun stop() {
        L.i(TAG, "$tag Stopped")
        cancelWorks()
        isStarted.set(false)
    }

    private fun fetchAgain(wasError: Boolean) {
        if (!isEnabled || !isStarted.get()) return
        val delay = if (wasError) {
            errorRetryDelay(errorCount, successCount)
        } else {
            TimeIntervalManager.getPollingInterval()
        }
        postWork(delay)
    }

    override fun refreshIntervals(immediateShort: Boolean) {
        // Stopped providers (e.g. non-alarm sources in the background) must stay stopped.
        if (!isStarted.get()) return
        cancelWorks()
        if (!isEnabled) return
        postWork(if (immediateShort) 0 else TimeIntervalManager.getPollingInterval())
    }

    private fun cancelWorks() {
        handler.removeCallbacks(fetchRunnable)
        cancel()
        isWorking.set(false)
    }

    private fun postWork(delayMillis: Long) {
        isWorking.set(true)
        handler.postDelayed(fetchRunnable, delayMillis)
        L.i(TAG, "- postWork : $delayMillis ms - $tag")
    }

    private fun notifyValue(rates: List<T>) {
        successCount++
        if (!isWorking.get()) return
        onResult(rates, sourceType)
    }

    override fun stopNonAlarmSources() {
        App.getInstance().provideAlarmsRepository().getAlarms { alarms ->
            if (alarms.none { it.sourceType == sourceType && it.isEnabled }) {
                stop()
            }
        }
    }

    private fun job(singleRun: Boolean) {
        disposables.add(
            Observable.defer { getObservable() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ rates ->
                    notifyValue(rates)
                    if (!singleRun) fetchAgain(wasError = false)
                }, { throwable ->
                    errorCount++
                    L.e(TAG, "$tag fetch failed: $throwable")
                    if (!singleRun) fetchAgain(wasError = true)
                })
        )
    }

    companion object {
        private const val TAG = "BasePoolingProvider"
        private const val NEXT_FETCH_ON_ERROR = 4000L
        private const val MAX_FETCH_ON_ERROR = 60000L

        /**
         * Delay before retrying after a failed fetch. Starts at [NEXT_FETCH_ON_ERROR] and grows
         * logarithmically with the share of failed fetches, capped at [MAX_FETCH_ON_ERROR]. A
         * previously healthy source never retries faster than the base delay.
         */
        internal fun errorRetryDelay(errorCount: Int, successCount: Int): Long {
            val ratio = errorCount / maxOf(1, successCount).toFloat()
            val growth = maxOf(0.0, ln(maxOf(1f, ratio).toDouble()))
            val delay = (NEXT_FETCH_ON_ERROR + growth * NEXT_FETCH_ON_ERROR).toLong()
            return minOf(delay, MAX_FETCH_ON_ERROR)
        }
    }
}
