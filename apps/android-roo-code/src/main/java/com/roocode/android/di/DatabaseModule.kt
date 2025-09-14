package com.roocode.android.di

import android.content.Context
import androidx.room.Room
import com.roocode.android.data.RooCodeDatabase
import com.roocode.android.data.dao.MessageDao
import com.roocode.android.data.dao.TaskDao
import com.roocode.android.data.dao.AgentDao
import com.roocode.android.data.dao.McpServerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideRooCodeDatabase(@ApplicationContext context: Context): RooCodeDatabase {
        return Room.databaseBuilder(
            context,
            RooCodeDatabase::class.java,
            "roocode_database"
        ).build()
    }

    @Provides
    fun provideMessageDao(database: RooCodeDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    fun provideTaskDao(database: RooCodeDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    fun provideAgentDao(database: RooCodeDatabase): AgentDao {
        return database.agentDao()
    }

    @Provides
    fun provideMcpServerDao(database: RooCodeDatabase): McpServerDao {
        return database.mcpServerDao()
    }
}