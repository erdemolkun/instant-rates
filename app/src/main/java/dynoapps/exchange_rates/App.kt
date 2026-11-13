package dynoapps.exchange_rates

import android.app.Application
import android.content.Context
import dynoapps.exchange_rates.alarm.AlarmsRepository
import timber.log.Timber

class App : Application() {

    fun provideAlarmsRepository(): AlarmsRepository = AlarmsRepository.getInstance(applicationContext)

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Release builds plant no tree, so they log nothing.
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        registerActivityLifecycleCallbacks(AppVisibilityTracker())
    }

    companion object {
        private lateinit var instance: App

        fun getInstance(): App = instance

        fun context(): Context = instance.applicationContext
    }
}
