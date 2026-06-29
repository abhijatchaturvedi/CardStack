package com.cardstack.app.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardstack.app.ui.common.BarChart
import com.cardstack.app.ui.common.DonutChart
import com.cardstack.app.ui.common.DonutLegend
import com.cardstack.app.ui.theme.AmberDue
import com.cardstack.app.ui.theme.IndigoAccent
import com.cardstack.app.ui.theme.OnSurfaceSecondary
import com.cardstack.app.ui.theme.RedDue

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Analytics", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        // ── Spend this month ───────────────────────────────────────────────────
        AnalyticsCard(title = "Spend This Month") {
            Text(
                "₹${"%,.0f".format(state.totalThisMonth)}",
                style = MaterialTheme.typography.headlineLarge,
                color = IndigoAccent,
                fontWeight = FontWeight.Bold
            )
        }

        // ── Category donut ─────────────────────────────────────────────────────
        AnalyticsCard(title = "Spend by Category") {
            if (state.categorySlices.isEmpty()) {
                EmptyHint("No transactions this month")
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DonutChart(
                        slices = state.categorySlices,
                        modifier = Modifier.size(140.dp),
                        centerLabel = "₹${"%,.0f".format(state.totalThisMonth)}"
                    )
                    DonutLegend(
                        slices = state.categorySlices,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── Monthly bar chart ──────────────────────────────────────────────────
        AnalyticsCard(title = "Last 6 Months") {
            if (state.monthlyBars.all { it.value == 0f }) {
                EmptyHint("No transaction history yet")
            } else {
                BarChart(entries = state.monthlyBars, barColor = IndigoAccent)
            }
        }

        // ── Per-card utilisation ───────────────────────────────────────────────
        AnalyticsCard(title = "Card Utilisation") {
            if (state.utilisationList.isEmpty()) {
                EmptyHint("No cards added yet")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.utilisationList.forEach { cu ->
                        val pct = (cu.utilisation * 100).toInt()
                        val color = when {
                            cu.utilisation >= 0.3f -> RedDue
                            cu.utilisation >= 0.2f -> AmberDue
                            else                   -> MaterialTheme.colorScheme.primary
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(cu.card.nickname, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "$pct%  ·  ₹${"%,.0f".format(cu.outstanding)} / ₹${"%,.0f".format(cu.card.creditLimit)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceSecondary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { cu.utilisation.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = color,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun AnalyticsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = OnSurfaceSecondary)
            content()
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodySmall, color = OnSurfaceSecondary)
    }
}
