# Entouche

**Your AI-Powered Personal Knowledge Assistant**

Entouche is a cross-platform mental wellness and productivity app built with Kotlin Multiplatform (KMP). It helps users manage anxiety, ADHD, and mental health challenges by providing intelligent note-taking, voice memos with AI transcription, mood tracking, and memory training games.

---

## Features

### Core Features

- **Smart Notes** - Create, organize, and search notes with AI-powered insights
- **Voice Capture** - Record voice memos with automatic AI transcription using Groq Whisper
- **Mood Tracker** - Track daily moods and identify patterns over time
- **Memory Game** - LLM-powered flashcard system to help with memory training

### Memory Game Modes

- **Flashcards** - Classic flip card learning
- **Quiz Mode** - Multiple choice questions
- **Speed Round** - Time-pressured questions for bonus points
- **Match Game** - Match questions with answers
- **Play with Friend** - Multiplayer mode where one person asks questions and another answers

### Design

- **Calm Ocean Theme** - Soothing blue/teal color palette designed for mental wellness
- **Glassmorphism UI** - Modern, elegant interface with frosted glass effects
- **Cross-Platform** - Runs on Android, iOS, and Desktop (JVM)

---

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 2.3.0 |
| UI Framework | Compose Multiplatform 1.9.3 |
| Backend | Supabase (Auth + PostgreSQL) |
| AI Services | Groq API (Whisper for transcription, LLaMA for parsing) |
| Networking | Ktor 3.3.3 |
| Navigation | Jetpack Navigation Compose 2.9.0 |
| Serialization | kotlinx.serialization 1.7.3 |

---

## Prerequisites

### All Platforms

- **JDK 17+** - Required for Gradle and Kotlin compilation
- **Android Studio** (recommended) or IntelliJ IDEA with Kotlin plugin
- **Git** - For cloning the repository

### Android Development

- **Android SDK** - API Level 26+ (Android 8.0 Oreo)
- **Android Emulator** or physical device with USB debugging enabled

### iOS Development (macOS only)

- **macOS** - Required for iOS development
- **Xcode 15+** - Available from the Mac App Store
- **CocoaPods** - Install with `sudo gem install cocoapods`

### Desktop Development

- No additional requirements beyond JDK

---

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/entouche.git
cd entouche
```

### 2. Configure API Keys

The app uses external services that require API keys:

#### Supabase Configuration

Edit `/composeApp/src/commonMain/kotlin/en/entouche/data/SupabaseClient.kt`:

```kotlin
object SupabaseConfig {
    const val SUPABASE_URL = "https://your-project.supabase.co"
    const val SUPABASE_ANON_KEY = "your-anon-key"
}
```

To get your Supabase credentials:
1. Create an account at [supabase.com](https://supabase.com)
2. Create a new project
3. Go to **Settings > API** to find your URL and anon key
4. Run the SQL migrations (see Database Setup below)

#### Groq API Configuration

Edit `/composeApp/src/commonMain/kotlin/en/entouche/audio/TranscriptionService.kt`:

```kotlin
object TranscriptionConfig {
    var apiKey: String = "your-groq-api-key"
}
```

To get a Groq API key:
1. Sign up at [console.groq.com](https://console.groq.com)
2. Navigate to **API Keys** and create a new key
3. Copy the key into the configuration

### 3. Database Setup

Run the following SQL files in your Supabase SQL Editor (in order):

1. `supabase_schema_final.sql` - Core tables (users, notes, etc.)
2. `supabase_mood_table.sql` - Mood tracking table
3. `supabase_memory_game.sql` - Memory game tables (card_decks, game_results)

---

## Building & Running

### Android

#### Using Android Studio (Recommended)

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Select `composeApp` configuration
4. Click **Run** or press `Shift+F10`

#### Using Command Line

```bash
# Build debug APK
./gradlew :composeApp:assembleDebug

# Install on connected device
adb install composeApp/build/outputs/apk/debug/composeApp-debug.apk

# Launch the app
adb shell am start -n en.entouche/.MainActivity
```

**APK Location:** `composeApp/build/outputs/apk/debug/composeApp-debug.apk`

### Desktop (JVM)

```bash
# Run directly
./gradlew :composeApp:run

# Or on Windows
.\gradlew.bat :composeApp:run
```

The desktop app will launch automatically after building.

### iOS

#### Using Xcode

1. Open `/iosApp/iosApp.xcodeproj` in Xcode
2. Select your target device or simulator
3. Click **Run** or press `Cmd+R`

#### Using Command Line (requires Xcode)

```bash
# Build the shared framework first
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# Then open and run from Xcode
open iosApp/iosApp.xcodeproj
```

**Note:** iOS development requires macOS with Xcode installed.

### Web Application

```bash
# 1. Build Kotlin/JS shared code
./gradlew :shared:jsBrowserDevelopmentLibraryDistribution

# 2. Install dependencies and run
npm install
npm run start
```

The web app will be available at `http://localhost:3000`

### Server (Ktor Backend)

```bash
./gradlew :server:run
```

The server will start at `http://localhost:8080`

---

## Project Structure

