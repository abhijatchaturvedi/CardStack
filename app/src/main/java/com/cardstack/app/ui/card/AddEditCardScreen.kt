package com.cardstack.app.ui.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardstack.app.data.db.CardNetwork
import com.cardstack.app.data.db.RewardCurrency
import com.cardstack.app.ui.common.CardVisual
import com.cardstack.app.ui.theme.CardGradients
import com.cardstack.app.ui.theme.IndigoAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCardScreen(
    cardId: Long?,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddEditCardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(cardId) {
        if (cardId != null) viewModel.loadCard(cardId)
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }

    // Build a preview card to show live gradient changes
    val previewCard = rememberUpdatedPreviewCard(state)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (cardId == null) "Add Card" else "Edit Card") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live card preview
            if (previewCard != null) {
                CardVisual(card = previewCard, modifier = Modifier.padding(top = 8.dp))
            }

            // Gradient picker
            SectionLabel("Card Style")
            GradientPicker(
                selected = state.gradientIndex,
                onSelect = viewModel::onGradient
            )

            SectionLabel("Card Info")
            AppTextField("Nickname (e.g. HDFC Regalia)", state.nickname, viewModel::onNickname)
            AppTextField("Bank Name", state.bankName, viewModel::onBankName)
            AppTextField(
                "Last 4 Digits", state.lastFourDigits, viewModel::onLastFour,
                keyboardType = KeyboardType.Number
            )

            SectionLabel("Network")
            NetworkPicker(state.network, viewModel::onNetwork)

            SectionLabel("Limits & Dates")
            AppTextField(
                "Credit Limit (₹)", state.creditLimit, viewModel::onCreditLimit,
                keyboardType = KeyboardType.Decimal
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTextField(
                    "Billing Cycle Day", state.billingCycleDate, viewModel::onBillingCycleDate,
                    keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f)
                )
                AppTextField(
                    "Due Day", state.paymentDueDate, viewModel::onPaymentDueDate,
                    keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f)
                )
            }

            SectionLabel("Rewards")
            RewardCurrencyPicker(state.rewardCurrency, viewModel::onRewardCurrency)
            AppTextField(
                "Reward Rate (%)", state.rewardRate, viewModel::onRewardRate,
                keyboardType = KeyboardType.Decimal
            )

            SectionLabel("Interest")
            AppTextField(
                "Monthly Interest Rate (%)", state.monthlyInterestRate, viewModel::onInterestRate,
                keyboardType = KeyboardType.Decimal
            )

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = { viewModel.save(cardId) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Check, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (cardId == null) "Add Card" else "Save Changes")
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun rememberUpdatedPreviewCard(state: AddEditCardUiState) =
    remember(state.nickname, state.bankName, state.lastFourDigits, state.network,
        state.rewardCurrency, state.rewardRate, state.paymentDueDate, state.gradientIndex) {
        val gradient = CardGradients.getOrElse(state.gradientIndex) { CardGradients[0] }
        com.cardstack.app.data.db.CardEntity(
            nickname = state.nickname.ifBlank { "Preview" },
            bankName = state.bankName.ifBlank { "Bank" },
            lastFourDigits = state.lastFourDigits.padStart(4, '•'),
            network = state.network,
            creditLimit = state.creditLimit.toDoubleOrNull() ?: 0.0,
            billingCycleDate = state.billingCycleDate.toIntOrNull() ?: 1,
            paymentDueDate = state.paymentDueDate.toIntOrNull() ?: 15,
            rewardCurrency = state.rewardCurrency,
            rewardRate = state.rewardRate.toDoubleOrNull() ?: 1.0,
            cardColorStart = gradient.first.value.toLong(),
            cardColorEnd = gradient.second.value.toLong()
        )
    }

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun AppTextField(
    label: String,
    value: String,
    onValue: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = IndigoAccent,
            focusedLabelColor = IndigoAccent
        )
    )
}

@Composable
private fun GradientPicker(selected: Int, onSelect: (Int) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        itemsIndexed(CardGradients) { idx, (start, end) ->
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(start, end)))
                    .then(
                        if (idx == selected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        else Modifier
                    )
                    .clickable { onSelect(idx) }
            )
        }
    }
}

@Composable
private fun NetworkPicker(selected: CardNetwork, onSelect: (CardNetwork) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CardNetwork.entries.forEach { network ->
            FilterChip(
                selected = network == selected,
                onClick = { onSelect(network) },
                label = { Text(network.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IndigoAccent,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
private fun RewardCurrencyPicker(selected: RewardCurrency, onSelect: (RewardCurrency) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RewardCurrency.entries.forEach { rc ->
            FilterChip(
                selected = rc == selected,
                onClick = { onSelect(rc) },
                label = { Text(rc.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IndigoAccent,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}
