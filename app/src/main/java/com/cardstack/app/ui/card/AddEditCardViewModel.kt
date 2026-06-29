package com.cardstack.app.ui.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardstack.app.data.db.*
import com.cardstack.app.data.repository.CardRepository
import com.cardstack.app.ui.theme.CardGradients
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditCardUiState(
    val nickname: String = "",
    val bankName: String = "",
    val lastFourDigits: String = "",
    val network: CardNetwork = CardNetwork.VISA,
    val creditLimit: String = "",
    val billingCycleDate: String = "1",
    val paymentDueDate: String = "15",
    val rewardCurrency: RewardCurrency = RewardCurrency.POINTS,
    val rewardRate: String = "1.0",
    val monthlyInterestRate: String = "3.5",
    val gradientIndex: Int = 0,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddEditCardViewModel @Inject constructor(
    private val repo: CardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditCardUiState())
    val state: StateFlow<AddEditCardUiState> = _state.asStateFlow()

    fun loadCard(cardId: Long) {
        viewModelScope.launch {
            val card = repo.getCard(cardId) ?: return@launch
            val gradIdx = CardGradients.indexOfFirst { (s, e) ->
                s.value.toLong() == card.cardColorStart && e.value.toLong() == card.cardColorEnd
            }.takeIf { it >= 0 } ?: 0
            _state.value = AddEditCardUiState(
                nickname = card.nickname,
                bankName = card.bankName,
                lastFourDigits = card.lastFourDigits,
                network = card.network,
                creditLimit = card.creditLimit.toString(),
                billingCycleDate = card.billingCycleDate.toString(),
                paymentDueDate = card.paymentDueDate.toString(),
                rewardCurrency = card.rewardCurrency,
                rewardRate = card.rewardRate.toString(),
                monthlyInterestRate = card.monthlyInterestRate.toString(),
                gradientIndex = gradIdx
            )
        }
    }

    fun onNickname(v: String) { _state.value = _state.value.copy(nickname = v) }
    fun onBankName(v: String) { _state.value = _state.value.copy(bankName = v) }
    fun onLastFour(v: String) { if (v.length <= 4 && v.all { it.isDigit() }) _state.value = _state.value.copy(lastFourDigits = v) }
    fun onNetwork(v: CardNetwork) { _state.value = _state.value.copy(network = v) }
    fun onCreditLimit(v: String) { _state.value = _state.value.copy(creditLimit = v) }
    fun onBillingCycleDate(v: String) { _state.value = _state.value.copy(billingCycleDate = v) }
    fun onPaymentDueDate(v: String) { _state.value = _state.value.copy(paymentDueDate = v) }
    fun onRewardCurrency(v: RewardCurrency) { _state.value = _state.value.copy(rewardCurrency = v) }
    fun onRewardRate(v: String) { _state.value = _state.value.copy(rewardRate = v) }
    fun onInterestRate(v: String) { _state.value = _state.value.copy(monthlyInterestRate = v) }
    fun onGradient(idx: Int) { _state.value = _state.value.copy(gradientIndex = idx) }

    fun save(existingId: Long? = null) {
        val s = _state.value
        val limit = s.creditLimit.toDoubleOrNull()
        val billing = s.billingCycleDate.toIntOrNull()
        val due = s.paymentDueDate.toIntOrNull()
        val rate = s.rewardRate.toDoubleOrNull()
        val interest = s.monthlyInterestRate.toDoubleOrNull()

        if (s.nickname.isBlank() || s.bankName.isBlank() || s.lastFourDigits.length != 4 ||
            limit == null || billing == null || due == null || rate == null || interest == null ||
            billing !in 1..31 || due !in 1..31
        ) {
            _state.value = s.copy(error = "Please fill all fields correctly")
            return
        }

        val gradient = CardGradients[s.gradientIndex.coerceIn(0, CardGradients.lastIndex)]
        val card = CardEntity(
            id = existingId ?: 0,
            nickname = s.nickname.trim(),
            bankName = s.bankName.trim(),
            lastFourDigits = s.lastFourDigits,
            network = s.network,
            creditLimit = limit,
            billingCycleDate = billing,
            paymentDueDate = due,
            rewardCurrency = s.rewardCurrency,
            rewardRate = rate,
            monthlyInterestRate = interest,
            cardColorStart = gradient.first.value.toLong(),
            cardColorEnd = gradient.second.value.toLong()
        )

        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, error = null)
            repo.saveCard(card)
            _state.value = _state.value.copy(isLoading = false, isSaved = true)
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
}
