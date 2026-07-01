package com.cardstack.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.cardstack.app.R
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardstack.app.data.db.CardEntity
import com.cardstack.app.ui.common.CardVisual
import com.cardstack.app.ui.common.DuePill
import com.cardstack.app.ui.theme.IndigoAccent

@Composable
fun HomeScreen(
    onCardClick: (Long) -> Unit,
    onAddCard: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCard,
                containerColor = IndigoAccent
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add card")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 16.dp,
                bottom = padding.calculateBottomPadding() + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { SummaryHeader(state) }

            if (state.cards.isEmpty()) {
                item { EmptyCardsHint() }
            } else {
                item {
                    Text(
                        "Your Cards",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(state.cards, key = { it.id }) { card ->
                    HomeCardRow(card = card, onClick = { onCardClick(card.id) })
                }
            }
        }
    }
}

@Composable
private fun SummaryHeader(state: HomeUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Image(
            painter = painterResource(R.drawable.logo_wordmark),
            contentDescription = "CardStack",
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .aspectRatio(2.47f),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCard(
                label = "Total Outstanding",
                value = "₹${"%,.0f".format(state.totalOutstanding)}",
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "Reward Value",
                value = "₹${"%,.0f".format(state.totalRewardValueInr)}",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HomeCardRow(card: CardEntity, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        CardVisual(card = card)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                card.nickname,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            DuePill(dueDayOfMonth = card.paymentDueDate)
        }
    }
}

@Composable
private fun EmptyCardsHint() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "No cards yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Tap + to add your first card",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
