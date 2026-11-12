package dynoapps.exchange_rates.alarm

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/** Data access object for [Alarm]. */
@Dao
interface AlarmDao {

    /** @return the row ID of the newly inserted alarm. */
    @Insert
    suspend fun insert(alarm: Alarm): Long

    @Query("SELECT * FROM " + Alarm.TABLE_NAME)
    suspend fun list(): List<Alarm>

    /** @return the number of deleted alarms, which should always be 1. */
    @Query("DELETE FROM " + Alarm.TABLE_NAME + " WHERE " + Alarm.COLUMN_ID + " = :id")
    suspend fun deleteById(id: Long): Int

    /** @return the number of updated alarms, which should always be 1. */
    @Update
    suspend fun update(alarm: Alarm): Int
}
