package dynoapps.exchange_rates.model.rates

import androidx.annotation.IntDef

interface IRate {

    @get:RateDef
    val rateType: Int

    fun getValue(valueType: Int): Float

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(
        UNKNOWN.toLong(),
        USD.toLong(),
        EUR.toLong(),
        ONS.toLong(),
        ONS_TRY.toLong(),
        EUR_USD.toLong(),
    )
    annotation class RateDef

    companion object {
        const val UNKNOWN = 0
        const val USD = 1
        const val EUR = 2
        const val ONS = 3
        const val ONS_TRY = 4
        const val EUR_USD = 5
    }
}
