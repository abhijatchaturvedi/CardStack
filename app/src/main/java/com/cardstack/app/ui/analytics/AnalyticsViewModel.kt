package com.cardstack.app.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardstack.app.data.db.CardEntity
import com.cardstack.app.data.db.TransactionCategory
import com.cardstack.app.data.repository.CardRepository
import com.cardstack.app.ui.common.BarEntry
import com.cardstack.app.ui.common.DonutSlice
import com.cardstack.app.ui.common.color
import com.cardstack.app.ui.common.displayName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class CardUtilisation(
    val card: CardEntity,
    val outstanding: Double,
    val utilisation: Float
)

data class AnalyticsUiState(
    val categorySlices: List<DonutSlice> = emptyList(),
    val monthlyBars: List<BarEntry> = emptyList(),
    val utilisationList: List<CardUtilisation> = emptyList(),
    val totalThisMonth: Double = 0.0
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repo: CardRepository
) : ViewModel() {

    private val zone = ZoneId.systemDefault()
    private val monthFmt = DateTimeFormatter.ofPattern("MMM")

    val uiState: StateFlow<AnalyticsUiState> = combine(
        repo.observeAllTransactions(),
        repo.observeCards()
    ) { txs, cards ->
        val now = LocalDate.now(zone)

        // Current month transactions
        val monthStart = now.withDayOfMonth(1).toEpochMillis()
        val monthEnd   = now.plusMonths(1).withDayOfMonth(1).toEpochMillis()
        val thisMonth  = txs.filter { it.date in monthStart until monthEnd }

        // Donut: spend by category this month
        val byCategory = thisMonth.groupBy { it.category }
        val slices = TransactionCategory.entries
            .mapNotNull { cat ->
                val total = byCategory[cat]?.sumOf { it.amount }?.toFloat() ?: 0f
                if (total > 0f) DonutSlice(cat.displayName(), total, cat.color()) else null
            }
            .sortedByDescending { it.value }

        // Bar: last 6 months total spend
        val bars = (5 downTo 0).map { offset ->
            val month = now.minusMonths(offset.toLong())
            val start = month.withDayOfMonth(1).toEpochMillis()
            val end   = month.plusMonths(1).withDayOfMonth(1).toEpochMillis()
            val total = txs.filter { it.date in start until end }.sumOf { it.amount }.toFloat()
            BarEntry(month.format(monthFmt), total)
        }

        // Per-card utilisation
        val utilisations = cards.map { card ->
            val outstanding = repo.getBalance(card.id)?.outstandingBalance ?: 0.0
            val util = if (card.creditLimit > 0) (outstanding / card.creditLimit).toFloat() else 0f
            CardUtilisation(card, outstanding, util)
        }

        AnalyticsUiState(
            categorySlices = slices,
            monthlyBars = bars,
            utilisationList = utilisations,
            totalThisMonth = thisMonth.sumOf { it.amount }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyticsUiState())

    private fun LocalDate.toEpochMillis(): Long =
        atStartOfDay(zone).toInstant().toEpochMilli()
}
