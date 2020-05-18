package dynoapps.exchange_rates.alarm;

import android.provider.BaseColumns;

import java.io.Serializable;
import java.util.Comparator;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
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
    public static Comparator<Alarm> COMPARATOR = (first, second) -> {

        int i = compare(first.sourceType, second.sourceType);
        if (i != 0) return i;

        i = compare(first.rateType, second.rateType);
        if (i != 0) return i;

        i = compare(first.val, second.val);
        return i;

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
    public boolean isAbove = false;
    @ColumnInfo(name = "is_enabled")
    public boolean isEnabled = true;
    /**
     * {@link dynoapps.exchange_rates.model.rates.IRate}
     */
    @ColumnInfo(name = "rate_type")
    public int rateType;
    /**
     * {@link dynoapps.exchange_rates.data.CurrencyType}
     */
    @ColumnInfo(name = "source_type")
    public int sourceType;
    /**
     * {@link dynoapps.exchange_rates.interfaces.ValueType}
     */
    public int value_type = ValueType.NONE;

    public static int getPushId(Alarm alarm) {
        return alarm.rateType * 100 + alarm.sourceType;
    }

    private static int compare(int x, int y) {
        return Integer.compare(x, y);
    }

    private static int compare(float x, float y) {
        return Float.compare(x, y);
    }
}