```
entouche/
├── composeApp/                 # Main Compose Multiplatform app
│   └── src/
│       ├── commonMain/         # Shared Kotlin code
│       │   └── kotlin/en/entouche/
│       │       ├── data/       # Data layer (Supabase, repositories)
│       │       ├── audio/      # Audio recording & transcription
│       │       ├── game/       # Memory game logic
│       │       └── ui/         # UI components and screens
│       │           ├── components/  # Reusable UI components
│       │           ├── screens/     # App screens
│       │           ├── theme/       # Colors, typography, shapes
│       │           ├── viewmodel/   # ViewModels
│       │           └── navigation/  # Navigation setup
│       ├── androidMain/        # Android-specific code
│       ├── iosMain/            # iOS-specific code
│       └── jvmMain/            # Desktop-specific code
├── iosApp/                     # iOS app entry point (Xcode project)
├── shared/                     # Shared library module
├── server/                     # Ktor server backend
├── webApp/                     # React web application
├── gradle/                     # Gradle wrapper and version catalog
└── *.sql                       # Supabase schema files
```

---

## Key Features Guide

### 1. Authentication

1. Launch the app
2. You'll see the **Sign In** screen
3. Enter your email and password, or tap **Sign Up** to create an account
4. Email verification may be required depending on Supabase settings

### 2. Creating Notes

1. From the **Home** screen, tap the **+** button or navigate to **Notes**
2. Tap **Create Note**
3. Enter a title and content
4. Save to store the note

### 3. Voice Memos with AI Transcription

1. Navigate to **Voice Capture** (microphone icon)
2. Tap the large **Record** button to start recording
3. Speak your memo
4. Tap **Stop** when finished
5. The app will automatically transcribe using Groq Whisper AI
6. Review and save your transcribed note

### 4. Memory Game

#### Creating a Deck

1. Navigate to **Memory Game** from the home screen
2. Tap **New Deck**
3. Either:
   - **Upload a document** (.txt, .md, .csv, .json) containing Q&A pairs
   - **Paste content** directly into the text area
4. Enter a deck name
5. Tap **Create Deck** - AI will parse and create flashcards

**Supported Q&A Formats:**
```
Q: What is photosynthesis?
A: The process by which plants convert light into energy

Question: What year did WWII end?
Answer: 1945

# Or just paste study notes - AI will generate questions!
```

#### Playing a Game

1. Select a deck from your list
2. Choose a game mode:
   - **Flashcards** - Tap to flip, mark as known/unknown
   - **Quiz Mode** - Select the correct answer from 4 options
   - **Speed Round** - Answer quickly for bonus points
   - **Play with Friend** - Multiplayer Q&A mode

#### Play with Friend Mode

1. Select **Play with Friend** mode
2. **Quiz Master** types a question
3. Pass the device to the **Player**
4. Player types their answer
5. Pass back to Quiz Master to judge correct/incorrect
6. Continue for as many rounds as you want
7. End game to see final scores

### 5. Mood Tracking

1. From the **Home** screen, find the mood tracker widget
2. Select your current mood
3. Optionally add notes about how you're feeling
4. View mood history and patterns over time

### 6. Settings

1. Tap the **Settings** icon (gear) in the bottom navigation
2. Available options:
   - Theme preferences
   - Notification settings
   - Account management
   - Sign out

---

## Troubleshooting

### Build Errors

**Gradle sync fails:**
```bash
# Clean and rebuild
./gradlew clean
./gradlew build
```

**JDK version issues:**
Ensure you're using JDK 17+:
```bash
java -version
```

### Runtime Errors

**"Failed to parse content: 400"**
- Check that your Groq API key is valid and not expired
- Ensure you have API credits remaining

**Authentication errors:**
- Verify Supabase URL and anon key are correct
- Check that the database tables exist (run SQL migrations)

**Voice recording not working:**
- Grant microphone permissions when prompted
- On Android, check app permissions in Settings

### iOS Specific

**CocoaPods errors:**
```bash
cd iosApp
pod install --repo-update
```

**Signing errors:**
- Open Xcode and configure your development team
- Go to **Signing & Capabilities** and select your team

---

## API Reference

### Supabase Tables

| Table | Description |
|-------|-------------|
| `notes` | User notes with title, content, timestamps |
| `mood_entries` | Mood tracking data |
| `card_decks` | Memory game flashcard decks |
| `game_results` | Memory game session results |

### External APIs

| Service | Purpose | Documentation |
|---------|---------|---------------|
| Groq Whisper | Voice transcription | [docs.groq.com](https://docs.groq.com) |
| Groq LLaMA | Q&A parsing | [docs.groq.com](https://docs.groq.com) |
| Supabase Auth | Authentication | [supabase.com/docs/auth](https://supabase.com/docs/guides/auth) |
| Supabase Database | PostgreSQL backend | [supabase.com/docs/database](https://supabase.com/docs/guides/database) |

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Acknowledgments

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Supabase](https://supabase.com)
- [Groq](https://groq.com)

---

## Support

For issues and feature requests, please [open an issue](https://github.com/yourusername/entouche/issues) on GitHub.
