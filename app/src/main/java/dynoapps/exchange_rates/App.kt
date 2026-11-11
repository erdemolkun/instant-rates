package dynoapps.exchange_rates

import android.app.Application
import android.content.Context
import dynoapps.exchange_rates.alarm.AlarmsRepository
import dynoapps.exchange_rates.util.L
import io.reactivex.rxjava3.plugins.RxJavaPlugins

class App : Application() {

    fun provideAlarmsRepository(): AlarmsRepository = AlarmsRepository.getInstance(applicationContext)

    override fun onCreate() {
        super.onCreate()
        instance = this
        RxJavaPlugins.setErrorHandler { throwable -> L.e("App", throwable.localizedMessage) }
        registerActivityLifecycleCallbacks(AppVisibilityTracker())
    }

    companion object {
        private lateinit var instance: App

        fun getInstance(): App = instance

        fun context(): Context = instance.applicationContext
    }
}
