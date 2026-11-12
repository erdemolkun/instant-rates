package dynoapps.exchange_rates.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber

/** Handles the "Stop" action of the background polling notification. */
class ServiceStopActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        stopSubject.onNext(true)
        try {
            context.stopService(Intent(context, RatePollingService::class.java))
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Stopping the polling service failed")
        }
    }

    companion object {
        // Log tags are limited to 23 characters before Android 8.0.
        private const val TAG = "ServiceStopReceiver"

        val stopSubject: PublishSubject<Boolean> = PublishSubject.create()
    }
}
