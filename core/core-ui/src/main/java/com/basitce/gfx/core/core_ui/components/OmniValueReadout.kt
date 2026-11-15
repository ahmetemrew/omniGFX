package com.basitce.gfx.core.core_ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_ui.theme.OmniRadius
import com.basitce.gfx.core.core_ui.theme.OmniTheme

/**
 * İMZA ANI 1 — Değer Okuması.
 *
 * Uygulamanın gerçek işi "bir config sayısını güvenle değiştirmek". Bu bileşen onu
 * bir ölçüm aleti okuması olarak çizer: config anahtarı monospace sönük, eski değer
 * üstü çizili, yeni değer pirinç renginde büyük okuma, altında tik rayı.
 * Başka bir uygulamada anlamı olmayan, ürüne özgü görselleştirme.
 */
@Composable
fun OmniValueReadout(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    previousValue: String? = null,
    unit: String? = null,
    /** Tik rayındaki konum, 0..1. Verilmezse ray yalnızca ölçek çizgisi olarak kalır. */
    fraction: Float? = null,
    /** Rayın sol/sağ uçlarına yazılan ölçek sınırları (ör. "0" – "120"). */
    minLabel: String? = null,
    maxLabel: String? = null,
    compact: Boolean = false
) {
    val semantic = OmniTheme.semantic
    val spacing = OmniTheme.spacing
    val motion = OmniTheme.motion
    val changed = previousValue != null && previousValue != value

    val valueColor by animateColorAsState(
        targetValue = if (changed) MaterialTheme.colorScheme.secondary
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = motion.mediumTween(),
        label = "readout_value_color"
    )
    val railPosition by animateFloatAsState(
        targetValue = fraction?.coerceIn(0f, 1f) ?: 0f,
        animationSpec = motion.mediumTween(),
        label = "readout_rail_position"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) OmniRadius.md else OmniRadius.xl),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = spacing.lg,
                vertical = if (compact) spacing.md else spacing.lg
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                Text(
                    text = label,
                    style = OmniTheme.mono.key,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (changed) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Değiştirildi",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                if (changed) {
                    Text(
                        text = previousValue.orEmpty(),
                        style = if (compact) OmniTheme.mono.value else OmniTheme.mono.readout,
                        color = semantic.readoutMuted,
                        textDecoration = TextDecoration.LineThrough,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = value,
                    style = if (compact) OmniTheme.mono.value else OmniTheme.mono.readout,
                    color = valueColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (unit != null) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = if (compact) 2.dp else 5.dp)
                    )
                }
            }

            OmniTickRail(
                fraction = if (fraction != null) railPosition else null,
                highlight = changed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 14.dp else 20.dp)
            )

            if (minLabel != null || maxLabel != null) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = minLabel.orEmpty(),
                        style = OmniTheme.mono.micro,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = maxLabel.orEmpty(),
                        style = OmniTheme.mono.micro,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Ölçek rayı: cetvel tikleri. [fraction] verilirse o konuma kadar olan tikler vurgulanır
 * ve konuma bir gösterge çubuğu çizilir. Verilmezse nötr ölçek çizgisi olarak kalır.
 */
@Composable
fun OmniTickRail(
    modifier: Modifier = Modifier,
    fraction: Float? = null,
    highlight: Boolean = false,
    tickCount: Int = 28
) {
    val semantic = OmniTheme.semantic
    val baseColor = semantic.readoutRail
    val activeColor = if (highlight) MaterialTheme.colorScheme.secondary
    else MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val baseline = h - 1.5.dp.toPx()
        val stroke = 1.dp.toPx()

        // Ölçek taban çizgisi
        drawLine(
            color = baseColor.copy(alpha = 0.45f),
            start = Offset(0f, baseline),
            end = Offset(w, baseline),
            strokeWidth = stroke
        )

        val step = w / (tickCount - 1)
        for (i in 0 until tickCount) {
            val x = i * step
            val major = i % 4 == 0
            val tickHeight = if (major) h * 0.72f else h * 0.38f
            val active = fraction != null && x <= fraction * w + 0.5f
            drawLine(
                color = if (active) activeColor else baseColor.copy(alpha = if (major) 0.75f else 0.4f),
                start = Offset(x, baseline),
                end = Offset(x, baseline - tickHeight),
                strokeWidth = if (major) stroke * 1.4f else stroke,
                cap = StrokeCap.Round
            )
        }

        // Konum göstergesi
        if (fraction != null) {
            val x = (fraction * w).coerceIn(2.dp.toPx(), w - 2.dp.toPx())
            drawRoundRect(
                color = activeColor,
                topLeft = Offset(x - 1.5.dp.toPx(), 0f),
                size = Size(3.dp.toPx(), h),
                cornerRadius = CornerRadius(1.5.dp.toPx())
            )
        }
    }
}
