package com.cardstack.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards ORDER BY paymentDueDate ASC")
    fun observeAll(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getById(id: Long): CardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(card: CardEntity): Long

    @Delete
    suspend fun delete(card: CardEntity)

    @Query("SELECT * FROM balances WHERE cardId = :cardId LIMIT 1")
    fun observeBalance(cardId: Long): Flow<BalanceEntity?>

    @Query("SELECT * FROM balances WHERE cardId = :cardId LIMIT 1")
    suspend fun getBalance(cardId: Long): BalanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBalance(balance: BalanceEntity)

    @Query("SELECT * FROM transactions WHERE cardId = :cardId ORDER BY date DESC")
    fun observeTransactionsByCard(cardId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun observeAllTransactions(): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE date >= :from AND date <= :to
        ORDER BY date DESC
    """)
    fun observeTransactionsInRange(from: Long, to: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTransaction(tx: TransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(tx: TransactionEntity)

    @Query("SELECT * FROM cards")
    suspend fun getAllCards(): List<CardEntity>

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM balances")
    suspend fun getAllBalances(): List<BalanceEntity>

    @Query("""
        SELECT COUNT(*) FROM transactions
        WHERE cardId = :cardId AND amount = :amount
        AND date / 86400000 = :dayBucket
    """)
    suspend fun countSimilarTransactions(cardId: Long, amount: Double, dayBucket: Long): Int
}
