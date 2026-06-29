package com.cardstack.app.data.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter fun fromCardNetwork(v: CardNetwork): String = v.name
    @TypeConverter fun toCardNetwork(v: String): CardNetwork = CardNetwork.valueOf(v)

    @TypeConverter fun fromRewardCurrency(v: RewardCurrency): String = v.name
    @TypeConverter fun toRewardCurrency(v: String): RewardCurrency = RewardCurrency.valueOf(v)

    @TypeConverter fun fromCategory(v: TransactionCategory): String = v.name
    @TypeConverter fun toCategory(v: String): TransactionCategory = TransactionCategory.valueOf(v)
}
