package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.MemeStudioState
import com.example.data.OverlayPosition
import com.example.ui.components.IncomingTransferCard
import com.example.util.MemeRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val REASON_PRESETS = listOf(
    "На лечение",
    "На новую цепь",
    "За улыбку",
    "На доширак",
    "Держись, брат"
)

private val AMOUNT_PRESETS = listOf(
    "+ 15₽",
    "+ 1₽",
    "+ 50₽",
    "+ 500₽"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemeStudioScreen(
    studioState: MemeStudioState,
    onSubtitleChange: (String) -> Unit,
    onReasonChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onPositionChange: (OverlayPosition) -> Unit,
    onBadgeToggle: (Boolean) -> Unit,
    onScaleChange: (Float) -> Unit,
    onPhotoSelected: (String?) -> Unit,
    onResetPreset: () -> Unit,
    onTestBanner: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPhotoSelected(uri.toString())
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Конструктор мем-чеков",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Собери свой «Входящий перевод» поверх фото",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = onResetPreset,
                    modifier = Modifier.testTag("studio_reset_button"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RestartAlt,
                        contentDescription = stringResource(R.string.reset_preset),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Live Card Preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.85f)
                        .clip(RoundedCornerShape(22.dp))
                ) {
                    if (studioState.selectedPhotoUri != null) {
                        AsyncImage(
                            model = studioState.selectedPhotoUri,
                            contentDescription = stringResource(R.string.hero_image_cd),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.courier_meme_hero),
                            contentDescription = stringResource(R.string.hero_image_cd),
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.TopCenter,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    val alignment = when (studioState.position) {
                        OverlayPosition.TOP_LEFT -> Alignment.TopStart
                        OverlayPosition.TOP_CENTER -> Alignment.TopCenter
                        OverlayPosition.BOTTOM_LEFT -> Alignment.BottomStart
                    }

                    Box(
                        modifier = Modifier
                            .align(alignment)
                            .padding(10.dp)
                            .graphicsLayer(
                                scaleX = studioState.badgeScale,
                                scaleY = studioState.badgeScale
                            )
                    ) {
                        IncomingTransferCard(
                            subtitle = studioState.subtitle,
                            reason = studioState.reason,
                            amountText = studioState.amountText,
                            showBadge = studioState.showBankBadge,
                            compact = true
                        )
                    }
                }
            }

            // Photo Pick / Reset Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pick_photo_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.pick_photo))
                }

                if (studioState.selectedPhotoUri != null) {
                    OutlinedButton(
                        onClick = { onPhotoSelected(null) },
                        modifier = Modifier.testTag("reset_photo_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(stringResource(R.string.reset_photo))
                    }
                }
            }

            // Text Inputs
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = studioState.subtitle,
                        onValueChange = onSubtitleChange,
                        label = { Text("Верхняя строка") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("studio_subtitle_input")
                    )

                    OutlinedTextField(
                        value = studioState.reason,
                        onValueChange = onReasonChange,
                        label = { Text("Назначение перевода") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("studio_reason_input")
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        REASON_PRESETS.forEach { preset ->
                            FilterChip(
                                selected = studioState.reason == preset,
                                onClick = { onReasonChange(preset) },
                                label = { Text(preset) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = studioState.amountText,
                        onValueChange = onAmountChange,
                        label = { Text("Сумма") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("studio_amount_input")
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AMOUNT_PRESETS.forEach { preset ->
                            FilterChip(
                                selected = studioState.amountText == preset,
                                onClick = { onAmountChange(preset) },
                                label = { Text(preset) }
                            )
                        }
                    }
                }
            }

            // Layout & Badge Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Позиция плашки",
                        style = MaterialTheme.typography.titleSmall
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OverlayPosition.entries.forEach { pos ->
                            FilterChip(
                                selected = studioState.position == pos,
                                onClick = { onPositionChange(pos) },
                                label = { Text(pos.label) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Зелёная иконка банка",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = studioState.showBankBadge,
                            onCheckedChange = onBadgeToggle,
                            modifier = Modifier.testTag("studio_badge_switch")
                        )
                    }

                    Column {
                        Text(
                            text = "Размер плашки: ${(studioState.badgeScale * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = studioState.badgeScale,
                            onValueChange = onScaleChange,
                            valueRange = 0.8f..1.25f
                        )
                    }
                }
            }

            // Bottom Actions: Test Banner & Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onTestBanner(
                            studioState.subtitle,
                            studioState.reason,
                            studioState.amountText
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("studio_test_banner_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Уведомление")
                }

                Button(
                    onClick = {
                        scope.launch {
                            val res = withContext(Dispatchers.IO) {
                                MemeRenderer.shareMemeCard(context, studioState)
                            }
                            res.onFailure {
                                Toast.makeText(
                                    context,
                                    "Не удалось подготовить картинку",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("studio_share_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.share_meme))
                }
            }

            Spacer(modifier = Modifier.height(76.dp))
        }
    }
}
