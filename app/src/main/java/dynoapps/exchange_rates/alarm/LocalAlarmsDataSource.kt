package dynoapps.exchange_rates.alarm

import android.annotation.SuppressLint
import android.content.Context
import dynoapps.exchange_rates.AppDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class LocalAlarmsDataSource(context: Context) : AlarmsDataSource {

    private val alarmDao: AlarmDao = AppDatabase.getInstance(context.applicationContext).alarm()

    @SuppressLint("CheckResult")
    override fun getAlarms(callback: (List<Alarm>) -> Unit) {
        alarmDao.list()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ alarms -> callback(alarms) }, { throwable ->
                Timber.tag(TAG).e(throwable, "Loading alarms failed")
                callback(emptyList())
            })
    }

    @SuppressLint("CheckResult")
    override fun saveAlarm(alarm: Alarm, callback: ((Alarm) -> Unit)?) {
        alarmDao.insert(alarm)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ id ->
                alarm.id = id
                callback?.invoke(alarm)
            }, { throwable -> Timber.tag(TAG).e(throwable, "Saving alarm failed") })
    }

    override fun deleteAlarm(alarm: Alarm): Single<Alarm> = alarmDao.deleteById(alarm.id).map { alarm }

    override fun updateAlarm(alarm: Alarm): Single<Alarm> = alarmDao.update(alarm).map { alarm }

    override fun refreshAlarms() {
        // Not required: AlarmsRepository decides when to reload from this data source.
    }

    private companion object {
        const val TAG = "LocalAlarmsDataSource"
    }
}
