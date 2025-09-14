# Android Roo Code

An Android app that brings the power of Roo Code agent orchestration to mobile devices. This app implements the same core functionality as the VS Code extension but optimized for mobile interaction patterns.

## 🚀 Features

### Core Functionality

- **🤖 Agent Orchestration**: Coordinate multiple AI agents for complex tasks
- **💬 Chat Interface**: Natural language conversation with Material Design 3
- **📋 Task Management**: Create, track, and manage tasks within conversations
- **🔌 MCP Server Integration**: Connect to Model Context Protocol servers
- **🌐 Internet Access**: Browse the web and fetch content as needed
- **🔗 Multi-Provider Support**: OpenAI, Anthropic, and local model support

### Technical Features

- **📱 Modern Android Architecture**: MVVM with Jetpack Compose
- **🗄️ Local Data Persistence**: Room database for offline functionality
- **⚡ Reactive UI**: StateFlow and Compose for real-time updates
- **🔧 Dependency Injection**: Hilt for clean architecture
- **🧪 Testing Ready**: Unit and integration test framework

## 🏗️ Architecture

The Android app follows the same core concepts as the VS Code extension but adapted for mobile:

```
┌─ UI Layer (Compose)
│  ├─ ChatScreen
│  ├─ TaskManagement
│  └─ Settings
│
├─ Domain Layer
│  ├─ AgentOrchestrator
│  ├─ TaskManager
│  └─ McpManager
│
├─ Data Layer
│  ├─ Room Database
│  ├─ API Services
│  └─ Repositories
│
└─ Services
   ├─ Browser Service
   ├─ MCP Servers
   └─ AI Providers
```

### Key Components

#### AgentOrchestrator (`AgentOrchestrator.kt`)

Coordinates multiple AI agents based on user input and task context:

- Selects relevant agents for each message
- Manages agent capabilities and specializations
- Processes responses from multiple agents
- Handles tool calls and MCP server interactions

#### McpManager (`McpManager.kt`)

Manages Model Context Protocol server connections:

- Dynamic server connection management
- Tool discovery and execution
- Error handling and reconnection logic
- Agent-specific tool filtering

#### TaskManager (`TaskManager.kt`)

Handles task lifecycle and persistence:

- Task creation, updating, and completion
- Message association and history
- Status tracking (active, paused, completed, error)
- Cross-task context management

#### BrowserService (`BrowserService.kt`)

Provides web interaction capabilities:

- URL content fetching
- Web search functionality
- JavaScript execution
- Form filling and element interaction

## 🛠️ Getting Started

### Prerequisites

- **Android Studio** Arctic Fox or later
- **Android SDK** API 24+ (Android 7.0)
- **Java** 11 or later
- **Gradle** 8.4+

### Setup

1. **Clone and navigate**:

    ```bash
    cd apps/android-roo-code
    ```

2. **Open in Android Studio**:

    - Open Android Studio
    - Select "Open an existing project"
    - Navigate to the `android-roo-code` directory

3. **Sync project**:

    - Android Studio will automatically prompt to sync
    - Or manually: `File > Sync Project with Gradle Files`

4. **Configure API keys**:
    - Create a `local.properties` file (gitignored)
    - Add your API keys:
        ```
        OPENAI_API_KEY=your_openai_key
        ANTHROPIC_API_KEY=your_anthropic_key
        ```

### Building

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

### Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# All tests
./gradlew check
```

## 📱 Usage Examples

### Basic Chat Interaction

```kotlin
// User types: "Help me create a React component"
// App automatically:
1. Creates a new task
2. Analyzes the request for relevant agents
3. Routes to coding-specialized agent
4. May use MCP servers for file operations
5. Provides step-by-step guidance
```

### Agent Orchestration

```kotlin
// User types: "Research the latest AI frameworks and create a comparison"
// App coordinates:
1. Web search agent for research
2. Data analysis agent for comparison
3. Documentation agent for formatting
4. All results merged into comprehensive response
```

### MCP Server Integration

```kotlin
// Available MCP servers are automatically discovered
// Tools are exposed based on agent capabilities:
- Filesystem operations
- Database queries
- API integrations
- Custom tool execution
```

## 🔧 Configuration

### Adding New Agents

1. Create agent definition in the database:

```kotlin
val newAgent = Agent(
    id = "specialized-agent-id",
    name = "Code Reviewer",
    description = "Specialized in code review and best practices",
    capabilities = listOf("code-review", "security-audit", "performance"),
    isActive = true
)
```

2. Register with orchestrator:

```kotlin
agentOrchestrator.registerAgent(newAgent)
```

### Adding MCP Servers

1. Define server configuration:

```kotlin
val mcpServer = McpServer(
    id = "custom-server",
    name = "Custom Tools",
    command = "node",
    args = listOf("/path/to/server.js"),
    isActive = true
)
```

2. Connect through manager:

```kotlin
mcpManager.connectToServer(mcpServer)
```

### API Provider Configuration

The app supports multiple AI providers:

```kotlin
// OpenAI
apiService.setProvider(ApiProvider.OPENAI, "your-api-key")

// Anthropic
apiService.setProvider(ApiProvider.ANTHROPIC, "your-api-key")

// Local models (Ollama, etc.)
apiService.setProvider(ApiProvider.LOCAL, "")
```

## 🗂️ Project Structure

```
src/main/java/com/roocode/android/
├── core/
│   ├── agent/              # Agent orchestration logic
│   ├── mcp/                # MCP server management
│   └── task/               # Task management
├── data/
│   ├── models/             # Data models and entities
│   ├── dao/                # Room DAOs
│   └── repositories/       # Repository pattern
├── di/                     # Dependency injection
├── services/
│   ├── api/                # AI provider APIs
│   └── browser/            # Web interaction
└── ui/
    ├── chat/               # Chat interface
    └── theme/              # Material Design theme
```

## 🔒 Security Considerations

- **API Keys**: Stored securely in encrypted preferences
- **Local Data**: Room database with encryption at rest
- **Network**: Certificate pinning for API connections
- **WebView**: Restricted to necessary permissions
- **MCP Servers**: Sandboxed execution environment

## 🚀 Performance Optimization

- **Lazy Loading**: Components loaded on-demand
- **Memory Management**: Proper lifecycle handling
- **Background Processing**: Coroutines for non-blocking operations
- **Caching**: Intelligent caching of API responses
- **Database**: Optimized queries with indexes

## 🧪 Testing Strategy

### Unit Tests

- Core logic testing with JUnit
- Repository and DAO testing
- Agent orchestration scenarios

### Integration Tests

- API service integration
- Database operations
- MCP server connections

### UI Tests

- Compose UI testing
- User interaction flows
- Accessibility testing

## 🔄 Continuous Integration

The app integrates with the main Roo Code monorepo CI/CD:

```bash
# Run from monorepo root
pnpm test  # Includes Android tests
pnpm build # Includes Android build
```

## 📈 Roadmap

- [ ] **Voice Interface**: Speech-to-text and text-to-speech
- [ ] **Offline Mode**: Full functionality without internet
- [ ] **Plugin System**: User-installable agent extensions
- [ ] **Collaboration**: Multi-user task sharing
- [ ] **Widgets**: Android home screen widgets
- [ ] **Wear OS**: Companion app for smartwatches

## 🤝 Contributing

1. Follow the main project's contribution guidelines
2. Use Android coding standards (Kotlin style guide)
3. Write tests for new functionality
4. Update documentation for API changes

## 📄 License

Same as main Roo Code project - Apache 2.0 © 2025 Roo Code, Inc.

---

**Ready to get started?** Open Android Studio, load the project, and start building the future of mobile AI agent orchestration! 🚀
