# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Piano Track Opus is an Android application written in Kotlin using the modern Android development stack. The project uses Gradle with Kotlin DSL for build configuration and follows standard Android project structure.

## Development Commands

### Building the Project
```bash
./gradlew build
```

### Running Tests
```bash
# Unit tests (local JVM)
./gradlew test

# Instrumented tests (requires Android device/emulator)
./gradlew connectedAndroidTest

# All tests
./gradlew check
```

### Installing the App
```bash
# Debug build
./gradlew installDebug

# Release build
./gradlew installRelease
```

### Cleaning the Project
```bash
./gradlew clean
```

### Linting
```bash
./gradlew lint
```

## Project Structure

- **Package**: `com.example.pianotrackopus`
- **Namespace**: `com.example.pianotrackopus`
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Compile SDK**: 36

### Key Directories
- `app/src/main/java/com/example/pianotrackopus/` - Main application source code (currently empty)
- `app/src/test/` - Unit tests
- `app/src/androidTest/` - Instrumented Android tests
- `app/src/main/res/` - Android resources (layouts, strings, drawables, etc.)

## Build Configuration

The project uses:
- **Gradle Version Catalog** (`gradle/libs.versions.toml`) for dependency management
- **Kotlin DSL** for build scripts
- **Android Gradle Plugin** 8.11.1
- **Kotlin** 2.0.21
- **Java 11** compatibility

### Key Dependencies
- AndroidX Core KTX
- AndroidX AppCompat
- Material Design Components
- JUnit for unit testing
- Espresso for UI testing

## Development Notes

- This appears to be a new/template Android project with minimal code implementation
- No main Activity or UI components have been implemented yet
- Standard Android testing framework is set up with example tests
- The project follows modern Android development practices with Kotlin and AndroidX libraries

## Git Workflow Rules

**IMPORTANT**: Do not commit any changes to git until the user has:
1. Confirmed that the implementation works correctly
2. Explicitly given permission to commit the changes

Wait for user confirmation before using `git commit` commands.