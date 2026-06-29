package com.cardstack.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardstack.app.data.db.CardEntity
import com.cardstack.app.data.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class HomeUiState(
    val cards: List<CardEntity> = emptyList(),
    val totalOutstanding: Double = 0.0,
    val totalRewardValueInr: Double = 0.0,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: CardRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = repo.observeCards()
        .map { cards ->
            val outstanding = cards.sumOf { card ->
                repo.getBalance(card.id)?.outstandingBalance ?: 0.0
            }
            val rewardValue = cards.sumOf { card ->
                // Convert reward balance to INR using reward rate
                // reward rate % applied to a nominal base — simplified: balance * rate / 100
                card.rewardBalance * (card.rewardRate / 100.0)
            }
            HomeUiState(
                cards = cards.sortedBy { it.paymentDueDate },
                totalOutstanding = outstanding,
                totalRewardValueInr = rewardValue
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
