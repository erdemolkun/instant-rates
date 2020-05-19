package dynoapps.exchange_rates.alarm;

/**
 * Created by erdemmac on 18/07/2017.
 */

import android.database.Cursor;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * Data access object for Alarm.
 */
@Dao
public interface AlarmDao {

    /**
     * Inserts a alarm into the table.
     *
     * @param alarm A new alarm.
     * @return The row ID of the newly inserted alarm.
     */
    @Insert
    Single<Long> insert(Alarm alarm);

    /**
     * Inserts multiple alarms into the database
     *
     * @param alarms An array of new alarms.
     * @return The row IDs of the newly inserted alarms.
     */
    @Insert
    long[] insertAll(Alarm[] alarms);

    /**
     * Select all alarms.
     *
     * @return A {@link Cursor} of all the cheeses in the table.
     */
    @Query("SELECT * FROM " + Alarm.TABLE_NAME)
    Flowable<List<Alarm>> selectAll();

    /**
     * Select all alarms.
     *
     * @return A {@link Cursor} of all the cheeses in the table.
     */
    @Query("SELECT * FROM " + Alarm.TABLE_NAME)
    Single<List<Alarm>> list();


    /**
     * Delete a alarm by the ID.
     *
     * @param id The row ID.
     * @return A number of alarms deleted. This should always be {@code 1}.
     */
    @Query("DELETE FROM " + Alarm.TABLE_NAME + " WHERE " + Alarm.COLUMN_ID + " = :id")
    Single<Integer> deleteById(long id);

    /**
     * Update the alarm. The alarm is identified by the row ID.
     *
     * @param alarm The alarm to update.
     * @return A number of alarms updated. This should always be {@code 1}.
     */
    @Update
    Single<Integer> update(Alarm alarm);

}