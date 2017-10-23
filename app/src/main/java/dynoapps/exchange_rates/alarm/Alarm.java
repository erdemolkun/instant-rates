package dynoapps.exchange_rates.alarm;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

import java.io.Serializable;
import java.util.Comparator;

import dynoapps.exchange_rates.interfaces.ValueType;

/**
 * Created by erdemmac on 13/12/2016.
 */
@Entity(tableName = Alarm.TABLE_NAME)
public class Alarm implements Serializable {

    /**
     * The name of the Alarm table.
     */
    public static final String TABLE_NAME = "tables";

    /**
     * The name of the ID column.
     */
    public static final String COLUMN_ID = BaseColumns._ID;
    public static Comparator<Alarm> COMPARATOR = new Comparator<Alarm>() {

        @Override
        public int compare(Alarm first, Alarm second) {

            int i = Alarm.compare(first.source_type, second.source_type);
            if (i != 0) return i;

            i = Alarm.compare(first.rate_type, second.rate_type);
            if (i != 0) return i;

            i = Alarm.compare(first.val, second.val);
            return i;

        }
    };
    /**
     * The unique ID of the alarm.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = COLUMN_ID)
    public long id;
    @ColumnInfo(name = "value")
    public Float val;
    @ColumnInfo(name = "is_above")
    public boolean is_above = false;
    @ColumnInfo(name = "is_enabled")
    public boolean is_enabled = true;
    /**
     * {@link dynoapps.exchange_rates.model.rates.IRate}
     */
    @ColumnInfo(name = "rate_type")
    public int rate_type;
    /**
     * {@link dynoapps.exchange_rates.data.CurrencyType}
     */
    @ColumnInfo(name = "source_type")
    public int source_type;
    /**
     * {@link dynoapps.exchange_rates.interfaces.ValueType}
     */
    public int value_type = ValueType.NONE;

    public static int getPushId(Alarm alarm) {
        return alarm.rate_type * 100 + alarm.source_type;
    }

    private static int compare(int x, int y) {
        return x < y ? -1
                : x > y ? 1
                : 0;
    }

    private static int compare(float x, float y) {
        return x < y ? -1
                : x > y ? 1
                : 0;
    }
}
