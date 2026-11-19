package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ManropeFontFamily
import com.example.ui.theme.SberGreen

/**
 * Crisp Sber-style incoming transfer overlay card matching the iconic meme notification.
 */
@Composable
fun IncomingTransferCard(
    subtitle: String,
    reason: String,
    amountText: String,
    modifier: Modifier = Modifier,
    showBadge: Boolean = true,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val cardShape = RoundedCornerShape(if (compact) 14.dp else 18.dp)
    val horizontalPad = if (compact) 10.dp else 14.dp
    val verticalPad = if (compact) 8.dp else 11.dp

    Surface(
        modifier = modifier
            .shadow(
                elevation = if (compact) 6.dp else 12.dp,
                shape = cardShape,
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .clip(cardShape)
            .border(1.dp, Color(0xFFE5EAE6), cardShape)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        color = Color.White,
        shape = cardShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = horizontalPad, vertical = verticalPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            if (showBadge) {
                BankTransferBadge(
                    size = if (compact) 34.dp else 44.dp
                )
                Spacer(modifier = Modifier.width(if (compact) 9.dp else 12.dp))
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(if (compact) 1.dp else 2.dp)
            ) {
                Text(
                    text = subtitle.ifBlank { "Входящий перевод" },
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = if (compact) 10.sp else 12.sp,
                    color = Color(0xFF8C9490),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = reason.ifBlank { "На лечение" },
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 13.sp else 16.sp,
                    color = Color(0xFF141715),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = amountText.ifBlank { "+ 15₽" },
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 16.sp else 20.sp,
                    color = SberGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Custom vector-drawn circular green bank transfer icon (hand + ruble coin).
 */
@Composable
fun BankTransferBadge(
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    val cd = stringResource(R.string.bank_badge_cd)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFF1DB954))
            .semantics { contentDescription = cd },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.66f)) {
            val w = this.size.width
            val h = this.size.height
            val stroke = Stroke(width = w * 0.085f, cap = StrokeCap.Round, join = StrokeJoin.Round)

            // Coin circle in upper right
            val coinCenter = Offset(w * 0.56f, h * 0.30f)
            val coinRadius = w * 0.22f
            drawCircle(
                color = Color.White,
                radius = coinRadius,
                center = coinCenter,
                style = stroke
            )

            // Ruble vertical + curve inside coin
            drawLine(
                color = Color.White,
                start = Offset(coinCenter.x - coinRadius * 0.22f, coinCenter.y - coinRadius * 0.48f),
                end = Offset(coinCenter.x - coinRadius * 0.22f, coinCenter.y + coinRadius * 0.50f),
                strokeWidth = w * 0.07f,
                cap = StrokeCap.Round
            )
            drawArc(
                color = Color.White,
                startAngle = -90f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(coinCenter.x - coinRadius * 0.22f, coinCenter.y - coinRadius * 0.45f),
                size = Size(coinRadius * 0.7f, coinRadius * 0.52f),
                style = Stroke(width = w * 0.07f, cap = StrokeCap.Round)
            )
            drawLine(
                color = Color.White,
                start = Offset(coinCenter.x - coinRadius * 0.42f, coinCenter.y + coinRadius * 0.22f),
                end = Offset(coinCenter.x + coinRadius * 0.18f, coinCenter.y + coinRadius * 0.22f),
                strokeWidth = w * 0.07f,
                cap = StrokeCap.Round
            )

            // Palm / hand holding coin beneath
            val handPath = Path().apply {
                moveTo(w * 0.12f, h * 0.65f)
                lineTo(w * 0.38f, h * 0.58f)
                lineTo(w * 0.78f, h * 0.66f)
                quadraticTo(w * 0.88f, h * 0.70f, w * 0.82f, h * 0.79f)
                lineTo(w * 0.46f, h * 0.88f)
                lineTo(w * 0.12f, h * 0.80f)
                close()
            }
            drawPath(
                path = handPath,
                color = Color.White,
                style = stroke
            )
        }
    }
}
