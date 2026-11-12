package dynoapps.exchange_rates.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import dynoapps.exchange_rates.LandingActivity
import dynoapps.exchange_rates.ProvidersManager
import dynoapps.exchange_rates.R
import dynoapps.exchange_rates.notification.NotificationHelper
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Foreground service that keeps the rate providers polling while the UI is closed so that price
 * alarms can still fire.
 */
class RatePollingService : Service() {

    private val scope = MainScope()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Timber.tag(TAG).i("onCreate")
        scope.launch { ServiceStopActionReceiver.stopRequests.collect { stopPolling() } }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // startForeground must be called promptly after startForegroundService on every start.
        showForegroundNotification()
        ProvidersManager.startSources()
        return START_STICKY
    }

    /** Called on Android 15+ once the daily time limit for dataSync foreground services is used up. */
    override fun onTimeout(startId: Int, fgsType: Int) {
        Timber.tag(TAG).i("Foreground service timed out")
        stopPolling()
    }

    override fun onDestroy() {
        Timber.tag(TAG).i("onDestroy")
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        scope.cancel()
        super.onDestroy()
    }

    private fun stopPolling() {
        ProvidersManager.stopAll()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun showForegroundNotification() {
        NotificationHelper.createConnectionChannelIfNotExists(this)

        val pushIntent = Intent(this, LandingActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            pushIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopActionPendingIntent = PendingIntent.getBroadcast(
            this,
            FOREGROUND_ID,
            Intent(this, ServiceStopActionReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopAction = NotificationCompat.Action.Builder(
            0,
            getString(R.string.notification_stop_action),
            stopActionPendingIntent,
        ).build()

        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_ID_CONNECTION)
            .setContentIntent(pendingIntent)
            .setContentText(getString(R.string.background_working))
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setWhen(0)
            .setOngoing(true)
            .addAction(stopAction)
            .setSmallIcon(R.drawable.ic_store_icon_24dp)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        } else {
            0
        }
        ServiceCompat.startForeground(this, FOREGROUND_ID, notification, type)
    }

    private companion object {
        const val TAG = "RatePollingService"
        const val FOREGROUND_ID = 1001
    }
}
