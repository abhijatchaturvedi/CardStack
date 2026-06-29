package com.cardstack.app.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BarEntry(val label: String, val value: Float)

@Composable
fun BarChart(
    entries: List<BarEntry>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    if (entries.isEmpty()) return
    val maxVal = entries.maxOf { it.value }.takeIf { it > 0f } ?: 1f
    val labelColorArgb = labelColor.toArgb()

    Canvas(modifier = modifier.fillMaxWidth().height(180.dp)) {
        val barCount = entries.size
        val totalPadding = size.width * 0.15f
        val barWidth = (size.width - totalPadding) / barCount * 0.6f
        val gap = (size.width - totalPadding) / barCount
        val chartHeight = size.height - 36.dp.toPx()  // leave room for labels
        val startX = totalPadding / 2f

        entries.forEachIndexed { i, entry ->
            val barHeight = (entry.value / maxVal) * chartHeight
            val x = startX + i * gap + (gap - barWidth) / 2f
            val y = chartHeight - barHeight

            drawRoundRect(
                color = barColor.copy(alpha = 0.85f),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(6.dp.toPx())
            )

            drawLabel(entry.label, x + barWidth / 2f, size.height, labelColorArgb)
        }
    }
}

private fun DrawScope.drawLabel(text: String, cx: Float, y: Float, colorArgb: Int) {
    drawContext.canvas.nativeCanvas.drawText(
        text,
        cx,
        y - 4.dp.toPx(),
        android.graphics.Paint().apply {
            color = colorArgb
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 10.sp.toPx()
            isAntiAlias = true
        }
    )
}
