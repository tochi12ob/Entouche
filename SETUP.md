# Entouche Setup Guide

This guide explains how to configure the required API keys and services for Entouche.

## Required Services

### 1. Supabase (Backend & Authentication)

Supabase provides the database and authentication for Entouche.

#### Setup Steps:

1. Go to [supabase.com](https://supabase.com) and create a free account
2. Click **New Project** and fill in the details
3. Wait for the project to be provisioned (1-2 minutes)
4. Go to **Settings** → **API** in your project dashboard
5. Copy the following values:
   - **Project URL** (e.g., `https://xxxxx.supabase.co`)
   - **anon/public key** (starts with `eyJ...`)

#### Configure in Code:

Edit `composeApp/src/commonMain/kotlin/en/entouche/data/SupabaseClient.kt`:

```kotlin
object SupabaseConfig {
    const val SUPABASE_URL = "https://your-project-id.supabase.co"
    const val SUPABASE_ANON_KEY = "your-anon-key-here"
}
```

#### Database Setup:

Run these SQL files in your Supabase **SQL Editor** (in order):

1. `supabase_schema_final.sql` - Core tables
2. `supabase_mood_table.sql` - Mood tracking
3. `supabase_memory_game.sql` - Memory game tables

---

### 2. Groq API (AI Transcription & Parsing)

Groq provides fast AI inference for voice transcription and Q&A parsing.

#### Setup Steps:

1. Go to [console.groq.com](https://console.groq.com) and sign up
2. Navigate to **API Keys** in the sidebar
3. Click **Create API Key**
4. Copy the generated key (starts with `gsk_...`)

#### Configure in Code:

Edit `composeApp/src/commonMain/kotlin/en/entouche/audio/TranscriptionService.kt`:

```kotlin
object TranscriptionConfig {
    var apiKey: String = "gsk_your_api_key_here"
}
```

---

## Quick Start Checklist

- [ ] Created Supabase project
- [ ] Copied Supabase URL and anon key to `SupabaseClient.kt`
- [ ] Ran SQL migrations in Supabase SQL Editor
- [ ] Created Groq API key
- [ ] Added Groq API key to `TranscriptionService.kt`
- [ ] Built and ran the app

---

## Troubleshooting

### "Authentication failed"
- Verify your Supabase URL doesn't have a trailing slash
- Check that the anon key is correct (copy it again)
- Ensure the database tables exist

### "Transcription failed" or "Failed to parse content"
- Verify your Groq API key is valid
- Check you have API credits remaining at console.groq.com
- Ensure the key starts with `gsk_`

### Build errors after adding keys
- Make sure there are no extra quotes or spaces
- Kotlin strings should be in double quotes: `"your-key"`

---

## Security Notes

- **Never commit real API keys** to version control
- The `.gitignore` file excludes `secrets.properties` and similar files
- For production, consider using environment variables or a secrets manager
- Rotate your API keys if you accidentally expose them
