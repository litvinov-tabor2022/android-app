package cz.jenda.tabor2022.data

import androidx.room.TypeConverter
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import java.time.Instant

class Converters {
    @TypeConverter
    fun toInstant(secs: Long?): Instant? {
        return secs?.let { Instant.ofEpochSecond(it) }
    }

    @TypeConverter
    fun fromInstant(instant: Instant?): Long? {
        return instant?.epochSecond
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): kotlinx.datetime.Instant? {
        return value?.let { kotlinx.datetime.Instant.fromEpochMilliseconds(value) }
    }

    @TypeConverter
    fun dateToTimestamp(instant: kotlinx.datetime.Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }
}