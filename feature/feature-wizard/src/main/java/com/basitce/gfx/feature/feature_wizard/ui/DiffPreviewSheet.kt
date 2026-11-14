package com.basitce.gfx.feature.feature_wizard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_engine.diff.DiffType
import com.basitce.gfx.core.core_engine.diff.LineDiffEngine
import com.basitce.gfx.core.core_ui.components.OmniPrimaryButton
import com.basitce.gfx.core.core_ui.components.OmniStatusChip
import com.basitce.gfx.core.core_ui.components.OmniTone
import com.basitce.gfx.core.core_ui.theme.OmniRadius
import com.basitce.gfx.core.core_ui.theme.OmniTheme

/**
 * Fark önizlemesi: config metni monospace, eklenen/silinen satırlar anlamsal
 * zeminle ayrışır. Yazmadan önce "ne değişecek?" sorusunun cevabı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiffPreviewSheet(
    originalContent: String,
    modifiedContent: String,
    onDismiss: () -> Unit
) {
    val diffLines = remember(originalContent, modifiedContent) {
        LineDiffEngine.diff(originalContent, modifiedContent)
    }
    val addedCount = diffLines.count { it.type == DiffType.ADD }
    val removedCount = diffLines.count { it.type == DiffType.REMOVE }
    val spacing = OmniTheme.spacing
    val semantic = OmniTheme.semantic

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = OmniRadius.lg, topEnd = OmniRadius.lg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.screen)
                .heightIn(max = 520.dp)
        ) {
            Text(
                text = "Değişiklik önizlemesi",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Dosyaya yazılmadan önce son halini gör.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = spacing.xxs)
            )

            Spacer(modifier = Modifier.height(spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OmniStatusChip(text = "$addedCount satır eklendi", tone = OmniTone.Success)
                OmniStatusChip(text = "$removedCount satır silindi", tone = OmniTone.Error)
            }

            Spacer(modifier = Modifier.height(spacing.md))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .background(semantic.codeSurface, RoundedCornerShape(OmniRadius.xs))
                    .padding(vertical = spacing.sm)
            ) {
                itemsIndexed(diffLines) { index, line ->
                    val bgColor = when (line.type) {
                        DiffType.ADD -> semantic.diffAdded
                        DiffType.REMOVE -> semantic.diffRemoved
                        DiffType.CONTEXT -> Color.Transparent
                    }
                    val textColor = when (line.type) {
                        DiffType.ADD -> semantic.onDiffAdded
                        DiffType.REMOVE -> semantic.onDiffRemoved
                        DiffType.CONTEXT -> semantic.onCodeSurface.copy(alpha = 0.65f)
                    }
                    val prefix = when (line.type) {
                        DiffType.ADD -> "+"
                        DiffType.REMOVE -> "-"
                        DiffType.CONTEXT -> " "
                    }

                    Text(
                        text = "$prefix ${line.text}",
                        style = OmniTheme.mono.key,
                        color = textColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bgColor)
                            .padding(horizontal = spacing.sm, vertical = spacing.xxs)
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.lg))

            OmniPrimaryButton(text = "Kapat", onClick = onDismiss)

            Spacer(modifier = Modifier.height(spacing.lg))
        }
    }
}
