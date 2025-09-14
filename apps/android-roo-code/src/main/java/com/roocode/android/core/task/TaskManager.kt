package com.roocode.android.core.task

import com.roocode.android.data.models.Task
import com.roocode.android.data.models.TaskStatus
import com.roocode.android.data.models.Message
import com.roocode.android.data.dao.TaskDao
import com.roocode.android.data.dao.MessageDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID
import java.util.Date

@Singleton
class TaskManager @Inject constructor(
    private val taskDao: TaskDao,
    private val messageDao: MessageDao
) {
    
    /**
     * Create a new task
     */
    suspend fun createTask(
        title: String,
        description: String,
        agentMode: String = "default"
    ): Task {
        val task = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            status = TaskStatus.ACTIVE,
            createdAt = Date(),
            updatedAt = Date(),
            agentMode = agentMode
        )
        
        taskDao.insertTask(task)
        return task
    }
    
    /**
     * Get all tasks
     */
    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
    }
    
    /**
     * Get task by ID
     */
    suspend fun getTaskById(taskId: String): Task? {
        return taskDao.getTaskById(taskId)
    }
    
    /**
     * Update task
     */
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.copy(updatedAt = Date()))
    }
    
    /**
     * Update task status
     */
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus) {
        val task = taskDao.getTaskById(taskId)
        if (task != null) {
            taskDao.updateTask(task.copy(
                status = status,
                updatedAt = Date()
            ))
        }
    }
    
    /**
     * Delete task and its messages
     */
    suspend fun deleteTask(taskId: String) {
        messageDao.deleteMessagesByTask(taskId)
        val task = taskDao.getTaskById(taskId)
        if (task != null) {
            taskDao.deleteTask(task)
        }
    }
    
    /**
     * Get messages for a task
     */
    fun getTaskMessages(taskId: String): Flow<List<Message>> {
        return messageDao.getMessagesByTask(taskId)
    }
    
    /**
     * Add message to task
     */
    suspend fun addMessageToTask(message: Message) {
        messageDao.insertMessage(message)
        
        // Update task's updatedAt timestamp
        val task = taskDao.getTaskById(message.taskId)
        if (task != null) {
            taskDao.updateTask(task.copy(updatedAt = Date()))
        }
    }
    
    /**
     * Get active tasks
     */
    fun getActiveTasks(): Flow<List<Task>> {
        return taskDao.getTasksByStatus(TaskStatus.ACTIVE.name)
    }
    
    /**
     * Get completed tasks
     */
    fun getCompletedTasks(): Flow<List<Task>> {
        return taskDao.getTasksByStatus(TaskStatus.COMPLETED.name)
    }
    
    /**
     * Pause a task
     */
    suspend fun pauseTask(taskId: String) {
        updateTaskStatus(taskId, TaskStatus.PAUSED)
    }
    
    /**
     * Resume a task
     */
    suspend fun resumeTask(taskId: String) {
        updateTaskStatus(taskId, TaskStatus.ACTIVE)
    }
    
    /**
     * Complete a task
     */
    suspend fun completeTask(taskId: String) {
        updateTaskStatus(taskId, TaskStatus.COMPLETED)
    }
    
    /**
     * Mark task as error
     */
    suspend fun markTaskAsError(taskId: String) {
        updateTaskStatus(taskId, TaskStatus.ERROR)
    }
    
    /**
     * Get task summary statistics
     */
    suspend fun getTaskSummary(): TaskSummary {
        val allTasks = taskDao.getAllTasks()
        // This is a simplified version - in practice you'd want proper aggregation
        return TaskSummary(
            totalTasks = 0, // Would need to calculate
            activeTasks = 0,
            completedTasks = 0,
            pausedTasks = 0,
            errorTasks = 0
        )
    }
}

data class TaskSummary(
    val totalTasks: Int,
    val activeTasks: Int,
    val completedTasks: Int,
    val pausedTasks: Int,
    val errorTasks: Int
)