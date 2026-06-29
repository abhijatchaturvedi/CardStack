package com.cardstack.app.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardstack.app.data.db.CardEntity
import com.cardstack.app.data.db.TransactionCategory
import com.cardstack.app.data.db.TransactionEntity
import com.cardstack.app.data.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionsUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val cards: List<CardEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: TransactionCategory? = null,
    val isAddSheetOpen: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repo: CardRepository
) : ViewModel() {

    private val cardIdFilter = MutableStateFlow<Long?>(null)
    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<TransactionCategory?>(null)
    private val isAddSheetOpen = MutableStateFlow(false)

    val uiState: StateFlow<TransactionsUiState> = combine(
        cardIdFilter.flatMapLatest { id ->
            if (id != null) repo.observeTransactionsByCard(id)
            else repo.observeAllTransactions()
        },
        repo.observeCards(),
        searchQuery,
        selectedCategory,
        isAddSheetOpen
    ) { txs, cards, query, category, sheetOpen ->
        val filtered = txs
            .filter { tx -> query.isBlank() || tx.merchant.contains(query, ignoreCase = true) }
            .filter { tx -> category == null || tx.category == category }
        TransactionsUiState(
            transactions = filtered,
            cards = cards,
            searchQuery = query,
            selectedCategory = category,
            isAddSheetOpen = sheetOpen
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionsUiState())

    fun init(cardId: Long?) { cardIdFilter.value = cardId }
    fun onSearch(q: String) { searchQuery.value = q }
    fun onCategoryFilter(c: TransactionCategory?) { selectedCategory.value = c }
    fun openAddSheet() { isAddSheetOpen.value = true }
    fun closeAddSheet() { isAddSheetOpen.value = false }

    fun addTransaction(
        cardId: Long,
        amount: Double,
        merchant: String,
        category: TransactionCategory,
        dateMillis: Long,
        notes: String
    ) {
        viewModelScope.launch {
            repo.saveTransaction(
                TransactionEntity(
                    cardId = cardId,
                    amount = amount,
                    merchant = merchant,
                    category = category,
                    date = dateMillis,
                    notes = notes
                )
            )
            isAddSheetOpen.value = false
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch { repo.deleteTransaction(tx) }
    }
}
