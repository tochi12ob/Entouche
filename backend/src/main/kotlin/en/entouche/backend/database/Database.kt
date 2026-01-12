package en.entouche.backend.database

import en.entouche.backend.config.AppConfig
import en.entouche.backend.models.Note
import en.entouche.backend.models.NoteType
import en.entouche.backend.models.User
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

// Database Tables
object Users : UUIDTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val name = varchar("name", 255)
    val passwordHash = varchar("password_hash", 255)
    val createdAt = timestamp("created_at").default(Clock.System.now())
}

object Notes : UUIDTable("notes") {
    val userId = uuid("user_id").references(Users.id)
    val title = varchar("title", 500)
    val content = text("content")
    val type = enumerationByName<NoteType>("type", 20)
    val tags = text("tags").default("[]") // JSON array stored as text
    val aiSummary = text("ai_summary").nullable()
    val actionItems = text("action_items").default("[]") // JSON array
    val transcription = text("transcription").nullable()
    val audioUrl = varchar("audio_url", 1000).nullable()
    val imageUrl = varchar("image_url", 1000).nullable()
    val reminderTime = timestamp("reminder_time").nullable()
    val isCompleted = bool("is_completed").default(false)
    val createdAt = timestamp("created_at").default(Clock.System.now())
    val updatedAt = timestamp("updated_at").default(Clock.System.now())
}

object VoiceMemos : UUIDTable("voice_memos") {
    val userId = uuid("user_id").references(Users.id)
    val noteId = uuid("note_id").references(Notes.id).nullable()
    val audioUrl = varchar("audio_url", 1000)
    val duration = integer("duration") // seconds
    val transcription = text("transcription").nullable()
    val summary = text("summary").nullable()
    val actionItems = text("action_items").default("[]")
    val createdAt = timestamp("created_at").default(Clock.System.now())
}

// Database initialization
object DatabaseFactory {
    // Default user ID - must match the client's userId
    const val DEFAULT_USER_ID = "00000000-0000-0000-0000-000000000001"

    fun init() {
        val database = Database.connect(
            url = AppConfig.databaseUrl,
            driver = AppConfig.databaseDriver
        )

        transaction(database) {
            SchemaUtils.create(Users, Notes, VoiceMemos)

            // Create default user if it doesn't exist
            val defaultUserId = UUID.fromString(DEFAULT_USER_ID)
            val existingUser = Users.selectAll().where { Users.id eq defaultUserId }.singleOrNull()

            if (existingUser == null) {
                Users.insert {
                    it[id] = defaultUserId
                    it[email] = "default@entouche.app"
                    it[name] = "Default User"
                    it[passwordHash] = "not-used" // Password not needed for default user
                }
            }
        }
    }
}

// Repository classes
class UserRepository {
    fun create(email: String, name: String, passwordHash: String): User = transaction {
        val id = Users.insertAndGetId {
            it[Users.email] = email
            it[Users.name] = name
            it[Users.passwordHash] = passwordHash
        }

        User(
            id = id.value.toString(),
            email = email,
            name = name,
            createdAt = Clock.System.now()
        )
    }

    fun findByEmail(email: String): UserWithPassword? = transaction {
        Users.selectAll().where { Users.email eq email }
            .map { row ->
                UserWithPassword(
                    id = row[Users.id].value.toString(),
                    email = row[Users.email],
                    name = row[Users.name],
                    passwordHash = row[Users.passwordHash],
                    createdAt = row[Users.createdAt]
                )
            }
            .singleOrNull()
    }

    fun findById(id: String): User? = transaction {
        Users.selectAll().where { Users.id eq UUID.fromString(id) }
            .map { row ->
                User(
                    id = row[Users.id].value.toString(),
                    email = row[Users.email],
                    name = row[Users.name],
                    createdAt = row[Users.createdAt]
                )
            }
            .singleOrNull()
    }
}

class NoteRepository {
    fun create(
        userId: String,
        title: String,
        content: String,
        type: NoteType,
        tags: List<String> = emptyList(),
        audioUrl: String? = null,
        imageUrl: String? = null,
        reminderTime: Instant? = null
    ): Note = transaction {
        val now = Clock.System.now()
        val id = Notes.insertAndGetId {
            it[Notes.userId] = UUID.fromString(userId)
            it[Notes.title] = title
            it[Notes.content] = content
            it[Notes.type] = type
            it[Notes.tags] = tags.joinToString(",")
            it[Notes.audioUrl] = audioUrl
            it[Notes.imageUrl] = imageUrl
            it[Notes.reminderTime] = reminderTime
            it[Notes.createdAt] = now
            it[Notes.updatedAt] = now
        }

        Note(
            id = id.value.toString(),
            userId = userId,
            title = title,
            content = content,
            type = type,
            tags = tags,
            createdAt = now,
            updatedAt = now,
            audioUrl = audioUrl,
            imageUrl = imageUrl,
            reminderTime = reminderTime
        )
    }

