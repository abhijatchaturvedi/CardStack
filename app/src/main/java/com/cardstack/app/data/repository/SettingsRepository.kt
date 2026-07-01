package com.cardstack.app.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("cardstack_prefs", Context.MODE_PRIVATE)

    fun getDefaultInterestRate(): Double = prefs.getFloat("default_interest_rate", 3.5f).toDouble()
    fun setDefaultInterestRate(rate: Double) { prefs.edit().putFloat("default_interest_rate", rate.toFloat()).apply() }

    fun getBiometricEnabled(): Boolean = prefs.getBoolean("biometric_enabled", true)
    fun setBiometricEnabled(v: Boolean) { prefs.edit().putBoolean("biometric_enabled", v).apply() }

    fun getLockTimeoutMinutes(): Int = prefs.getInt("lock_timeout_minutes", 2)
    fun setLockTimeoutMinutes(v: Int) { prefs.edit().putInt("lock_timeout_minutes", v).apply() }

    fun getSmsAutoImportEnabled(): Boolean = prefs.getBoolean("sms_auto_import", false)
    fun setSmsAutoImportEnabled(v: Boolean) { prefs.edit().putBoolean("sms_auto_import", v).apply() }

    // Theme — exposed as a StateFlow so MainActivity can react to changes without restart
    private val _themeChoice = MutableStateFlow(
        prefs.getString("theme_choice", "SYSTEM") ?: "SYSTEM"
    )
    val themeChoiceFlow: StateFlow<String> = _themeChoice.asStateFlow()

    fun setThemeChoice(name: String) {
        prefs.edit().putString("theme_choice", name).apply()
        _themeChoice.value = name
    }
}
