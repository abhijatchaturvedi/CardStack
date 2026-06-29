package com.cardstack.app.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardstack.app.data.db.TransactionCategory
import com.cardstack.app.data.db.TransactionEntity
import com.cardstack.app.ui.common.color
import com.cardstack.app.ui.common.displayName
import com.cardstack.app.ui.common.icon
import com.cardstack.app.ui.theme.IndigoAccent
import com.cardstack.app.ui.theme.OnSurfaceSecondary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    cardId: Long?,
    onBack: () -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(cardId) { viewModel.init(cardId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openAddSheet,
                containerColor = IndigoAccent
            ) { Icon(Icons.Default.Add, "Add transaction") }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearch,
                placeholder = { Text("Search merchant…") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoAccent)
            )

            // Category filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedCategory == null,
                        onClick = { viewModel.onCategoryFilter(null) },
                        label = { Text("All") },
                        colors = filterChipColors()
                    )
                }
                items(TransactionCategory.entries) { cat ->
                    FilterChip(
                        selected = state.selectedCategory == cat,
                        onClick = { viewModel.onCategoryFilter(if (state.selectedCategory == cat) null else cat) },
                        label = { Text(cat.displayName()) },
                        colors = filterChipColors()
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            if (state.transactions.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transactions yet", color = OnSurfaceSecondary)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.transactions, key = { it.id }) { tx ->
                        TransactionRow(
                            tx = tx,
                            cardName = state.cards.find { it.id == tx.cardId }?.nickname ?: "",
                            onDelete = { viewModel.deleteTransaction(tx) }
                        )
                    }
                }
            }
        }
    }

    if (state.isAddSheetOpen) {
        AddTransactionSheet(
            cards = state.cards,
            preselectedCardId = cardId,
            onDismiss = viewModel::closeAddSheet,
            onSave = viewModel::addTransaction
        )
    }
}

@Composable
private fun TransactionRow(
    tx: TransactionEntity,
    cardName: String,
    onDelete: () -> Unit
) {
    val dateFmt = remember { DateTimeFormatter.ofPattern("dd MMM").withZone(ZoneId.systemDefault()) }
    val catColor = tx.category.color()

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(catColor.copy(alpha = 0.18f), MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tx.category.icon(),
                    contentDescription = null,
                    tint = catColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.merchant, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "$cardName · ${tx.category.displayName()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceSecondary
                )
            }

            // Amount + date
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₹${"%,.0f".format(tx.amount)}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    dateFmt.format(Instant.ofEpochMilli(tx.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceSecondary
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete, null,
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun filterChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = IndigoAccent,
    selectedLabelColor = Color.White
)
