package com.cardstack.app.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.cardstack.app.data.db.TransactionCategory

fun TransactionCategory.displayName() = when (this) {
    TransactionCategory.FOOD          -> "Food"
    TransactionCategory.TRAVEL        -> "Travel"
    TransactionCategory.FUEL          -> "Fuel"
    TransactionCategory.SHOPPING      -> "Shopping"
    TransactionCategory.ENTERTAINMENT -> "Entertainment"
    TransactionCategory.UTILITIES     -> "Utilities"
    TransactionCategory.OTHER         -> "Other"
}

fun TransactionCategory.color() = when (this) {
    TransactionCategory.FOOD          -> Color(0xFFFF7043)
    TransactionCategory.TRAVEL        -> Color(0xFF42A5F5)
    TransactionCategory.FUEL          -> Color(0xFFFFCA28)
    TransactionCategory.SHOPPING      -> Color(0xFFAB47BC)
    TransactionCategory.ENTERTAINMENT -> Color(0xFFEC407A)
    TransactionCategory.UTILITIES     -> Color(0xFF66BB6A)
    TransactionCategory.OTHER         -> Color(0xFF78909C)
}

fun TransactionCategory.icon(): ImageVector = when (this) {
    TransactionCategory.FOOD          -> Icons.Outlined.Restaurant
    TransactionCategory.TRAVEL        -> Icons.Outlined.Flight
    TransactionCategory.FUEL          -> Icons.Outlined.LocalGasStation
    TransactionCategory.SHOPPING      -> Icons.Outlined.ShoppingBag
    TransactionCategory.ENTERTAINMENT -> Icons.Outlined.Movie
    TransactionCategory.UTILITIES     -> Icons.Outlined.ElectricBolt
    TransactionCategory.OTHER         -> Icons.Outlined.MoreHoriz
}
