package com.roocode.android.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.roocode.android.data.models.Message
import com.roocode.android.data.models.Task
import com.roocode.android.data.models.Agent
import com.roocode.android.data.models.McpServer

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE taskId = :taskId ORDER BY timestamp ASC")
    fun getMessagesByTask(taskId: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): Message?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Update
    suspend fun updateMessage(message: Message)

    @Delete
    suspend fun deleteMessage(message: Message)

    @Query("DELETE FROM messages WHERE taskId = :taskId")
    suspend fun deleteMessagesByTask(taskId: String)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY updatedAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): Task?

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY updatedAt DESC")
    fun getTasksByStatus(status: String): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
}

@Dao
interface AgentDao {
    @Query("SELECT * FROM agents")
    fun getAllAgents(): Flow<List<Agent>>

    @Query("SELECT * FROM agents WHERE isActive = 1")
    fun getActiveAgents(): Flow<List<Agent>>

    @Query("SELECT * FROM agents WHERE id = :agentId")
    suspend fun getAgentById(agentId: String): Agent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgent(agent: Agent)

    @Update
    suspend fun updateAgent(agent: Agent)

    @Delete
    suspend fun deleteAgent(agent: Agent)
}

@Dao
interface McpServerDao {
    @Query("SELECT * FROM mcp_servers")
    fun getAllMcpServers(): Flow<List<McpServer>>

    @Query("SELECT * FROM mcp_servers WHERE isActive = 1")
    fun getActiveMcpServers(): Flow<List<McpServer>>

    @Query("SELECT * FROM mcp_servers WHERE id = :serverId")
    suspend fun getMcpServerById(serverId: String): McpServer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMcpServer(server: McpServer)

    @Update
    suspend fun updateMcpServer(server: McpServer)

    @Delete
    suspend fun deleteMcpServer(server: McpServer)
}