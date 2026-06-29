package com.cardstack.app.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cardstack.app.data.db.CardEntity
import com.cardstack.app.data.db.TransactionCategory
import com.cardstack.app.ui.common.displayName
import com.cardstack.app.ui.theme.IndigoAccent
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionSheet(
    cards: List<CardEntity>,
    preselectedCardId: Long?,
    onDismiss: () -> Unit,
    onSave: (cardId: Long, amount: Double, merchant: String, category: TransactionCategory, dateMillis: Long, notes: String) -> Unit
) {
    var selectedCardId by remember { mutableStateOf(preselectedCardId ?: cards.firstOrNull()?.id ?: 0L) }
    var amount by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(TransactionCategory.OTHER) }
    var notes by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(LocalDate.now().toEpochMillis()) }
    var cardMenuExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Add Transaction", style = MaterialTheme.typography.titleLarge)

            // Card picker
            if (preselectedCardId == null) {
                ExposedDropdownMenuBox(
                    expanded = cardMenuExpanded,
                    onExpandedChange = { cardMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = cards.find { it.id == selectedCardId }?.nickname ?: "Select card",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Card") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(cardMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoAccent)
                    )
                    ExposedDropdownMenu(
                        expanded = cardMenuExpanded,
                        onDismissRequest = { cardMenuExpanded = false }
                    ) {
                        cards.forEach { card ->
                            DropdownMenuItem(
                                text = { Text(card.nickname) },
                                onClick = { selectedCardId = card.id; cardMenuExpanded = false }
                            )
                        }
                    }
                }
            }

            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (₹)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoAccent)
            )

            // Merchant
            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it },
                label = { Text("Merchant") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoAccent)
            )

            // Category chips
            Text("Category", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TransactionCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = cat == category,
                        onClick = { category = cat },
                        label = { Text(cat.displayName()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoAccent,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoAccent)
            )

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    when {
                        amt == null || amt <= 0 -> error = "Enter a valid amount"
                        merchant.isBlank()      -> error = "Enter a merchant name"
                        selectedCardId == 0L    -> error = "Select a card"
                        else -> onSave(selectedCardId, amt, merchant.trim(), category, dateMillis, notes.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent)
            ) { Text("Save Transaction") }
        }
    }
}

private fun LocalDate.toEpochMillis(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
