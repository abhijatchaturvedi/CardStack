package com.cardstack.app.ui.card

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardstack.app.ui.common.CardVisual
import com.cardstack.app.ui.common.DuePill
import com.cardstack.app.ui.theme.AmberDue
import com.cardstack.app.ui.theme.IndigoAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    cardId: Long,
    onEdit: () -> Unit,
    onViewTransactions: () -> Unit,
    onBack: () -> Unit,
    viewModel: CardDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBalanceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(cardId) { viewModel.load(cardId) }

    val card = state.card ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(card.nickname) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit") }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CardVisual(card = card)

            // Balance row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DuePill(dueDayOfMonth = card.paymentDueDate)
                TextButton(onClick = { showBalanceDialog = true }) {
                    Text("Update Balance", color = IndigoAccent)
                }
            }

            // Balance cards
            state.balance?.let { bal ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoTile("Outstanding", "₹${"%,.0f".format(bal.outstandingBalance)}", Modifier.weight(1f))
                    InfoTile("Min Due", "₹${"%,.0f".format(bal.minimumDue)}", Modifier.weight(1f))
                    InfoTile("Full Due", "₹${"%,.0f".format(bal.fullDue)}", Modifier.weight(1f))
                }

                // Utilisation bar
                val utilisation = if (card.creditLimit > 0) (bal.outstandingBalance / card.creditLimit).toFloat() else 0f
                UtilisationBar(utilisation = utilisation, limit = card.creditLimit)
            }

            Divider(color = MaterialTheme.colorScheme.outline)

            // Interest simulator
            Text("Interest Simulator", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = state.plannedPayment,
                onValueChange = viewModel::onPlannedPayment,
                label = { Text("Planned Payment (₹)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndigoAccent,
                    focusedLabelColor = IndigoAccent
                )
            )
            state.projectedInterest?.let { interest ->
                Surface(
                    color = if (interest > 0) AmberDue.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            null,
                            tint = if (interest > 0) AmberDue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            if (interest > 0)
                                "Projected interest this month: ₹${"%,.2f".format(interest)}"
                            else
                                "No interest — balance fully covered",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline)

            // Reward balance
            InfoTile(
                label = "${card.rewardCurrency.name} Balance",
                value = "${"%,.0f".format(card.rewardBalance)} pts  ≈  ₹${"%,.0f".format(card.rewardBalance * card.rewardRate / 100)}"
            )

            // Transactions shortcut
            OutlinedButton(
                onClick = onViewTransactions,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Receipt, null)
                Spacer(Modifier.width(8.dp))
                Text("View All Transactions")
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete ${card.nickname}?") },
            text = { Text("This will permanently delete this card and all its transactions.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteCard(card, onBack)
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showBalanceDialog) {
        BalanceUpdateDialog(
            initial = state.balance,
            onDismiss = { showBalanceDialog = false },
            onSave = { out, min, full ->
                viewModel.updateBalance(cardId, out, min, full)
                showBalanceDialog = false
            }
        )
    }
}

@Composable
private fun InfoTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun UtilisationBar(utilisation: Float, limit: Double) {
    val pct = (utilisation * 100).toInt()
    val color = when {
        utilisation >= 0.3f -> MaterialTheme.colorScheme.error
        utilisation >= 0.2f -> AmberDue
        else -> MaterialTheme.colorScheme.primary
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Utilisation $pct%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Limit ₹${"%,.0f".format(limit)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LinearProgressIndicator(
            progress = { utilisation.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        if (utilisation >= 0.3f) {
            Text(
                "High utilisation — may impact credit score",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun BalanceUpdateDialog(
    initial: com.cardstack.app.data.db.BalanceEntity?,
    onDismiss: () -> Unit,
    onSave: (Double, Double, Double) -> Unit
) {
    var outstanding by remember { mutableStateOf(initial?.outstandingBalance?.toString() ?: "") }
    var minDue by remember { mutableStateOf(initial?.minimumDue?.toString() ?: "") }
    var fullDue by remember { mutableStateOf(initial?.fullDue?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Balance") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = outstanding, onValueChange = { outstanding = it },
                    label = { Text("Outstanding (₹)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = minDue, onValueChange = { minDue = it },
                    label = { Text("Minimum Due (₹)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = fullDue, onValueChange = { fullDue = it },
                    label = { Text("Full Due (₹)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    outstanding.toDoubleOrNull() ?: 0.0,
                    minDue.toDoubleOrNull() ?: 0.0,
                    fullDue.toDoubleOrNull() ?: 0.0
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
