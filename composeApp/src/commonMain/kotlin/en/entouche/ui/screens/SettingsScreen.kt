package en.entouche.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import en.entouche.audio.TranscriptionConfig
import en.entouche.ui.components.*
import en.entouche.ui.theme.*

@Composable
fun SettingsScreen(
    onSignOut: () -> Unit = {},
    userName: String? = null,
    userEmail: String? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var darkMode by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var aiSummaries by remember { mutableStateOf(true) }
    var autoTranscribe by remember { mutableStateOf(true) }
    var cloudSync by remember { mutableStateOf(true) }
    var groqApiKey by remember { mutableStateOf(TranscriptionConfig.apiKey) }
    var showApiKeyField by remember { mutableStateOf(false) }

    // Get user initials for avatar
    val userInitials = remember(userName, userEmail) {
        userName?.split(" ")?.mapNotNull { it.firstOrNull()?.uppercase() }?.take(2)?.joinToString("")
            ?: userEmail?.firstOrNull()?.uppercase()?.toString()
            ?: "U"
    }
    val displayName = userName ?: userEmail?.substringBefore("@") ?: "User"
    val displayEmail = userEmail ?: ""

    GradientBackground(
        modifier = modifier,
        animated = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = Dimensions.screenPaddingHorizontal)
        ) {
            Spacer(modifier = Modifier.height(Dimensions.spacingLg))

            // Header
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingXl))

            // Profile Section
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                size = GlassCardSize.Medium
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(TealWave, Seafoam)
                                ),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitials,
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextOnTeal
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        if (displayEmail.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = displayEmail,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = Success,
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Synced",
                                style = MaterialTheme.typography.labelSmall,
                                color = Success
                            )
                        }
                    }

                    GlassIconButton(
                        icon = Icons.Outlined.Edit,
                        onClick = { /* Edit profile */ },
                        style = GlassButtonStyle.Ghost,
                        size = 40.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingXl))

            // Appearance Section
            SettingsSection(title = "Appearance") {
                SettingsToggleItem(
                    icon = Icons.Outlined.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Use dark color theme",
                    isChecked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingLg))

            // AI Features Section
            SettingsSection(title = "AI Features") {
                SettingsToggleItem(
                    icon = Icons.Filled.AutoAwesome,
                    title = "AI Summaries",
                    subtitle = "Automatically generate summaries for notes",
                    isChecked = aiSummaries,
                    onCheckedChange = { aiSummaries = it },
                    accentColor = TealWave
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsToggleItem(
                    icon = Icons.Outlined.Mic,
                    title = "Auto Transcription",
                    subtitle = "Transcribe voice memos automatically",
                    isChecked = autoTranscribe,
                    onCheckedChange = { autoTranscribe = it },
                    accentColor = Seafoam
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Groq API Key configuration
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showApiKeyField = !showApiKeyField },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (TranscriptionConfig.isConfigured()) Success.copy(alpha = 0.15f) else Warning.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Filled.Key,
                                contentDescription = null,
                                tint = if (TranscriptionConfig.isConfigured()) Success else Warning,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Groq API Key",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (TranscriptionConfig.isConfigured()) "Configured - Tap to change" else "Required for AI transcription",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (TranscriptionConfig.isConfigured()) Success else TextSecondary
                            )
                        }

                        androidx.compose.material3.Icon(
                            imageVector = if (showApiKeyField) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = showApiKeyField,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            GlassTextField(
                                value = groqApiKey,
                                onValueChange = { groqApiKey = it },
                                placeholder = "Enter your Groq API key",
                                leadingIcon = Icons.Filled.VpnKey,
                                isPassword = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Get free key at console.groq.com",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )

                                GlassButton(
                                    text = "Save",
                                    onClick = {
                                        TranscriptionConfig.apiKey = groqApiKey
                                        showApiKeyField = false
                                    },
                                    style = GlassButtonStyle.Primary,
                                    size = GlassButtonSize.Small,
                                    enabled = groqApiKey.isNotBlank()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingLg))

            // Notifications Section
            SettingsSection(title = "Notifications") {
                SettingsToggleItem(
                    icon = Icons.Outlined.Notifications,
                    title = "Push Notifications",
                    subtitle = "Receive reminders and updates",
                    isChecked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingLg))

            // Data & Sync Section
            SettingsSection(title = "Data & Sync") {
                SettingsToggleItem(
                    icon = Icons.Outlined.CloudSync,
                    title = "Cloud Sync",
                    subtitle = "Sync notes across all devices",
                    isChecked = cloudSync,
                    onCheckedChange = { cloudSync = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsNavItem(
                    icon = Icons.Outlined.Storage,
                    title = "Storage",
                    subtitle = "24 notes • 8 voice memos • 12 MB used",
                    onClick = { /* Navigate to storage */ }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsNavItem(
                    icon = Icons.Outlined.Download,
                    title = "Export Data",
                    subtitle = "Download all your notes",
                    onClick = { /* Export data */ }
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingLg))

            // Privacy Section
            SettingsSection(title = "Privacy & Security") {
                SettingsNavItem(
                    icon = Icons.Outlined.Lock,
                    title = "App Lock",
                    subtitle = "Require authentication to open",
                    onClick = { /* Navigate to app lock */ }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsNavItem(
                    icon = Icons.Outlined.PrivacyTip,
                    title = "Privacy Policy",
                    onClick = { /* Open privacy policy */ }
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingLg))

            // About Section
            SettingsSection(title = "About") {
                SettingsNavItem(
                    icon = Icons.Outlined.Info,
                    title = "Version",
                    subtitle = "1.0.0 (Build 1)",
                    onClick = { /* Show version info */ }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsNavItem(
                    icon = Icons.Outlined.Email,
                    title = "Send Feedback",
                    onClick = { /* Open feedback */ }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsNavItem(
                    icon = Icons.Outlined.Star,
                    title = "Rate the App",
                    onClick = { /* Open app store */ }
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingXl))

            // Sign out button
            GlassButton(
                text = "Sign Out",
                onClick = onSignOut,
                style = GlassButtonStyle.Ghost,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Outlined.Logout
            )

            Spacer(modifier = Modifier.height(Dimensions.bottomNavHeight + Dimensions.spacingXl))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            size = GlassCardSize.Medium
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: androidx.compose.ui.graphics.Color = TealWave
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = accentColor.copy(alpha = 0.15f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TealWave,
                checkedTrackColor = TealWave.copy(alpha = 0.3f),
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = FrostedGlass
            )
        )
    }
}

@Composable
private fun SettingsNavItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = GlassWhite,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        androidx.compose.material3.Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onClick)
        )
    }
}
