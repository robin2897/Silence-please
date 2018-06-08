package com.inc.rims.silenceplease.util

import java.util.*

class Validation {
    fun checkTodayDayMatch(days: List<Int>): Boolean {
        val now = Calendar.getInstance()
        val today = now.get(Calendar.DAY_OF_WEEK)
        return days[today - 1] == 1
    }

     fun checkIsTimeAfterNow(milliTime: Long): Boolean {
        val nowAtEpochStart = Calendar.getInstance()
        nowAtEpochStart.set(Calendar.YEAR, 1970)
        nowAtEpochStart.set(Calendar.MONTH, Calendar.JANUARY)
        nowAtEpochStart.set(Calendar.DATE, 1)

        return milliTime > nowAtEpochStart.timeInMillis
    }
}