# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Build and install on connected device

# Test
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests (requires device/emulator)

# Lint
./gradlew lint                   # Run Android lint analysis

# Clean
./gradlew clean                  # Clean build artifacts
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Architecture

Snapnote is an Android note-taking app with ML Kit OCR capabilities for scanning whiteboards and documents.

**Tech Stack:**
- Kotlin with View Binding
- Material Design 3
- Google ML Kit Text Recognition
- Min SDK 30 / Target SDK 36
- Use MVVM architecture pattern
- Use Coroutines for asynchronous tasks
- Use Shared Preferences for data storage.

**Project Structure:**
- Single module app (`app/`)
- Activity-based navigation (no Fragments)
- Version catalog in `gradle/libs.versions.toml`


**Key Activities:**
- `NotesActivity` - Main launcher, displays notes list
- `FoldersActivity` - Folder organization
- `NoteDetailActivity` - Note viewer with edit, summarize, translate features
- `WelcomeActivity` - Onboarding screen

**Source Paths:**
- Activities: `app/src/main/java/com/harryvu176/snapnote/`
- Layouts: `app/src/main/res/layout/`
- Resources: `app/src/main/res/values/` (strings.xml, colors.xml, themes.xml)

**Brand Colors:**
- Primary: #1F316F (Deep Blue)
- Secondary: #5163A6 (Medium Blue)
- Background: #F4F6FB (Light Blue)