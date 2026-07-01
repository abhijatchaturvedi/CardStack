package com.cardstack.app.data.repository

import com.cardstack.app.data.db.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepository @Inject constructor(private val dao: CardDao) {

    fun observeCards(): Flow<List<CardEntity>> = dao.observeAll()
    fun observeBalance(cardId: Long): Flow<BalanceEntity?> = dao.observeBalance(cardId)
    fun observeTransactionsByCard(cardId: Long): Flow<List<TransactionEntity>> =
        dao.observeTransactionsByCard(cardId)
    fun observeAllTransactions(): Flow<List<TransactionEntity>> = dao.observeAllTransactions()
    fun observeTransactionsInRange(from: Long, to: Long): Flow<List<TransactionEntity>> =
        dao.observeTransactionsInRange(from, to)

    suspend fun getCard(id: Long): CardEntity? = dao.getById(id)
    suspend fun saveCard(card: CardEntity): Long = dao.upsert(card)
    suspend fun deleteCard(card: CardEntity) = dao.delete(card)

    suspend fun saveBalance(balance: BalanceEntity) = dao.upsertBalance(balance)
    suspend fun getBalance(cardId: Long): BalanceEntity? = dao.getBalance(cardId)

    suspend fun saveTransaction(tx: TransactionEntity): Long = dao.upsertTransaction(tx)
    suspend fun deleteTransaction(tx: TransactionEntity) = dao.deleteTransaction(tx)

    suspend fun getAllCards(): List<CardEntity> = dao.getAllCards()
    suspend fun getAllTransactions(): List<TransactionEntity> = dao.getAllTransactions()
    suspend fun getAllBalances(): List<BalanceEntity> = dao.getAllBalances()

    suspend fun hasSimilarTransaction(cardId: Long, amount: Double, dateMillis: Long): Boolean =
        dao.countSimilarTransactions(cardId, amount, dateMillis / 86_400_000L) > 0
}
