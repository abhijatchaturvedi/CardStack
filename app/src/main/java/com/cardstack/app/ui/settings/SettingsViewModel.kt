package com.cardstack.app.ui.settings

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardstack.app.data.ExportImportManager
import com.cardstack.app.data.db.TransactionEntity
import com.cardstack.app.data.repository.CardRepository
import com.cardstack.app.data.repository.SettingsRepository
import com.cardstack.app.sms.SmsParser
import com.cardstack.app.ui.theme.ThemeChoice
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
    val defaultInterestRate: String = "3.5",
    val biometricEnabled: Boolean = true,
    val lockTimeoutMinutes: Int = 2,
    val smsAutoImportEnabled: Boolean = false,
    val themeChoice: ThemeChoice = ThemeChoice.SYSTEM,
    val statusMessage: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository,
    private val exportManager: ExportImportManager,
    private val cardRepo: CardRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsUiState(
            defaultInterestRate = settingsRepo.getDefaultInterestRate().toString(),
            biometricEnabled = settingsRepo.getBiometricEnabled(),
            lockTimeoutMinutes = settingsRepo.getLockTimeoutMinutes(),
            smsAutoImportEnabled = settingsRepo.getSmsAutoImportEnabled(),
            themeChoice = ThemeChoice.valueOf(settingsRepo.themeChoiceFlow.value)
        )
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun onInterestRate(v: String) { _state.update { it.copy(defaultInterestRate = v) } }

    fun saveInterestRate() {
        val rate = _state.value.defaultInterestRate.toDoubleOrNull()
        if (rate != null) {
            settingsRepo.setDefaultInterestRate(rate)
            showStatus("Interest rate saved")
        }
    }

    fun onBiometricToggle(v: Boolean) {
        settingsRepo.setBiometricEnabled(v)
        _state.update { it.copy(biometricEnabled = v) }
    }

    fun onLockTimeout(minutes: Int) {
        settingsRepo.setLockTimeoutMinutes(minutes)
        _state.update { it.copy(lockTimeoutMinutes = minutes) }
    }

    fun onThemeChange(choice: ThemeChoice) {
        settingsRepo.setThemeChoice(choice.name)
        _state.update { it.copy(themeChoice = choice) }
    }

    fun onSmsToggle(enabled: Boolean) {
        settingsRepo.setSmsAutoImportEnabled(enabled)
        _state.update { it.copy(smsAutoImportEnabled = enabled) }
    }

    fun scanInbox() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val count = withContext(Dispatchers.IO) { importFromInbox() }
            _state.update { it.copy(isLoading = false) }
            showStatus(
                if (count >= 0) "Imported $count transaction${if (count != 1) "s" else ""} from SMS"
                else "Could not read SMS inbox"
            )
        }
    }

    private suspend fun importFromInbox(): Int {
        val ninetyDaysAgo = System.currentTimeMillis() - 90L * 24 * 3_600 * 1_000
        val cursor = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.BODY, Telephony.Sms.DATE),
            "${Telephony.Sms.DATE} > ?",
            arrayOf(ninetyDaysAgo.toString()),
            "${Telephony.Sms.DATE} DESC"
        ) ?: return -1

        val cards = cardRepo.getAllCards()
        var count = 0
        cursor.use {
            val bodyCol = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateCol = it.getColumnIndexOrThrow(Telephony.Sms.DATE)
            while (it.moveToNext()) {
                val body = it.getString(bodyCol) ?: continue
                val date = it.getLong(dateCol)
                val parsed = SmsParser.parse(body) ?: continue
                val card = cards.find { c -> c.lastFourDigits == parsed.lastFourDigits } ?: continue
                if (cardRepo.hasSimilarTransaction(card.id, parsed.amount, date)) continue
                cardRepo.saveTransaction(
                    TransactionEntity(
                        cardId = card.id,
                        amount = parsed.amount,
                        merchant = parsed.merchant,
                        category = parsed.category,
                        date = date
                    )
                )
                count++
            }
        }
        return count
    }

    fun exportJson() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            exportManager.exportJson().fold(
                onSuccess = { filename -> showStatus("Saved to Downloads: $filename") },
                onFailure = { showStatus("Export failed: ${it.message}") }
            )
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            exportManager.exportCsv().fold(
                onSuccess = { filename -> showStatus("CSV saved to Downloads: $filename") },
                onFailure = { showStatus("Export failed: ${it.message}") }
            )
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun importJson(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            exportManager.importJson(uri).fold(
                onSuccess = { showStatus("Import successful") },
                onFailure = { showStatus("Import failed: ${it.message}") }
            )
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun clearStatus() { _state.update { it.copy(statusMessage = null) } }

    private fun showStatus(msg: String) { _state.update { it.copy(statusMessage = msg) } }
}
