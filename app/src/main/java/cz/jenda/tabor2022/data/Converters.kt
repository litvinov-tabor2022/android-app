package cz.jenda.tabor2022.data

import androidx.room.TypeConverter
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
}