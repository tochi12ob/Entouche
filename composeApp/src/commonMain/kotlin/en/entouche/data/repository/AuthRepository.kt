package en.entouche.data.repository

import en.entouche.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepository {
    private val auth = SupabaseClient.auth

    val sessionStatus: Flow<SessionStatus> = auth.sessionStatus

    val isLoggedIn: Flow<Boolean> = sessionStatus.map { status ->
        status is SessionStatus.Authenticated
    }

    val currentUser: UserInfo?
        get() = auth.currentUserOrNull()

    val currentUserId: String?
        get() = currentUser?.id

    suspend fun signUp(email: String, password: String, name: String): Result<UserInfo> = runCatching {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        // After signup, get the current user
        auth.currentUserOrNull() ?: throw Exception("Sign up succeeded but user info not available")
    }

    suspend fun signIn(email: String, password: String): Result<UserInfo> = runCatching {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        auth.currentUserOrNull() ?: throw Exception("Sign in succeeded but user info not available")
    }

    suspend fun signOut(): Result<Unit> = runCatching {
        auth.signOut()
    }

    suspend fun resetPassword(email: String): Result<Unit> = runCatching {
        auth.resetPasswordForEmail(email)
    }

    suspend fun refreshSession(): Result<Unit> = runCatching {
        auth.refreshCurrentSession()
    }

    fun getAccessToken(): String? {
        return auth.currentAccessTokenOrNull()
    }
}
