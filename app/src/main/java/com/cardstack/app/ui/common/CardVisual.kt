package com.cardstack.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardstack.app.data.db.CardEntity
import com.cardstack.app.data.db.CardNetwork

@Composable
fun CardVisual(card: CardEntity, modifier: Modifier = Modifier) {
    val colorStart = Color(card.cardColorStart)
    val colorEnd = Color(card.cardColorEnd)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f)        // standard card ratio
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(colorStart, colorEnd)))
            .padding(24.dp)
    ) {
        // Bank name top-left
        Text(
            text = card.bankName.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.85f),
            letterSpacing = 2.sp,
            modifier = Modifier.align(Alignment.TopStart)
        )

        // Network top-right
        Text(
            text = card.network.displayName(),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopEnd)
        )

        // Card number (masked) centre
        Text(
            text = "•••• •••• •••• ${card.lastFourDigits}",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            letterSpacing = 3.sp,
            modifier = Modifier.align(Alignment.Center)
        )

        // Nickname + reward info bottom
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(
                text = card.nickname,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${card.rewardRate}% ${card.rewardCurrency.displayName()}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        // Due date bottom-right
        Text(
            text = "Due: ${card.paymentDueDate.ordinalDay()}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

private fun com.cardstack.app.data.db.RewardCurrency.displayName() = when (this) {
    com.cardstack.app.data.db.RewardCurrency.POINTS -> "Points"
    com.cardstack.app.data.db.RewardCurrency.CASHBACK -> "Cashback"
    com.cardstack.app.data.db.RewardCurrency.MILES -> "Miles"
}

private fun CardNetwork.displayName() = when (this) {
    CardNetwork.VISA -> "VISA"
    CardNetwork.MASTERCARD -> "MC"
    CardNetwork.AMEX -> "AMEX"
    CardNetwork.RUPAY -> "RuPay"
}

private fun Int.ordinalDay(): String {
    val suffix = when {
        this in 11..13 -> "th"
        this % 10 == 1 -> "st"
        this % 10 == 2 -> "nd"
        this % 10 == 3 -> "rd"
        else -> "th"
    }
    return "$this$suffix"
}
