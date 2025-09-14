package com.roocode.android.ui.chat

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.roocode.android.data.models.*
import com.roocode.android.core.agent.AgentOrchestrator
import com.roocode.android.core.task.TaskManager
import com.roocode.android.services.api.ApiService
import com.roocode.android.services.browser.BrowserService
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val taskManager: TaskManager,
    private val agentOrchestrator: AgentOrchestrator,
    private val apiService: ApiService,
    private val browserService: BrowserService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    var messageText by mutableStateOf("")
        private set
    
    var currentTask: Task? = null
        private set
    
    init {
        createInitialTask()
    }
    
    fun updateMessageText(text: String) {
        messageText = text
    }
    
    fun sendMessage() {
        if (messageText.isBlank() || currentTask == null) return
        
        val userMessage = messageText
        messageText = ""
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Get active agents
                val agents = agentOrchestrator.getActiveAgents()
                
                // Process message through agent orchestrator
                agentOrchestrator.processMessage(
                    task = currentTask!!,
                    userMessage = userMessage,
                    agents = agents
                ).collect { message ->
                    // Add message to task
                    taskManager.addMessageToTask(message)
                    
                    // Update local messages list
                    _messages.value = _messages.value + message
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Unknown error occurred"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    fun createNewTask(title: String, description: String) {
        viewModelScope.launch {
            try {
                val task = taskManager.createTask(title, description)
                currentTask = task
                _messages.value = emptyList()
                
                // Load messages for this task
                taskManager.getTaskMessages(task.id).collect { messages ->
                    _messages.value = messages
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error creating task"
                )
            }
        }
    }
    
    fun loadTask(taskId: String) {
        viewModelScope.launch {
            try {
                val task = taskManager.getTaskById(taskId)
                if (task != null) {
                    currentTask = task
                    
                    // Load messages for this task
                    taskManager.getTaskMessages(taskId).collect { messages ->
                        _messages.value = messages
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error loading task"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun pauseTask() {
        currentTask?.let { task ->
            viewModelScope.launch {
                taskManager.pauseTask(task.id)
                currentTask = currentTask?.copy(status = TaskStatus.PAUSED)
            }
        }
    }
    
    fun resumeTask() {
        currentTask?.let { task ->
            viewModelScope.launch {
                taskManager.resumeTask(task.id)
                currentTask = currentTask?.copy(status = TaskStatus.ACTIVE)
            }
        }
    }
    
    fun completeTask() {
        currentTask?.let { task ->
            viewModelScope.launch {
                taskManager.completeTask(task.id)
                currentTask = currentTask?.copy(status = TaskStatus.COMPLETED)
            }
        }
    }
    
    private fun createInitialTask() {
        viewModelScope.launch {
            try {
                val task = taskManager.createTask(
                    title = "Chat Session",
                    description = "General chat with Roo Code"
                )
                currentTask = task
                
                // Load messages for this task
                taskManager.getTaskMessages(task.id).collect { messages ->
                    _messages.value = messages
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error creating initial task"
                )
            }
        }
    }
}

data class ChatUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)