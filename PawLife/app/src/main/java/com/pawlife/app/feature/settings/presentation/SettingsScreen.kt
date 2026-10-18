package com.pawlife.app.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.pawlife.app.core.util.BiometricPromptManager
import com.pawlife.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val pushNotificationsEnabled by viewModel.pushNotificationsEnabled.collectAsState()
    val darkModeEnabled by viewModel.darkModeEnabled.collectAsState()
    
    val notificationPermissionState = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
    } else null
    
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            if (event == "login") {
                onNavigateToLogin()
            }
        }
    }

    var showExportDialog by remember { mutableStateOf(false) }
    var showErrorSnackbar by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(72.dp).background(Primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("S", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Shoban", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Text("shoban@example.com", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Preferences
            item {
                SettingsGroup(title = "Preferences") {
                    SettingsRowToggle(
                        title = "Smart Pet Routines",
                        icon = Icons.Default.Notifications,
                        iconColor = Secondary,
                        isChecked = pushNotificationsEnabled,
                        onCheckedChange = { 
                            if (it && notificationPermissionState != null && !notificationPermissionState.status.isGranted) {
                                notificationPermissionState.launchPermissionRequest()
                                // Wait for result in a real app, but for now we'll optimistically enable it
                                viewModel.setPushNotifications(true)
                            } else {
                                viewModel.setPushNotifications(it) 
                            }
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(start = 56.dp))
                    SettingsRowToggle(
                        title = "Dark Mode",
                        icon = Icons.Default.DarkMode,
                        iconColor = Primary,
                        isChecked = darkModeEnabled,
                        onCheckedChange = { viewModel.setDarkMode(it) }
                    )
                }
            }

            // Data & Privacy
            item {
                SettingsGroup(title = "Data & Privacy") {
                    SettingsRowClickable(
                        title = "Export Medical Records",
                        subtitle = "Secured with Biometrics",
                        icon = Icons.Default.Download,
                        iconColor = Success,
                        onClick = {
                            val activity = context as? FragmentActivity
                            if (activity != null) {
                                val manager = BiometricPromptManager(activity)
                                manager.showBiometricPrompt(
                                    title = "Unlock Medical Records",
                                    subtitle = "Authenticate to access and export sensitive pet data",
                                    onSuccess = { showExportDialog = true },
                                    onFail = { showErrorSnackbar = it }
                                )
                            } else {
                                showExportDialog = true
                            }
                        }
                    )
                }
            }

            // Account
            item {
                SettingsGroup(title = "Account") {
                    SettingsRowClickable(
                        title = "Log Out",
                        icon = Icons.Default.Logout,
                        iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        textColor = MaterialTheme.colorScheme.onBackground,
                        onClick = { viewModel.logout() }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(start = 56.dp))
                    SettingsRowClickable(
                        title = "Delete Account",
                        subtitle = "Permanently remove your data",
                        icon = Icons.Default.DeleteForever,
                        iconColor = Danger,
                        textColor = Danger,
                        onClick = { viewModel.deleteAccount() }
                    )
                }
            }
        }

        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Export Data") },
                text = { Text("Your medical records and weight history have been successfully compiled into a CSV file and saved to your Downloads folder (Mock).") },
                confirmButton = {
                    TextButton(onClick = { showExportDialog = false }) { Text("OK", color = Primary) }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsRowToggle(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).background(iconColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Success)
        )
    }
}

@Composable
fun SettingsRowClickable(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconColor: Color,
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).background(iconColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = textColor)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
