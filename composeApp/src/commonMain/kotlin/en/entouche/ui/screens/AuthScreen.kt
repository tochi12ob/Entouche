package en.entouche.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import en.entouche.ui.components.*
import en.entouche.ui.theme.*
import en.entouche.ui.viewmodel.AuthViewModel

enum class AuthMode {
    SIGN_IN,
    SIGN_UP
}

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val isSigningIn by viewModel.isSigningIn.collectAsState()
    val isSigningUp by viewModel.isSigningUp.collectAsState()
    val isLoading = isSigningIn || isSigningUp

    fun validateAndSubmit() {
        when (authMode) {
            AuthMode.SIGN_IN -> {
                if (email.isNotBlank() && password.isNotBlank()) {
                    viewModel.signIn(email.trim(), password)
                }
            }
            AuthMode.SIGN_UP -> {
                if (email.isNotBlank() && password.isNotBlank() &&
                    password == confirmPassword && name.isNotBlank()) {
                    viewModel.signUp(email.trim(), password, name.trim())
                }
            }
        }
    }

    GradientBackground(
        modifier = modifier,
        animated = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimensions.screenPaddingHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo/App Name
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = TealWave,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Entouche",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "Your calm space for thoughts",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Auth Mode Toggle
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                size = GlassCardSize.Small
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AuthModeTab(
                        text = "Sign In",
                        selected = authMode == AuthMode.SIGN_IN,
                        onClick = {
                            authMode = AuthMode.SIGN_IN
                            viewModel.clearError()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    AuthModeTab(
                        text = "Sign Up",
                        selected = authMode == AuthMode.SIGN_UP,
                        onClick = {
                            authMode = AuthMode.SIGN_UP
                            viewModel.clearError()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Auth Form
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                size = GlassCardSize.Large
            ) {
                AnimatedContent(
                    targetState = authMode,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300))
                    }
                ) { mode ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Name field (only for sign up)
                        AnimatedVisibility(
                            visible = mode == AuthMode.SIGN_UP,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            GlassTextField(
                                value = name,
                                onValueChange = { name = it },
                                placeholder = "Enter your name",
                                leadingIcon = Icons.Filled.Person,
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Email field
                        GlassTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "Enter your email",
                            leadingIcon = Icons.Filled.Email,
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Password field
                        GlassTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "Enter your password",
                            leadingIcon = Icons.Filled.Lock,
                            isPassword = true,
                            keyboardType = KeyboardType.Password,
                            imeAction = if (mode == AuthMode.SIGN_UP) ImeAction.Next else ImeAction.Done,
                            onImeAction = {
                                if (mode == AuthMode.SIGN_IN) validateAndSubmit()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Confirm password (only for sign up)
                        AnimatedVisibility(
                            visible = mode == AuthMode.SIGN_UP,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column {
                                GlassTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    placeholder = "Confirm your password",
                                    leadingIcon = Icons.Filled.Lock,
                                    isPassword = true,
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done,
                                    onImeAction = { validateAndSubmit() },
                                    isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                                    Text(
                                        text = "Passwords don't match",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Error,
                                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                    )
                                }
                            }
                        }

                        // Error message
                        AnimatedVisibility(
                            visible = authState.error != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            authState.error?.let { error ->
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    size = GlassCardSize.Small
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = if (error.contains("sent")) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                            contentDescription = null,
                                            tint = if (error.contains("sent")) Success else Error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = error,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (error.contains("sent")) Success else Error
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Submit button
                        GlassButton(
                            text = when {
                                isLoading -> "Please wait..."
                                mode == AuthMode.SIGN_IN -> "Sign In"
                                else -> "Create Account"
                            },
                            onClick = { validateAndSubmit() },
                            style = GlassButtonStyle.Primary,
                            size = GlassButtonSize.Large,
                            enabled = !isLoading && email.isNotBlank() && password.isNotBlank() &&
                                    (mode == AuthMode.SIGN_IN || (password == confirmPassword && name.isNotBlank())),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Forgot password (only for sign in)
                        AnimatedVisibility(
                            visible = mode == AuthMode.SIGN_IN,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            TextButton(
                                onClick = {
                                    if (email.isNotBlank()) {
                                        viewModel.resetPassword(email.trim())
                                    }
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = "Forgot Password?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TealWave
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Terms text (for sign up)
            AnimatedVisibility(
                visible = authMode == AuthMode.SIGN_UP,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "By creating an account, you agree to our Terms of Service and Privacy Policy",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AuthModeTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) TealWave else TextMuted
        )
    }
}
