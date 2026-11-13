package dynoapps.exchange_rates.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber

/** Handles the "Stop" action of the background polling notification. */
class ServiceStopActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        _stopRequests.tryEmit(Unit)
        try {
            context.stopService(Intent(context, RatePollingService::class.java))
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Stopping the polling service failed")
        }
    }

    companion object {
        // Log tags are limited to 23 characters before Android 8.0.
        private const val TAG = "ServiceStopReceiver"

        private val _stopRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

        /** Emits when the user taps "Stop" on the notification. */
        val stopRequests: SharedFlow<Unit> = _stopRequests.asSharedFlow()
    }
}
