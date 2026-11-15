package com.basitce.gfx.core.core_ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_ui.theme.OmniRadius
import com.basitce.gfx.core.core_ui.theme.OmniTheme

/**
 * Birincil eylem: stadium (fiziksel alet tuşu), basılınca 12dp'ye morph eder.
 * Şekil kontrastı bilinçli — veri öğeleri 4dp, birincil eylem tam yuvarlak.
 * Azaltılmış harekette morph anında olur.
 */
@Composable
fun OmniPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    fillWidth: Boolean = true,
    loading: Boolean = false,
    /** Kart içi ikincil konumdaki birincil eylem (ör. profil "Uygula"): 36dp gövde.
     *  Dokunma hedefi kartın tamamı tarafından ≥48dp olarak karşılanır. */
    compact: Boolean = false
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val motion = OmniTheme.motion
    val radius by animateDpAsState(
        targetValue = if (pressed) OmniRadius.md else OmniRadius.full,
        animationSpec = motion.snappySpring(),
        label = "primary_button_shape"
    )

    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        interactionSource = interaction,
        shape = RoundedCornerShape(radius),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = if (compact) 8.dp else 14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            // Pasif birincil de ikincil gibi %38 metin taşır (tur 3): gri zemin +
            // soluk metin ikili olarak "şu an kullanılamaz" okunur; ikincil
            // butonun pasif diliyle tutarlı (tur 2 P1-8).
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        ),
        modifier = modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = if (compact) 36.dp else OmniTheme.spacing.touchTarget)
    ) {
        if (loading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OmniTheme.spacing.sm)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        } else {
            ButtonContent(text = text, icon = icon)
        }
    }
}

/** İkincil eylem: ince kenarlık, aynı stadium dili ama dolgusuz. */
@Composable
fun OmniSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    fillWidth: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(OmniRadius.full),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        // Pasifken de çerçeve görünür kalır (outlineVariant); metin %38'e iner.
        // Boş "İçe aktar" aktif gibi parlamaz (tur 2 P1-8).
        border = if (enabled) {
            ButtonDefaults.outlinedButtonBorder(enabled)
        } else {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            )
        },
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        ),
        modifier = modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = OmniTheme.spacing.touchTarget)
    ) {
        ButtonContent(text = text, icon = icon)
    }
}

/** Üçüncül eylem: metin butonu, liste içi ve dialog aksiyonları için. */
@Composable
fun OmniTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(OmniRadius.sm),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier.heightIn(min = OmniTheme.spacing.touchTarget)
    ) {
        ButtonContent(text = text, icon = icon)
    }
}

/** Geri alınamaz / tehlikeli eylem (dosyaya yazma, yedeği silme). */
@Composable
fun OmniDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(OmniRadius.full),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = OmniTheme.spacing.touchTarget)
    ) {
        ButtonContent(text = text, icon = icon)
    }
}

@Composable
private fun ButtonContent(text: String, icon: ImageVector?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OmniTheme.spacing.sm)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
