package dynoapps.exchange_rates.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dynoapps.exchange_rates.util.L
import io.reactivex.rxjava3.subjects.PublishSubject

/** Handles the "Stop" action of the background polling notification. */
class ServiceStopActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        stopSubject.onNext(true)
        try {
            context.stopService(Intent(context, RatePollingService::class.java))
        } catch (e: Exception) {
            L.ex(e, "ServiceStopActionReceiver")
        }
    }

    companion object {
        val stopSubject: PublishSubject<Boolean> = PublishSubject.create()
    }
}
