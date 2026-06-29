package com.cardstack.app.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
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
}
