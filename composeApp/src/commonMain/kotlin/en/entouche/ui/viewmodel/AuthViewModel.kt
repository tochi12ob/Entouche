package en.entouche.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import en.entouche.data.repository.AuthRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val userEmail: String? = null,
    val userName: String? = null,
    val userId: String? = null,
    val error: String? = null
)

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    private val _isSigningUp = MutableStateFlow(false)
    val isSigningUp: StateFlow<Boolean> = _isSigningUp.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val user = authRepository.currentUser
                        _authState.value = AuthState(
                            isLoading = false,
                            isAuthenticated = true,
                            userEmail = user?.email,
                            userName = user?.userMetadata?.get("name")?.toString()?.removeSurrounding("\""),
                            userId = user?.id,
                            error = null
                        )
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _authState.value = AuthState(
                            isLoading = false,
                            isAuthenticated = false,
                            error = null
                        )
                    }
                    is SessionStatus.Initializing -> {
                        _authState.value = _authState.value.copy(isLoading = true)
                    }
                    is SessionStatus.RefreshFailure -> {
                        _authState.value = AuthState(
                            isLoading = false,
                            isAuthenticated = false,
                            error = "Session expired. Please sign in again."
                        )
                    }
                }
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isSigningIn.value = true
            _authState.value = _authState.value.copy(error = null)

            authRepository.signIn(email, password)
                .onFailure { e ->
                    _authState.value = _authState.value.copy(
                        error = e.message ?: "Sign in failed"
                    )
                }

            _isSigningIn.value = false
        }
    }

    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            _isSigningUp.value = true
            _authState.value = _authState.value.copy(error = null)

            authRepository.signUp(email, password, name)
                .onFailure { e ->
                    _authState.value = _authState.value.copy(
                        error = e.message ?: "Sign up failed"
                    )
                }

            _isSigningUp.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(error = null)

            authRepository.resetPassword(email)
                .onSuccess {
                    _authState.value = _authState.value.copy(
                        error = "Password reset email sent to $email"
                    )
                }
                .onFailure { e ->
                    _authState.value = _authState.value.copy(
                        error = e.message ?: "Failed to send reset email"
                    )
                }
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }

    fun getAccessToken(): String? = authRepository.getAccessToken()
    fun getUserId(): String? = authRepository.currentUserId
}
