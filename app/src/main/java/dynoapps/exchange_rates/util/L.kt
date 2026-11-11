package dynoapps.exchange_rates.util

import android.util.Log

object L {
    private const val TAG = "ExchangeRates"
    private const val MAX_LOG_TAG_LENGTH = 23

    private fun makeLogTag(tag: String): String =
        if (tag.length > MAX_LOG_TAG_LENGTH) tag.substring(0, MAX_LOG_TAG_LENGTH - 1) else tag

    fun e(tag: String, msg: String?) {
        Log.e(makeLogTag(tag), msg.orEmpty())
    }

    fun i(tag: String, msg: String) {
        Log.i(makeLogTag(tag), msg)
    }

    fun ex(throwable: Throwable?, message: String = "") {
        Log.e(TAG, message, throwable)
    }
}
