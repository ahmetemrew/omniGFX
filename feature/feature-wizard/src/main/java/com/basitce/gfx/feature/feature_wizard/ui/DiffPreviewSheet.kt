package com.basitce.gfx.feature.feature_wizard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_engine.diff.DiffType
import com.basitce.gfx.core.core_engine.diff.LineDiffEngine
import com.basitce.gfx.core.core_ui.theme.OmniBackground
import com.basitce.gfx.core.core_ui.theme.OmniError
import com.basitce.gfx.core.core_ui.theme.OmniOnPrimary
import com.basitce.gfx.core.core_ui.theme.OmniOnSurface
import com.basitce.gfx.core.core_ui.theme.OmniOnSurfaceVariant
import com.basitce.gfx.core.core_ui.theme.OmniPrimary
import com.basitce.gfx.core.core_ui.theme.OmniSuccess
import com.basitce.gfx.core.core_ui.theme.OmniSurface

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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = OmniSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .heightIn(max = 500.dp)
        ) {
            Text(
                text = "Canlı Diff Önizleme",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = OmniOnSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Orijinal ile düzenlenen içerik farkları",
                style = MaterialTheme.typography.bodySmall,
                color = OmniOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DiffStatBadge(
                    label = "+$addedCount satır",
                    color = OmniSuccess
                )
                DiffStatBadge(
                    label = "-$removedCount satır",
                    color = OmniError
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .background(OmniBackground, RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                itemsIndexed(diffLines) { index, line ->
                    val bgColor = when (line.type) {
                        DiffType.ADD -> OmniSuccess.copy(alpha = 0.12f)
                        DiffType.REMOVE -> OmniError.copy(alpha = 0.12f)
                        DiffType.CONTEXT -> Color.Transparent
                    }
                    val textColor = when (line.type) {
                        DiffType.ADD -> OmniSuccess
                        DiffType.REMOVE -> OmniError
                        DiffType.CONTEXT -> OmniOnSurfaceVariant.copy(alpha = 0.6f)
                    }
                    val prefix = when (line.type) {
                        DiffType.ADD -> "+"
                        DiffType.REMOVE -> "-"
                        DiffType.CONTEXT -> " "
                    }

                    Text(
                        text = "$prefix ${line.text}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = textColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bgColor)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OmniPrimary)
            ) {
                Text("Kapat", color = OmniOnPrimary)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DiffStatBadge(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(end = 6.dp)
                .background(color.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
        }
    }
}
