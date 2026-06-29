package com.cardstack.app.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.cardstack.app.data.db.BalanceEntity
import com.cardstack.app.data.db.CardEntity
import com.cardstack.app.data.db.TransactionEntity
import com.cardstack.app.data.repository.CardRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

data class ExportData(
    val version: Int = 1,
    val exportedAt: String,
    val cards: List<CardEntity>,
    val balances: List<BalanceEntity>,
    val transactions: List<TransactionEntity>
)

@Singleton
class ExportImportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: CardRepository
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        .withZone(ZoneId.systemDefault())

    suspend fun exportJson(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val data = ExportData(
                exportedAt = Instant.now().toString(),
                cards = repo.getAllCards(),
                balances = repo.getAllBalances(),
                transactions = repo.getAllTransactions()
            )
            val json = gson.toJson(data)
            val filename = "cardstack_backup_${System.currentTimeMillis()}.json"
            writeToDownloads(filename, "application/json", json.toByteArray())
            filename
        }
    }

    suspend fun exportCsv(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val cards = repo.getAllCards().associateBy { it.id }
            val txs = repo.getAllTransactions().sortedByDescending { it.date }
            val sb = StringBuilder("Date,Card,Merchant,Category,Amount,Notes\n")
            txs.forEach { tx ->
                val cardName = cards[tx.cardId]?.nickname ?: "Unknown"
                val date = dateFmt.format(Instant.ofEpochMilli(tx.date))
                sb.append("$date,\"$cardName\",\"${tx.merchant}\",${tx.category.name},${tx.amount},\"${tx.notes}\"\n")
            }
            val filename = "cardstack_transactions_${System.currentTimeMillis()}.csv"
            writeToDownloads(filename, "text/csv", sb.toString().toByteArray())
            filename
        }
    }

    suspend fun importJson(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val json = context.contentResolver.openInputStream(uri)!!.use { it.readBytes().toString(Charsets.UTF_8) }
            val data = gson.fromJson(json, ExportData::class.java)
            data.cards.forEach { repo.saveCard(it) }
            data.balances.forEach { repo.saveBalance(it) }
            data.transactions.forEach { repo.saveTransaction(it) }
        }
    }

    private fun writeToDownloads(filename: String, mimeType: String, bytes: ByteArray): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, filename)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)!!
        context.contentResolver.openOutputStream(uri)!!.use { it.write(bytes) }
        return uri
    }
}
