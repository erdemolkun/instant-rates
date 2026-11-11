package dynoapps.exchange_rates.data

/**
 * Rate source ids. They are stored in preferences and in alarms, so ids are never reused:
 * 2 (Enpara), 4 (TlKur), 6 (Yahoo) and 7 (Paragaranti) belonged to sources that were removed
 * because their endpoints no longer exist.
 */
object CurrencyType {
    const val NOT_SET = 0
    const val ALTININ = 1
    const val BIGPARA = 3
    const val YAPIKREDI = 5
    const val BLOOMBERGHT = 8
}
