package com.inc.rims.silenceplease.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverter
import com.google.gson.annotations.SerializedName

@Entity(tableName = "timetable")
data class DataModel(
        @PrimaryKey
        var id: String,
        @SerializedName(value = "start_time")
        var startTime: Long,
        @SerializedName(value = "end_time")
        var endTime: Long,
        var days: List<Int>,
        @SerializedName(value = "is_active")
        var isActive: Boolean,
        @SerializedName(value = "is_silent")
        var isSilent: Boolean,
        @SerializedName(value = "is_vibrate")
        var isVibrate: Boolean
)

data class JsonArrayDataModel(
        var items: List<DataModel>
)

class DaysListConverter {
    @TypeConverter
    fun toList(value: String): List<Int> {
        return value.split("-").map { it.toInt() }
    }

    @TypeConverter
    fun toString(value: List<Int>): String {
        var str = ""
        var first = true
        value.forEach {
            str += if (first) "$it" else "-$it"
            first = false
        }
        return str
    }
}