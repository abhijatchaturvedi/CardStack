package com.cardstack.app.ui.rewards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardstack.app.data.db.TransactionCategory
import com.cardstack.app.ui.common.displayName
import com.cardstack.app.ui.theme.GreenDue
import com.cardstack.app.ui.theme.IndigoAccent

@Composable
fun RewardsScreen(viewModel: RewardsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editingCard by remember { mutableStateOf<CardRewardSummary?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Rewards", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        // Total rewards value
        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = IndigoAccent.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Outlined.WorkspacePremium, null, tint = IndigoAccent, modifier = Modifier.size(36.dp))
                    Column {
                        Text("Total Reward Value", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "₹${"%,.0f".format(state.totalRewardInr)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = IndigoAccent
                        )
                    }
                }
            }
        }

        // Per-card reward balances
        item {
            Text("Reward Balances", style = MaterialTheme.typography.titleMedium)
        }

        if (state.cards.isEmpty()) {
            item {
                Text("No cards yet", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            items(state.cards, key = { it.card.id }) { summary ->
                RewardCardRow(
                    summary = summary,
                    onEdit = { editingCard = summary }
                )
            }
        }

        // Best card suggester
        item { Divider() }
        item {
            Text("Best Card Suggester", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Pick a spending category to see which card earns the most",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(TransactionCategory.entries) { cat ->
                    FilterChip(
                        selected = state.selectedCategory == cat,
                        onClick = { viewModel.onCategorySelected(cat) },
                        label = { Text(cat.displayName()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoAccent,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        if (state.bestCardResults.isEmpty()) {
            item { Text("Add cards to see suggestions", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            itemsIndexed(state.bestCardResults) { index, result ->
                BestCardRow(rank = index + 1, result = result)
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }

    // Edit reward balance dialog
    editingCard?.let { summary ->
        var balanceInput by remember { mutableStateOf(summary.card.rewardBalance.toString()) }
        AlertDialog(
            onDismissRequest = { editingCard = null },
            title = { Text("Update ${summary.card.rewardCurrency.name} Balance") },
            text = {
                OutlinedTextField(
                    value = balanceInput,
                    onValueChange = { balanceInput = it },
                    label = { Text("${summary.card.rewardCurrency.name} balance") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoAccent)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    balanceInput.toDoubleOrNull()?.let { viewModel.updateRewardBalance(summary.card, it) }
                    editingCard = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editingCard = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun RewardCardRow(summary: CardRewardSummary, onEdit: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(summary.card.nickname, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${"%,.0f".format(summary.card.rewardBalance)} ${summary.card.rewardCurrency.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "≈ ₹${"%,.0f".format(summary.inrValue)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = GreenDue,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "@${summary.card.rewardRate}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, "Edit balance", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun BestCardRow(rank: Int, result: BestCardResult) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (rank == 1) IndigoAccent.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "#$rank",
                style = MaterialTheme.typography.titleMedium,
                color = rankColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(result.card.nickname, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(result.card.bankName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${result.rewardRate}%",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (rank == 1) IndigoAccent else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "₹${"%,.1f".format(result.estimatedRewardPer100)} per ₹100",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
