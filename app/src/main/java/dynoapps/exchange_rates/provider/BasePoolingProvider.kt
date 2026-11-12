package dynoapps.exchange_rates.provider

import android.os.Handler
import android.os.Looper
import androidx.annotation.VisibleForTesting
import dynoapps.exchange_rates.App
import dynoapps.exchange_rates.SourcesManager
import dynoapps.exchange_rates.model.rates.BaseRate
import dynoapps.exchange_rates.time.TimeIntervalManager
import dynoapps.exchange_rates.AppScope
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.ln
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber

/**
 * Polls one rate source on the main looper at the interval chosen in [TimeIntervalManager] and
 * reports each successful fetch to [onResult].
 */
abstract class BasePoolingProvider<T : BaseRate>(
    private val onResult: (rates: List<T>, sourceType: Int) -> Unit,
) : IPollingSource {

    /** Runs the fetches on the main thread; [cancel] cancels the running ones. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val handler = Handler(Looper.getMainLooper())
    private val fetchRunnable = Runnable { job(singleRun = false) }

    private var errorCount = 0
    private var successCount = 0

    /** True while a fetch is scheduled or running. */
    private val isWorking = AtomicBoolean(false)

    /** True between [start] and [stop]. */
    private val isStarted = AtomicBoolean(false)

    /** Log tag: the concrete provider's class name. */
    private val tag: String = javaClass.simpleName

    /** Fetches the rates once. Main-safe. */
    protected abstract suspend fun fetch(): List<T>

    /** Fetches once, blocking the calling thread. Used by device tests to check each source. */
    @VisibleForTesting
    fun fetchBlocking(): List<T> = runBlocking { fetch() }

    private val isEnabled: Boolean
        get() = SourcesManager.getSource(sourceType)?.isEnabled == true

    override fun oneShot() {
        job(singleRun = true)
    }

    override fun cancel() {
        scope.coroutineContext.cancelChildren()
    }

    override fun start() {
        if (!isEnabled || isWorking.get() || isStarted.get()) return
        Timber.tag(tag).i("Started")
        postWork(0)
        isStarted.set(true)
    }

    override fun stop() {
        Timber.tag(tag).i("Stopped")
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
        Timber.tag(tag).i("postWork: $delayMillis ms")
    }

    private fun notifyValue(rates: List<T>) {
        successCount++
        if (!isWorking.get()) return
        onResult(rates, sourceType)
    }

    override fun stopNonAlarmSources() {
        AppScope.launch(tag, "Loading alarms failed") {
            val alarms = App.getInstance().provideAlarmsRepository().getAlarms()
            if (alarms.none { it.sourceType == sourceType && it.isEnabled }) {
                stop()
            }
        }
    }

    private fun job(singleRun: Boolean) {
        scope.launch {
            val rates = try {
                fetch()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorCount++
                Timber.tag(tag).e("Fetch failed: $e")
                if (!singleRun) fetchAgain(wasError = true)
                return@launch
            }
            notifyValue(rates)
            if (!singleRun) fetchAgain(wasError = false)
        }
    }

    companion object {
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
