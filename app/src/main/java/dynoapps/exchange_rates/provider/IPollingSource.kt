package dynoapps.exchange_rates.provider

interface IPollingSource {

    /** One of [dynoapps.exchange_rates.data.CurrencyType]'s constants. */
    val sourceType: Int

    fun oneShot()

    fun start()

    fun stop()

    fun cancel()

    fun refreshIntervals(immediateShort: Boolean)

    fun stopNonAlarmSources()
}
