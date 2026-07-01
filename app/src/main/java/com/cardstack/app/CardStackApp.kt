package com.cardstack.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.cardstack.app.sms.SmsReceiver
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CardStackApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SmsReceiver.CHANNEL_ID,
                "SMS Auto-Import",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when a transaction is automatically logged from an SMS"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
