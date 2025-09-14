package com.roocode.android.services.api

import com.roocode.android.core.mcp.McpTool
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Response
import retrofit2.http.*
import com.google.gson.annotations.SerializedName

interface OpenAIApiService {
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest
    ): Response<ChatCompletionResponse>
}

interface AnthropicApiService {
    @POST("messages")
    suspend fun createMessage(
        @Header("Authorization") authorization: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: AnthropicMessageRequest
    ): Response<AnthropicMessageResponse>
}

@Singleton
class ApiService @Inject constructor(
    private val openAIApi: OpenAIApiService,
    private val anthropicApi: AnthropicApiService
) {
    private var currentProvider: ApiProvider = ApiProvider.OPENAI
    private var apiKey: String = ""
    
    fun setProvider(provider: ApiProvider, apiKey: String) {
        this.currentProvider = provider
        this.apiKey = apiKey
    }
    
    suspend fun generateResponse(
        messages: List<String>,
        systemPrompt: String,
        tools: List<McpTool> = emptyList()
    ): ApiResponse {
        return when (currentProvider) {
            ApiProvider.OPENAI -> generateOpenAIResponse(messages, systemPrompt, tools)
            ApiProvider.ANTHROPIC -> generateAnthropicResponse(messages, systemPrompt, tools)
            ApiProvider.LOCAL -> generateLocalResponse(messages, systemPrompt, tools)
        }
    }
    
    private suspend fun generateOpenAIResponse(
        messages: List<String>,
        systemPrompt: String,
        tools: List<McpTool>
    ): ApiResponse {
        val chatMessages = buildList {
            add(ChatMessage("system", systemPrompt))
            messages.forEach { message ->
                add(ChatMessage("user", message))
            }
        }
        
        val request = ChatCompletionRequest(
            model = "gpt-4",
            messages = chatMessages,
            tools = tools.map { tool ->
                ToolDefinition(
                    type = "function",
                    function = FunctionDefinition(
                        name = tool.name,
                        description = tool.description,
                        parameters = mapOf(
                            "type" to "object",
                            "properties" to emptyMap<String, Any>()
                        )
                    )
                )
            }.takeIf { it.isNotEmpty() }
        )
        
        val response = openAIApi.createChatCompletion(
            authorization = "Bearer $apiKey",
            request = request
        )
        
        return if (response.isSuccessful) {
            val body = response.body()!!
            ApiResponse(
                content = body.choices.firstOrNull()?.message?.content ?: "",
                success = true,
                error = null,
                toolCalls = body.choices.firstOrNull()?.message?.toolCalls ?: emptyList()
            )
        } else {
            ApiResponse(
                content = "",
                success = false,
                error = "API Error: ${response.code()} ${response.message()}",
                toolCalls = emptyList()
            )
        }
    }
    
    private suspend fun generateAnthropicResponse(
        messages: List<String>,
        systemPrompt: String,
        tools: List<McpTool>
    ): ApiResponse {
        val anthropicMessages = messages.map { message ->
            AnthropicMessage("user", message)
        }
        
        val request = AnthropicMessageRequest(
            model = "claude-3-sonnet-20240229",
            maxTokens = 4000,
            system = systemPrompt,
            messages = anthropicMessages,
            tools = tools.map { tool ->
                AnthropicTool(
                    name = tool.name,
                    description = tool.description,
                    inputSchema = mapOf(
                        "type" to "object",
                        "properties" to emptyMap<String, Any>()
                    )
                )
            }.takeIf { it.isNotEmpty() }
        )
        
        val response = anthropicApi.createMessage(
            authorization = "Bearer $apiKey",
            request = request
        )
        
        return if (response.isSuccessful) {
            val body = response.body()!!
            val content = body.content.firstOrNull()?.text ?: ""
            ApiResponse(
                content = content,
                success = true,
                error = null,
                toolCalls = emptyList() // TODO: Parse Anthropic tool calls
            )
        } else {
            ApiResponse(
                content = "",
                success = false,
                error = "API Error: ${response.code()} ${response.message()}",
                toolCalls = emptyList()
            )
        }
    }
    
    private suspend fun generateLocalResponse(
        messages: List<String>,
        systemPrompt: String,
        tools: List<McpTool>
    ): ApiResponse {
        // Placeholder for local model integration (e.g., using Ollama)
        return ApiResponse(
            content = "Local model response not implemented yet",
            success = true,
            error = null,
            toolCalls = emptyList()
        )
    }
}

enum class ApiProvider {
    OPENAI,
    ANTHROPIC,
    LOCAL
}

data class ApiResponse(
    val content: String,
    val success: Boolean,
    val error: String?,
    val toolCalls: List<ToolCall>
)

// OpenAI API Models
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val tools: List<ToolDefinition>? = null
)

data class ChatMessage(
    val role: String,
    val content: String
)

data class ToolDefinition(
    val type: String,
    val function: FunctionDefinition
)

data class FunctionDefinition(
    val name: String,
    val description: String,
    val parameters: Map<String, Any>
)

data class ChatCompletionResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: ResponseMessage
)

data class ResponseMessage(
    val content: String?,
    @SerializedName("tool_calls")
    val toolCalls: List<ToolCall>?
)

data class ToolCall(
    val id: String,
    val type: String,
    val function: FunctionCall
)

data class FunctionCall(
    val name: String,
    val arguments: String
)

// Anthropic API Models
data class AnthropicMessageRequest(
    val model: String,
    @SerializedName("max_tokens")
    val maxTokens: Int,
    val system: String,
    val messages: List<AnthropicMessage>,
    val tools: List<AnthropicTool>? = null
)

data class AnthropicMessage(
    val role: String,
    val content: String
)

data class AnthropicTool(
    val name: String,
    val description: String,
    @SerializedName("input_schema")
    val inputSchema: Map<String, Any>
)

data class AnthropicMessageResponse(
    val content: List<AnthropicContent>
)

data class AnthropicContent(
    val type: String,
    val text: String?
)