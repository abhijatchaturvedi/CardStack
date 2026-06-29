package com.cardstack.app.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardstack.app.data.ExportImportManager
import com.cardstack.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val defaultInterestRate: String = "3.5",
    val biometricEnabled: Boolean = true,
    val lockTimeoutMinutes: Int = 2,
    val statusMessage: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository,
    private val exportManager: ExportImportManager
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsUiState(
            defaultInterestRate = settingsRepo.getDefaultInterestRate().toString(),
            biometricEnabled = settingsRepo.getBiometricEnabled(),
            lockTimeoutMinutes = settingsRepo.getLockTimeoutMinutes()
        )
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun onInterestRate(v: String) { _state.value = _state.value.copy(defaultInterestRate = v) }

    fun saveInterestRate() {
        val rate = _state.value.defaultInterestRate.toDoubleOrNull()
        if (rate != null) {
            settingsRepo.setDefaultInterestRate(rate)
            showStatus("Interest rate saved")
        }
    }

    fun onBiometricToggle(v: Boolean) {
        settingsRepo.setBiometricEnabled(v)
        _state.value = _state.value.copy(biometricEnabled = v)
    }

    fun onLockTimeout(minutes: Int) {
        settingsRepo.setLockTimeoutMinutes(minutes)
        _state.value = _state.value.copy(lockTimeoutMinutes = minutes)
    }

    fun exportJson() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            exportManager.exportJson().fold(
                onSuccess = { filename -> showStatus("Saved to Downloads: $filename") },
                onFailure = { showStatus("Export failed: ${it.message}") }
            )
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            exportManager.exportCsv().fold(
                onSuccess = { filename -> showStatus("CSV saved to Downloads: $filename") },
                onFailure = { showStatus("Export failed: ${it.message}") }
            )
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun importJson(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            exportManager.importJson(uri).fold(
                onSuccess = { showStatus("Import successful") },
                onFailure = { showStatus("Import failed: ${it.message}") }
            )
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun clearStatus() { _state.value = _state.value.copy(statusMessage = null) }

    private fun showStatus(msg: String) { _state.value = _state.value.copy(statusMessage = msg) }
}
