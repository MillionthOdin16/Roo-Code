package com.roocode.android.data.repositories

import com.roocode.android.data.dao.MessageDao
import com.roocode.android.data.dao.TaskDao
import com.roocode.android.data.dao.AgentDao
import com.roocode.android.data.dao.McpServerDao
import com.roocode.android.data.models.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val taskDao: TaskDao,
    private val agentDao: AgentDao,
    private val mcpServerDao: McpServerDao
) {
    
    fun getTaskMessages(taskId: String): Flow<List<Message>> {
        return messageDao.getMessagesByTask(taskId)
    }
    
    suspend fun insertMessage(message: Message) {
        messageDao.insertMessage(message)
    }
    
    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
    }
    
    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }
    
    suspend fun getTaskById(taskId: String): Task? {
        return taskDao.getTaskById(taskId)
    }
    
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }
    
    fun getActiveAgents(): Flow<List<Agent>> {
        return agentDao.getActiveAgents()
    }
    
    suspend fun insertAgent(agent: Agent) {
        agentDao.insertAgent(agent)
    }
    
    fun getActiveMcpServers(): Flow<List<McpServer>> {
        return mcpServerDao.getActiveMcpServers()
    }
    
    suspend fun insertMcpServer(server: McpServer) {
        mcpServerDao.insertMcpServer(server)
    }
}