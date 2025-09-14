package com.roocode.android.core.mcp

import com.roocode.android.data.models.Agent
import com.roocode.android.data.models.McpServer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class McpManager @Inject constructor() {
    private val activeServers = mutableMapOf<String, McpServerConnection>()
    private val serverTools = mutableMapOf<String, List<McpTool>>()
    
    /**
     * Connect to an MCP server
     */
    suspend fun connectToServer(server: McpServer): Result<Unit> {
        return try {
            val connection = McpServerConnection(server)
            val success = connection.connect()
            
            if (success) {
                activeServers[server.id] = connection
                refreshServerTools(server.id)
                Log.i("McpManager", "Connected to MCP server: ${server.name}")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to connect to MCP server: ${server.name}"))
            }
        } catch (e: Exception) {
            Log.e("McpManager", "Error connecting to MCP server: ${server.name}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Disconnect from an MCP server
     */
    suspend fun disconnectFromServer(serverId: String) {
        activeServers[serverId]?.disconnect()
        activeServers.remove(serverId)
        serverTools.remove(serverId)
        Log.i("McpManager", "Disconnected from MCP server: $serverId")
    }
    
    /**
     * Get all available tools from all connected MCP servers
     */
    fun getAvailableTools(): List<McpTool> {
        return serverTools.values.flatten()
    }
    
    /**
     * Get tools available for a specific agent
     */
    fun getToolsForAgent(agent: Agent): List<McpTool> {
        return getAvailableTools().filter { tool ->
            agent.capabilities.any { capability ->
                tool.category.contains(capability, ignoreCase = true) ||
                tool.name.contains(capability, ignoreCase = true)
            }
        }
    }
    
    /**
     * Execute a tool call
     */
    suspend fun executeTool(
        toolName: String,
        parameters: Map<String, Any>
    ): Result<McpToolResult> {
        return try {
            val tool = findToolByName(toolName)
                ?: return Result.failure(Exception("Tool not found: $toolName"))
            
            val server = activeServers[tool.serverId]
                ?: return Result.failure(Exception("Server not connected for tool: $toolName"))
            
            val result = server.executeToolCall(toolName, parameters)
            Result.success(result)
        } catch (e: Exception) {
            Log.e("McpManager", "Error executing tool: $toolName", e)
            Result.failure(e)
        }
    }
    
    /**
     * Refresh tools for a specific server
     */
    private suspend fun refreshServerTools(serverId: String) {
        val connection = activeServers[serverId] ?: return
        val tools = connection.listTools()
        serverTools[serverId] = tools
    }
    
    /**
     * Find a tool by name across all servers
     */
    private fun findToolByName(toolName: String): McpTool? {
        return serverTools.values.flatten().find { it.name == toolName }
    }
    
    /**
     * Get status of all MCP servers
     */
    fun getServerStatuses(): Map<String, McpServerStatus> {
        return activeServers.mapValues { (_, connection) ->
            McpServerStatus(
                isConnected = connection.isConnected(),
                toolCount = serverTools[connection.serverId]?.size ?: 0,
                lastError = connection.getLastError()
            )
        }
    }
}

/**
 * Represents a connection to an MCP server
 */
class McpServerConnection(private val server: McpServer) {
    val serverId: String = server.id
    private var isConnected = false
    private var lastError: String? = null
    
    suspend fun connect(): Boolean {
        return try {
            // In a real implementation, this would establish the actual connection
            // using the MCP protocol over stdio, HTTP, or WebSocket
            Log.d("McpServerConnection", "Connecting to ${server.name} with command: ${server.command}")
            
            // Simulate connection process
            isConnected = true
            lastError = null
            true
        } catch (e: Exception) {
            lastError = e.message
            isConnected = false
            false
        }
    }
    
    fun disconnect() {
        isConnected = false
        lastError = null
    }
    
    fun isConnected(): Boolean = isConnected
    
    fun getLastError(): String? = lastError
    
    suspend fun listTools(): List<McpTool> {
        if (!isConnected) return emptyList()
        
        // In a real implementation, this would query the MCP server for available tools
        return when (server.name) {
            "filesystem" -> listOf(
                McpTool("read_file", "Read file contents", "filesystem", serverId),
                McpTool("write_file", "Write file contents", "filesystem", serverId),
                McpTool("list_directory", "List directory contents", "filesystem", serverId)
            )
            "web_search" -> listOf(
                McpTool("search_web", "Search the web", "search", serverId),
                McpTool("fetch_url", "Fetch URL content", "web", serverId)
            )
            "database" -> listOf(
                McpTool("query_database", "Execute database query", "database", serverId),
                McpTool("list_tables", "List database tables", "database", serverId)
            )
            else -> emptyList()
        }
    }
    
    suspend fun executeToolCall(
        toolName: String,
        parameters: Map<String, Any>
    ): McpToolResult {
        if (!isConnected) {
            throw Exception("Server not connected")
        }
        
        // In a real implementation, this would send the tool call to the MCP server
        Log.d("McpServerConnection", "Executing tool: $toolName with params: $parameters")
        
        // Simulate tool execution
        return McpToolResult(
            success = true,
            result = "Tool $toolName executed successfully",
            error = null
        )
    }
}

data class McpTool(
    val name: String,
    val description: String,
    val category: String,
    val serverId: String
)

data class McpToolResult(
    val success: Boolean,
    val result: String?,
    val error: String?
)

data class McpServerStatus(
    val isConnected: Boolean,
    val toolCount: Int,
    val lastError: String?
)