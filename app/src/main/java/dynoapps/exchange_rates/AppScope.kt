package dynoapps.exchange_rates

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * App-wide main-thread scope for work that must outlive the screen or provider that started it,
 * such as writing an alarm. It is never cancelled.
 */
object AppScope {

    // Main.immediate: started on the main thread, work runs right away until it first suspends.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** Launches [block], logging [errorMessage] under [tag] if it fails. */
    fun launch(tag: String, errorMessage: String, block: suspend CoroutineScope.() -> Unit): Job =
        scope.launch(CoroutineExceptionHandler { _, throwable -> Timber.tag(tag).e(throwable, errorMessage) }, block = block)
}
