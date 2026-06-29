package com.cardstack.app.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class CardNetwork { VISA, MASTERCARD, AMEX, RUPAY }
enum class RewardCurrency { POINTS, CASHBACK, MILES }
enum class TransactionCategory {
    FOOD, TRAVEL, FUEL, SHOPPING, ENTERTAINMENT, UTILITIES, OTHER
}

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nickname: String,
    val bankName: String,
    val lastFourDigits: String,
    val network: CardNetwork,
    val creditLimit: Double,
    val billingCycleDate: Int,       // day of month
    val paymentDueDate: Int,          // day of month
    val rewardCurrency: RewardCurrency,
    val rewardRate: Double,           // percentage
    val monthlyInterestRate: Double = 3.5,
    val cardColorStart: Long = 0xFF5C6BC0,
    val cardColorEnd: Long = 0xFF283593,
    val rewardBalance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "balances",
    foreignKeys = [ForeignKey(
        entity = CardEntity::class,
        parentColumns = ["id"],
        childColumns = ["cardId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("cardId")]
)
data class BalanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardId: Long,
    val outstandingBalance: Double = 0.0,
    val minimumDue: Double = 0.0,
    val fullDue: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transactions",
    foreignKeys = [ForeignKey(
        entity = CardEntity::class,
        parentColumns = ["id"],
        childColumns = ["cardId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("cardId")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardId: Long,
    val amount: Double,
    val merchant: String,
    val category: TransactionCategory,
    val date: Long,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
