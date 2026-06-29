package com.cardstack.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardstack.app.ui.theme.IndigoAccent
import com.cardstack.app.ui.theme.OnSurfaceSecondary

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.importJson(it) } }

    // Status snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(8.dp))

            // ── Interest rate ──────────────────────────────────────────────────
            SettingsSection("Interest") {
                OutlinedTextField(
                    value = state.defaultInterestRate,
                    onValueChange = viewModel::onInterestRate,
                    label = { Text("Default Monthly Interest Rate (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoAccent),
                    trailingIcon = {
                        TextButton(onClick = viewModel::saveInterestRate) { Text("Save") }
                    }
                )
                Text(
                    "Used as the default in the interest simulator when no per-card rate is set.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceSecondary
                )
            }

            // ── Security ───────────────────────────────────────────────────────
            SettingsSection("Security") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Biometric Lock", style = MaterialTheme.typography.bodyMedium)
                        Text("Require fingerprint / face on open", style = MaterialTheme.typography.bodySmall, color = OnSurfaceSecondary)
                    }
                    Switch(
                        checked = state.biometricEnabled,
                        onCheckedChange = viewModel::onBiometricToggle,
                        colors = SwitchDefaults.colors(checkedThumbColor = IndigoAccent, checkedTrackColor = IndigoAccent.copy(alpha = 0.4f))
                    )
                }

                Divider()

                Text("Auto-lock after", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2, 5, 10).forEach { mins ->
                        FilterChip(
                            selected = state.lockTimeoutMinutes == mins,
                            onClick = { viewModel.onLockTimeout(mins) },
                            label = { Text("${mins}m") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IndigoAccent,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            // ── Data ───────────────────────────────────────────────────────────
            SettingsSection("Data") {
                if (state.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = IndigoAccent)
                }

                SettingsButton(
                    icon = Icons.Outlined.FileDownload,
                    title = "Export as JSON",
                    subtitle = "Full backup — cards, balances, transactions",
                    onClick = viewModel::exportJson
                )
                SettingsButton(
                    icon = Icons.Outlined.TableChart,
                    title = "Export as CSV",
                    subtitle = "Transactions only, opens in Excel / Sheets",
                    onClick = viewModel::exportCsv
                )
                SettingsButton(
                    icon = Icons.Outlined.FileUpload,
                    title = "Import from JSON",
                    subtitle = "Restore a previous backup",
                    onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) }
                )
            }

            // ── About ──────────────────────────────────────────────────────────
            SettingsSection("About") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Version", style = MaterialTheme.typography.bodyMedium)
                    Text("1.0", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceSecondary)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Storage", style = MaterialTheme.typography.bodyMedium)
                    Text("Local only · No cloud sync", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceSecondary)
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = IndigoAccent,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun SettingsButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, null, tint = IndigoAccent)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceSecondary)
            }
        }
    }
}
