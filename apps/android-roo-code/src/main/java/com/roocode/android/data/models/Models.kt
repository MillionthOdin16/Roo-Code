package com.roocode.android.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val taskId: String,
    val type: MessageType,
    val role: MessageRole,
    val content: String,
    val timestamp: Date,
    val metadata: String? = null
)

enum class MessageType {
    SAY,
    ASK,
    TOOL_USE,
    TOOL_RESULT,
    ERROR
}

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val createdAt: Date,
    val updatedAt: Date,
    val agentMode: String = "default",
    val metadata: String? = null
)

enum class TaskStatus {
    ACTIVE,
    COMPLETED,
    PAUSED,
    ERROR
}

@Entity(tableName = "agents")
data class Agent(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val capabilities: List<String>,
    val isActive: Boolean = false,
    val configuration: String? = null
)

@Entity(tableName = "mcp_servers")
data class McpServer(
    @PrimaryKey val id: String,
    val name: String,
    val command: String,
    val args: List<String>,
    val isActive: Boolean = false,
    val lastError: String? = null,
    val configuration: String? = null
)