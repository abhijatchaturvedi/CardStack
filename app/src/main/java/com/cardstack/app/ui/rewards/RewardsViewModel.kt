package com.cardstack.app.ui.rewards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardstack.app.data.db.CardEntity
import com.cardstack.app.data.db.TransactionCategory
import com.cardstack.app.data.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CardRewardSummary(
    val card: CardEntity,
    val inrValue: Double
)

data class BestCardResult(
    val card: CardEntity,
    val rewardRate: Double,
    val estimatedRewardPer100: Double
)

data class RewardsUiState(
    val cards: List<CardRewardSummary> = emptyList(),
    val totalRewardInr: Double = 0.0,
    val selectedCategory: TransactionCategory = TransactionCategory.FOOD,
    val bestCardResults: List<BestCardResult> = emptyList()
)

@HiltViewModel
class RewardsViewModel @Inject constructor(
    private val repo: CardRepository
) : ViewModel() {

    private val selectedCategory = MutableStateFlow(TransactionCategory.FOOD)

    val uiState: StateFlow<RewardsUiState> = combine(
        repo.observeCards(),
        selectedCategory
    ) { cards, category ->
        val summaries = cards.map { card ->
            CardRewardSummary(
                card = card,
                inrValue = card.rewardBalance * (card.rewardRate / 100.0)
            )
        }
        val totalInr = summaries.sumOf { it.inrValue }

        // Best card: all cards ranked by reward rate for the selected category
        // (per-category rates not modelled yet — uses the card's universal reward rate)
        val bestCards = cards
            .map { card ->
                BestCardResult(
                    card = card,
                    rewardRate = card.rewardRate,
                    estimatedRewardPer100 = 100.0 * (card.rewardRate / 100.0)
                )
            }
            .sortedByDescending { it.rewardRate }

        RewardsUiState(
            cards = summaries,
            totalRewardInr = totalInr,
            selectedCategory = category,
            bestCardResults = bestCards
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RewardsUiState())

    fun onCategorySelected(cat: TransactionCategory) { selectedCategory.value = cat }

    fun updateRewardBalance(card: CardEntity, newBalance: Double) {
        viewModelScope.launch {
            repo.saveCard(card.copy(rewardBalance = newBalance))
        }
    }
}
