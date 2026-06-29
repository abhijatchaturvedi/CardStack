package com.cardstack.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardstack.app.ui.theme.AmberDue
import com.cardstack.app.ui.theme.GreenDue
import com.cardstack.app.ui.theme.RedDue
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun DuePill(dueDayOfMonth: Int) {
    val daysLeft = daysUntilDue(dueDayOfMonth)
    val (bg, label) = when {
        daysLeft < 0  -> Pair(RedDue, "Overdue")
        daysLeft < 3  -> Pair(RedDue, "${daysLeft}d left")
        daysLeft < 7  -> Pair(AmberDue, "${daysLeft}d left")
        else          -> Pair(GreenDue, "${daysLeft}d left")
    }

    Text(
        text = label,
        color = Color.Black,
        fontSize = 11.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    )
}

fun daysUntilDue(dueDayOfMonth: Int): Long {
    val today = LocalDate.now()
    var due = today.withDayOfMonth(minOf(dueDayOfMonth, today.lengthOfMonth()))
    if (!due.isAfter(today)) {
        val next = today.plusMonths(1)
        due = next.withDayOfMonth(minOf(dueDayOfMonth, next.lengthOfMonth()))
    }
    return ChronoUnit.DAYS.between(today, due)
}
