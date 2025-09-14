package com.roocode.android.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.roocode.android.data.dao.MessageDao
import com.roocode.android.data.dao.TaskDao
import com.roocode.android.data.dao.AgentDao
import com.roocode.android.data.dao.McpServerDao
import com.roocode.android.data.models.Message
import com.roocode.android.data.models.Task
import com.roocode.android.data.models.Agent
import com.roocode.android.data.models.McpServer
import java.util.Date

@Database(
    entities = [Message::class, Task::class, Agent::class, McpServer::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class RooCodeDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun taskDao(): TaskDao
    abstract fun agentDao(): AgentDao
    abstract fun mcpServerDao(): McpServerDao

    companion object {
        @Volatile
        private var INSTANCE: RooCodeDatabase? = null

        fun getDatabase(context: Context): RooCodeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RooCodeDatabase::class.java,
                    "roocode_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
}