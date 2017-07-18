package dynoapps.exchange_rates.alarm;

/**
 * Created by erdemmac on 18/07/2017.
 */

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.database.Cursor;

import java.util.List;

import io.reactivex.Flowable;

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
    long insert(Alarm alarm);

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
    List<Alarm> list();


    /**
     * Delete a alarm by the ID.
     *
     * @param id The row ID.
     * @return A number of alarms deleted. This should always be {@code 1}.
     */
    @Query("DELETE FROM " + Alarm.TABLE_NAME + " WHERE " + Alarm.COLUMN_ID + " = :id")
    int deleteById(long id);

    /**
     * Update the alarm. The alarm is identified by the row ID.
     *
     * @param alarm The alarm to update.
     * @return A number of alarms updated. This should always be {@code 1}.
     */
    @Update
    int update(Alarm alarm);

}