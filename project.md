Entouche
Your AI personal Knowledge assistant that keeps track of the ‘hard to remember stuff’ of your daily life.
This helps people with
-Anxiety
-ADHD
- People with mental health issues generally


Project Concept: "ContextKeeper" - AI-Powered Personal Knowledge Assistant
This would be a cross-platform app that acts as your second brain, capturing and organizing information from your daily life while making it intelligently searchable and actionable.
Core Innovation
The app passively collects context from various sources (voice memos, photos of documents/whiteboards, quick text notes, web clips) and uses AI to automatically understand, categorize, extract key information, and create connections between different pieces of knowledge. Think of it as if Evernote and a personal AI assistant had a baby.
Key Features Leveraging AI
Intelligent capture and processing: Take a photo of a business card, receipt, or handwritten note—AI extracts structured data automatically. Record a voice memo during a meeting—it transcribes and creates action items. Clip a web article—AI generates a summary and tags.
Semantic search: Instead of keyword matching, you can ask "What was that restaurant my colleague recommended last month?" or "Find all my notes about the Johnson project" and the AI understands the intent and context.
Proactive insights: The AI notices patterns and surfaces relevant information. Before a meeting with someone, it shows you previous conversations and notes about them. When you're working on a project, it suggests related notes you took weeks ago.
Smart reminders and follow-ups: AI identifies commitments and action items from your notes and voice memos, automatically creating context-aware reminders.
Why KMP Makes This Perfect
Your core AI processing, note storage, sync logic, and business rules are identical across platforms—perfect for sharing via KMP. Users want this on their phone for quick capture, tablet for reviewing, and desktop for deep work. The data layer, API calls to Claude/other AI services, and synchronization logic all live in the common module.
Platform-specific implementations handle camera access, voice recording, and native widgets, but the heavy lifting is shared.
Technical Architecture
Common module: SQLDelight for local database, Ktor for networking with your backend/AI APIs, kotlinx.serialization for data handling, and your core business logic for note processing and AI orchestration.
AI integration: Use the Anthropic API (Claude) for text understanding, summarization, and extraction. For voice, integrate Whisper API or similar for transcription. You could also add vision capabilities for document/photo analysis.
Platform-specific: Native camera/microphone access on each platform, platform-specific UI (Compose Multiplatform or native), background sync using WorkManager on Android and background tasks on iOS.
What Makes This Feasible
You can start with a basic version: note capture, AI summarization, and simple search. Then iterate by adding voice memos, then photo capture, then proactive insights. The AI APIs handle the complex ML work, so you're orchestrating rather than building models from scratch. KMP means you build features once and deploy everywhere, significantly reducing development time.
Monetization Potential
Freemium model with basic AI features free and advanced capabilities (unlimited AI processing, advanced search, team sharing) as paid tiers. The AI costs are predictable per user, making the economics work.
Unique Angle
Unlike general note apps, this is specifically designed around AI-first workflows. Unlike ChatGPT, this maintains persistent context about your life and work. It's the intersection of personal knowledge management and AI assistance.
This project would teach you KMP architecture, mobile development best practices, AI API integration, and building a real product people would actually want to use. It's ambitious enough to be interesting but scoped appropriately for a solo developer or small team.
What aspects of this resonate with you? I can dive deeper into any part—the technical architecture, specific features, or how to approach building it incrementally.
