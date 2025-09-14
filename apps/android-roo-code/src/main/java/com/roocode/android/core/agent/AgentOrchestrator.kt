package com.roocode.android.core.agent

import com.roocode.android.data.models.Agent
import com.roocode.android.data.models.Message
import com.roocode.android.data.models.MessageType
import com.roocode.android.data.models.MessageRole
import com.roocode.android.data.models.Task
import com.roocode.android.services.api.ApiService
import com.roocode.android.core.mcp.McpManager
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import java.util.Date

@Singleton
class AgentOrchestrator @Inject constructor(
    private val apiService: ApiService,
    private val mcpManager: McpManager
) {
    private val activeAgents = mutableMapOf<String, Agent>()
    
    /**
     * Process a user message and coordinate agent responses
     */
    suspend fun processMessage(
        task: Task,
        userMessage: String,
        agents: List<Agent>
    ): Flow<Message> = flow {
        val messageId = UUID.randomUUID().toString()
        val timestamp = Date()
        
        // Emit user message
        emit(Message(
            id = messageId,
            taskId = task.id,
            type = MessageType.SAY,
            role = MessageRole.USER,
            content = userMessage,
            timestamp = timestamp
        ))
        
        // Determine which agents should handle this request
        val relevantAgents = selectRelevantAgents(agents, userMessage, task)
        
        if (relevantAgents.isEmpty()) {
            // Default agent response
            val response = apiService.generateResponse(
                messages = listOf(userMessage),
                systemPrompt = buildSystemPrompt(task),
                tools = mcpManager.getAvailableTools()
            )
            
            emit(Message(
                id = UUID.randomUUID().toString(),
                taskId = task.id,
                type = MessageType.SAY,
                role = MessageRole.ASSISTANT,
                content = response.content,
                timestamp = Date()
            ))
        } else {
            // Orchestrate multiple agents
            for (agent in relevantAgents) {
                val agentResponse = processWithAgent(agent, userMessage, task)
                emit(agentResponse)
            }
        }
    }
    
    /**
     * Select which agents should handle a particular message
     */
    private fun selectRelevantAgents(
        agents: List<Agent>,
        message: String,
        task: Task
    ): List<Agent> {
        return agents.filter { agent ->
            agent.isActive && agentCanHandle(agent, message, task)
        }
    }
    
    /**
     * Determine if an agent can handle a specific message
     */
    private fun agentCanHandle(agent: Agent, message: String, task: Task): Boolean {
        // Simple capability matching - in a real implementation this would be more sophisticated
        val messageLower = message.lowercase()
        return agent.capabilities.any { capability ->
            messageLower.contains(capability.lowercase())
        }
    }
    
    /**
     * Process message with a specific agent
     */
    private suspend fun processWithAgent(
        agent: Agent,
        message: String,
        task: Task
    ): Message {
        val agentPrompt = buildAgentPrompt(agent, message, task)
        val tools = mcpManager.getToolsForAgent(agent)
        
        val response = apiService.generateResponse(
            messages = listOf(message),
            systemPrompt = agentPrompt,
            tools = tools
        )
        
        return Message(
            id = UUID.randomUUID().toString(),
            taskId = task.id,
            type = MessageType.SAY,
            role = MessageRole.ASSISTANT,
            content = "[${agent.name}]: ${response.content}",
            timestamp = Date(),
            metadata = agent.id
        )
    }
    
    /**
     * Build system prompt for default agent
     */
    private fun buildSystemPrompt(task: Task): String {
        return """
            You are Roo Code, an AI assistant that helps users with tasks and projects.
            
            Current task: ${task.title}
            Description: ${task.description}
            
            You can:
            - Answer questions and provide explanations
            - Help with coding and development tasks
            - Browse the internet for information
            - Use various tools and integrations
            - Coordinate with other specialized agents when needed
            
            Be helpful, precise, and proactive in your assistance.
        """.trimIndent()
    }
    
    /**
     * Build agent-specific prompt
     */
    private fun buildAgentPrompt(agent: Agent, message: String, task: Task): String {
        return """
            You are ${agent.name}, a specialized AI agent.
            
            Description: ${agent.description}
            Capabilities: ${agent.capabilities.joinToString(", ")}
            
            Current task: ${task.title}
            Task description: ${task.description}
            
            User message: $message
            
            Respond according to your specialization and capabilities.
        """.trimIndent()
    }
    
    /**
     * Register an agent for active use
     */
    fun registerAgent(agent: Agent) {
        activeAgents[agent.id] = agent
    }
    
    /**
     * Unregister an agent
     */
    fun unregisterAgent(agentId: String) {
        activeAgents.remove(agentId)
    }
    
    /**
     * Get all active agents
     */
    fun getActiveAgents(): List<Agent> {
        return activeAgents.values.toList()
    }
}