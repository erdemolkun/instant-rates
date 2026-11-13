package dynoapps.exchange_rates

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dynoapps.exchange_rates.alarm.Alarm
import dynoapps.exchange_rates.alarm.AlarmDao

@Database(entities = [Alarm::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun alarm(): AlarmDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "alarms")
                    .build()
                    .also { instance = it }
            }
    }
}
