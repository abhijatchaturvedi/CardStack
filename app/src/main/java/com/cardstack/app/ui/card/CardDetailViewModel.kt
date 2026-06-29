package com.cardstack.app.ui.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardstack.app.data.db.BalanceEntity
import com.cardstack.app.data.db.CardEntity
import com.cardstack.app.data.db.TransactionEntity
import com.cardstack.app.data.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CardDetailUiState(
    val card: CardEntity? = null,
    val balance: BalanceEntity? = null,
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val projectedInterest: Double? = null,
    val plannedPayment: String = "",
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CardDetailViewModel @Inject constructor(
    private val repo: CardRepository
) : ViewModel() {

    private val cardId = MutableStateFlow<Long?>(null)
    private val plannedPayment = MutableStateFlow("")

    val uiState: StateFlow<CardDetailUiState> = cardId
        .filterNotNull()
        .flatMapLatest { id ->
            combine(
                repo.observeCards().map { list -> list.find { it.id == id } },
                repo.observeBalance(id),
                repo.observeTransactionsByCard(id),
                plannedPayment
            ) { card, balance, txs, payment ->
                val interest = computeInterest(card, balance, payment)
                CardDetailUiState(
                    card = card,
                    balance = balance,
                    recentTransactions = txs.take(5),
                    projectedInterest = interest,
                    plannedPayment = payment
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CardDetailUiState())

    fun load(id: Long) { cardId.value = id }

    fun onPlannedPayment(v: String) { plannedPayment.value = v }

    fun updateBalance(cardId: Long, outstanding: Double, minDue: Double, fullDue: Double) {
        viewModelScope.launch {
            val existing = repo.getBalance(cardId)
            repo.saveBalance(
                BalanceEntity(
                    id = existing?.id ?: 0,
                    cardId = cardId,
                    outstandingBalance = outstanding,
                    minimumDue = minDue,
                    fullDue = fullDue
                )
            )
        }
    }

    fun deleteCard(card: CardEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.deleteCard(card)
            onDone()
        }
    }

    private fun computeInterest(card: CardEntity?, balance: BalanceEntity?, payment: String): Double? {
        val outstanding = balance?.outstandingBalance ?: return null
        val planned = payment.toDoubleOrNull() ?: return null
        val rate = card?.monthlyInterestRate ?: 3.5
        val revolving = (outstanding - planned).coerceAtLeast(0.0)
        return revolving * (rate / 100.0)
    }
}
