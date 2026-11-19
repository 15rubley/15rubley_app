package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClientReview
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlin.math.roundToInt

/**
 * Cursed Courier Rating & Client Reviews card.
 * Keeps the courier rating perpetually low with humorous micro-penalties
 * and displays sarcastic 1-2 star client reviews.
 */
@Composable
fun CourierRatingCard(
    rating: Double,
    lastDelta: Double,
    penaltyReason: String,
    reviews: List<ClientReview>,
    attitudeLabel: String = "Терпят зелёный рюкзак",
    breakdownCount: Int = 0,
    onAttemptBoostRating: () -> Unit,
    onTriggerComplaintCall: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isCritical = rating < 2.2
    val accentColor = if (isCritical) CoralAlert else AmberAlert
    val statusLabel = when {
        rating <= 1.5 -> "Легенда жалобной книги"
        rating <= 2.3 -> "На грани увольнения"
        rating <= 3.0 -> "Подозрительно старательный"
        else -> "Срочная калибровка вниз!"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("courier_rating_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Рейтинг курьера",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor
                    )
                }

                Surface(
                    color = accentColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = String.format("%.1f", rating),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = accentColor,
                            modifier = Modifier.testTag("courier_rating_value")
                        )
                        Text(
                            text = if (lastDelta < 0) {
                                String.format("(%.1f)", lastDelta)
                            } else {
                                String.format("(+%.1f)", lastDelta)
                            },
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = accentColor.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Star Strip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val filledCount = rating.roundToInt().coerceIn(1, 5)
                repeat(5) { idx ->
                    Icon(
                        imageVector = if (idx < filledCount) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                        contentDescription = null,
                        tint = if (idx < filledCount) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Выше 3.6★ алгоритм не пропускает",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Latest Penalty Reason & Attitude Pill
            Surface(
                color = CoralAlert.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = CoralAlert,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = penaltyReason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = if (breakdownCount > 0) {
                            "Отношение людей: $attitudeLabel (поломок: $breakdownCount)"
                        } else {
                            "Отношение людей: $attitudeLabel"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (breakdownCount > 0) CoralAlert else AmberAlert
                    )
                }
            }

            OutlinedButton(
                onClick = onAttemptBoostRating,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("try_boost_rating_button"),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.TrendingDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Попробовать поднять рейтинг",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            if (onTriggerComplaintCall != null) {
                FilledTonalButton(
                    onClick = onTriggerComplaintCall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shift_test_complaint_call_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = CoralAlert.copy(alpha = 0.14f),
                        contentColor = CoralAlert
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Call,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Принять звонок с жалобой клиента",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Client Reviews Section
            if (reviews.isNotEmpty()) {
                Text(
                    text = "Отзывы клиентов (${reviews.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    reviews.take(4).forEach { review ->
                        ClientReviewItem(review = review)
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientReviewItem(review: ClientReview) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${review.author} • ${review.timestampLabel}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(5) { idx ->
                        Icon(
                            imageVector = if (idx < review.stars) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = null,
                            tint = if (idx < review.stars) AmberAlert else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = review.orderTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
