package com.meditrack.app.util

object Config {
    /** A dose with no response after this long is recorded as MISSED. */
    const val MISSED_AFTER_MS = 60 * 60 * 1000L
    const val SNOOZE_MS = 10 * 60 * 1000L
    const val LOW_STOCK_THRESHOLD = 5
}