    fun findById(id: String): Note? = transaction {
        Notes.selectAll().where { Notes.id eq UUID.fromString(id) }
            .map { it.toNote() }
            .singleOrNull()
    }

    fun findByUserId(userId: String, limit: Int = 50, offset: Int = 0): List<Note> = transaction {
        Notes.selectAll()
            .where { Notes.userId eq UUID.fromString(userId) }
            .orderBy(Notes.updatedAt, SortOrder.DESC)
            .limit(limit).offset(offset.toLong())
            .map { it.toNote() }
    }

    fun findByUserIdAndType(userId: String, type: NoteType): List<Note> = transaction {
        Notes.selectAll()
            .where { (Notes.userId eq UUID.fromString(userId)) and (Notes.type eq type) }
            .orderBy(Notes.updatedAt, SortOrder.DESC)
            .map { it.toNote() }
    }

    fun update(id: String, updates: NoteUpdates): Note? = transaction {
        val updated = Notes.update({ Notes.id eq UUID.fromString(id) }) {
            updates.title?.let { title -> it[Notes.title] = title }
            updates.content?.let { content -> it[Notes.content] = content }
            updates.tags?.let { tags -> it[Notes.tags] = tags.joinToString(",") }
            updates.aiSummary?.let { summary -> it[Notes.aiSummary] = summary }
            updates.actionItems?.let { items -> it[Notes.actionItems] = items.joinToString("|||") }
            updates.reminderTime?.let { time -> it[Notes.reminderTime] = time }
            updates.isCompleted?.let { completed -> it[Notes.isCompleted] = completed }
            it[Notes.updatedAt] = Clock.System.now()
        }

        if (updated > 0) findById(id) else null
    }

    fun delete(id: String): Boolean = transaction {
        Notes.deleteWhere { Notes.id eq UUID.fromString(id) } > 0
    }

    fun search(userId: String, query: String): List<Note> = transaction {
        Notes.selectAll()
            .where {
                (Notes.userId eq UUID.fromString(userId)) and
                        ((Notes.title like "%$query%") or (Notes.content like "%$query%"))
            }
            .orderBy(Notes.updatedAt, SortOrder.DESC)
            .map { it.toNote() }
    }

    fun countByUserId(userId: String): Long = transaction {
        Notes.selectAll()
            .where { Notes.userId eq UUID.fromString(userId) }
            .count()
    }

    fun countByUserIdAndType(userId: String, type: NoteType): Long = transaction {
        Notes.selectAll()
            .where { (Notes.userId eq UUID.fromString(userId)) and (Notes.type eq type) }
            .count()
    }

    private fun ResultRow.toNote(): Note {
        val tagsString = this[Notes.tags]
        val actionItemsString = this[Notes.actionItems]

        return Note(
            id = this[Notes.id].value.toString(),
            userId = this[Notes.userId].toString(),
            title = this[Notes.title],
            content = this[Notes.content],
            type = this[Notes.type],
            tags = if (tagsString.isBlank()) emptyList() else tagsString.split(","),
            aiSummary = this[Notes.aiSummary],
            actionItems = if (actionItemsString.isBlank()) emptyList() else actionItemsString.split("|||"),
            transcription = this[Notes.transcription],
            audioUrl = this[Notes.audioUrl],
            imageUrl = this[Notes.imageUrl],
            reminderTime = this[Notes.reminderTime],
            isCompleted = this[Notes.isCompleted],
            createdAt = this[Notes.createdAt],
            updatedAt = this[Notes.updatedAt]
        )
    }
}

data class UserWithPassword(
    val id: String,
    val email: String,
    val name: String,
    val passwordHash: String,
    val createdAt: Instant
)

data class NoteUpdates(
    val title: String? = null,
    val content: String? = null,
    val tags: List<String>? = null,
    val aiSummary: String? = null,
    val actionItems: List<String>? = null,
    val reminderTime: Instant? = null,
    val isCompleted: Boolean? = null
)
