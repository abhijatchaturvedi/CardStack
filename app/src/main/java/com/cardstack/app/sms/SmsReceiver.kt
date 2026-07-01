package com.cardstack.app.sms

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.cardstack.app.MainActivity
import com.cardstack.app.R
import com.cardstack.app.data.db.TransactionEntity
import com.cardstack.app.data.repository.CardRepository
import com.cardstack.app.data.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject lateinit var cardRepo: CardRepository
    @Inject lateinit var settings: SettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        if (!settings.getSmsAutoImportEnabled()) return

        val body = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            .joinToString("") { it.messageBody }
        val parsed = SmsParser.parse(body) ?: return

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val card = cardRepo.getAllCards()
                    .find { it.lastFourDigits == parsed.lastFourDigits }
                    ?: return@launch

                if (cardRepo.hasSimilarTransaction(card.id, parsed.amount, parsed.dateMillis)) return@launch

                cardRepo.saveTransaction(
                    TransactionEntity(
                        cardId = card.id,
                        amount = parsed.amount,
                        merchant = parsed.merchant,
                        category = parsed.category,
                        date = parsed.dateMillis
                    )
                )
                postNotification(context, card.nickname, parsed)
            } finally {
                pending.finish()
            }
        }
    }

    private fun postNotification(context: Context, cardName: String, tx: ParsedSmsTransaction) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) return

        val tap = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Transaction logged · $cardName")
            .setContentText("₹${"%.0f".format(tx.amount)} at ${tx.merchant}")
            .setContentIntent(tap)
            .setAutoCancel(true)
            .build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(tx.hashCode(), notif)
    }

    companion object {
        const val CHANNEL_ID = "sms_auto_import"
    }
}
