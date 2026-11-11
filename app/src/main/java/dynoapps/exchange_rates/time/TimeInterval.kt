package dynoapps.exchange_rates.time

import dynoapps.exchange_rates.App
import dynoapps.exchange_rates.R
import java.util.concurrent.TimeUnit

class TimeInterval internal constructor(private val value: Int, private val timeUnit: TimeUnit) {

    fun to(unit: TimeUnit): Long = unit.convert(value.toLong(), timeUnit)

    override fun toString(): String {
        val resources = App.context().resources
        return when (timeUnit) {
            TimeUnit.SECONDS -> resources.getQuantityString(R.plurals.sec_short, value, value)
            TimeUnit.MINUTES -> resources.getQuantityString(R.plurals.min_short, value, value)
            else -> value.toString()
        }
    }
}
