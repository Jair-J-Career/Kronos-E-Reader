package com.kronos.data.database.converter

import androidx.room.TypeConverter
import com.kronos.domain.model.ReadingStatus

class RoomTypeConverters {

    @TypeConverter
    fun fromReadingStatus(status: ReadingStatus): String = status.name

    @TypeConverter
    fun toReadingStatus(value: String): ReadingStatus = ReadingStatus.valueOf(value)
}
