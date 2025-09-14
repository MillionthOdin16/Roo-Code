#!/bin/bash

# Android Roo Code Launcher Script
# This script helps with common Android development tasks

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

show_help() {
    echo "Android Roo Code Development Helper"
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  setup      - Initial setup and dependency installation"
    echo "  build      - Build the Android app"
    echo "  test       - Run all tests"
    echo "  install    - Install debug build on connected device"
    echo "  clean      - Clean build artifacts"
    echo "  lint       - Run linting checks"
    echo "  help       - Show this help message"
    echo ""
    echo "Prerequisites:"
    echo "  - Android Studio installed"
    echo "  - Android SDK (API 24+)"
    echo "  - Java 11+"
    echo ""
}

setup() {
    echo "🚀 Setting up Android Roo Code development environment..."
    
    # Check for required tools
    if ! command -v java &> /dev/null; then
        echo "❌ Java not found. Please install Java 11 or later."
        exit 1
    fi
    
    if [ ! -d "$ANDROID_HOME" ] && [ ! -d "$ANDROID_SDK_ROOT" ]; then
        echo "❌ Android SDK not found. Please install Android Studio and set ANDROID_HOME."
        exit 1
    fi
    
    echo "✅ Java found: $(java -version 2>&1 | head -n 1)"
    echo "✅ Android SDK found"
    
    # Create local.properties if it doesn't exist
    if [ ! -f "local.properties" ]; then
        echo "📝 Creating local.properties..."
        echo "# Add your API keys here" > local.properties
        echo "# OPENAI_API_KEY=your_openai_key" >> local.properties
        echo "# ANTHROPIC_API_KEY=your_anthropic_key" >> local.properties
        echo "⚠️  Please add your API keys to local.properties"
    fi
    
    echo "✅ Setup complete!"
}

build() {
    echo "🔨 Building Android Roo Code..."
    ./gradlew assembleDebug
    echo "✅ Build complete!"
}

test() {
    echo "🧪 Running tests..."
    ./gradlew test
    echo "✅ Tests complete!"
}

install() {
    echo "📱 Installing on connected device..."
    ./gradlew installDebug
    echo "✅ Installation complete!"
}

clean() {
    echo "🧹 Cleaning build artifacts..."
    ./gradlew clean
    echo "✅ Clean complete!"
}

lint() {
    echo "🔍 Running lint checks..."
    ./gradlew lint
    echo "✅ Lint complete!"
}

# Main script logic
case "${1:-help}" in
    setup)
        setup
        ;;
    build)
        build
        ;;
    test)
        test
        ;;
    install)
        install
        ;;
    clean)
        clean
        ;;
    lint)
        lint
        ;;
    help|*)
        show_help
        ;;
esac