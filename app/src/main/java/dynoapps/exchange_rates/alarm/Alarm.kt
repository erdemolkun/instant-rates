package dynoapps.exchange_rates.alarm

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dynoapps.exchange_rates.interfaces.ValueType
import java.io.Serializable

/**
 * A price alarm. Fields are exposed with [JvmField] so Room sees exactly the same columns as the
 * original Java entity; the database has no migrations, so the schema must not change.
 */
@Entity(tableName = Alarm.TABLE_NAME)
class Alarm : Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = COLUMN_ID)
    @JvmField
    var id: Long = 0

    @ColumnInfo(name = "value")
    @JvmField
    var `val`: Float? = null

    @ColumnInfo(name = "is_above")
    @JvmField
    var isAbove: Boolean = false

    @ColumnInfo(name = "is_enabled")
    @JvmField
    var isEnabled: Boolean = true

    /** One of [dynoapps.exchange_rates.model.rates.IRate]'s constants. */
    @ColumnInfo(name = "rate_type")
    @JvmField
    var rateType: Int = 0

    /** One of [dynoapps.exchange_rates.data.CurrencyType]'s constants. */
    @ColumnInfo(name = "source_type")
    @JvmField
    var sourceType: Int = 0

    /** One of [ValueType]'s constants. */
    @JvmField
    var value_type: Int = ValueType.NONE

    companion object {
        const val TABLE_NAME = "tables"
        const val COLUMN_ID = "_id"

        @JvmField
        val COMPARATOR: Comparator<Alarm> = compareBy<Alarm>({ it.sourceType }, { it.rateType })
            .thenBy { it.`val` ?: 0f }

        fun getPushId(alarm: Alarm): Int = alarm.rateType * 100 + alarm.sourceType
    }
}
