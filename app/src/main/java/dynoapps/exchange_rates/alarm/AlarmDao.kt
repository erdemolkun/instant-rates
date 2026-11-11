package dynoapps.exchange_rates.alarm

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.reactivex.rxjava3.core.Single

/** Data access object for [Alarm]. */
@Dao
interface AlarmDao {

    /** @return the row ID of the newly inserted alarm. */
    @Insert
    fun insert(alarm: Alarm): Single<Long>

    @Query("SELECT * FROM " + Alarm.TABLE_NAME)
    fun list(): Single<List<Alarm>>

    /** @return the number of deleted alarms, which should always be 1. */
    @Query("DELETE FROM " + Alarm.TABLE_NAME + " WHERE " + Alarm.COLUMN_ID + " = :id")
    fun deleteById(id: Long): Single<Int>

    /** @return the number of updated alarms, which should always be 1. */
    @Update
    fun update(alarm: Alarm): Single<Int>
}
