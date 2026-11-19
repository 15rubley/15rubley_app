package com.example.ui.components

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
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * App Settings sheet allowing the user to toggle the persistent 5-speed gearbox widget,
 * dark theme, and haptic feedback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    showGearboxWidget: Boolean,
    isDarkTheme: Boolean,
    hapticsEnabled: Boolean,
    soundsEnabled: Boolean,
    garageMusicEnabled: Boolean,
    onToggleGearboxWidget: (Boolean) -> Unit,
    onToggleDarkTheme: () -> Unit,
    onToggleHaptics: () -> Unit,
    onToggleSounds: (Boolean) -> Unit,
    onToggleGarageMusic: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .testTag("settings_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Настройки курьера",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            SettingToggleRow(
                icon = Icons.Rounded.Speed,
                title = "Виджет рычага КПП (5 ступеней)",
                subtitle = "Плавающий переключатель передач поверх экранов",
                checked = showGearboxWidget,
                onCheckedChange = onToggleGearboxWidget,
                tag = "settings_gearbox_switch"
            )

            SettingToggleRow(
                icon = Icons.Rounded.DarkMode,
                title = "Тёмная тема",
                subtitle = "Ночной режим для вечерних смен",
                checked = isDarkTheme,
                onCheckedChange = { onToggleDarkTheme() },
                tag = "settings_theme_switch"
            )

            SettingToggleRow(
                icon = Icons.Rounded.VolumeUp,
                title = "Звуки и озвучка КПП (V1 / ROTATE)",
                subtitle = "Скрип тормозов, заказы, ЛС и Боинг-голос",
                checked = soundsEnabled,
                onCheckedChange = onToggleSounds,
                tag = "settings_sounds_switch"
            )

            SettingToggleRow(
                icon = Icons.Rounded.MusicNote,
                title = "Музыка в гараже (Get Low)",
                subtitle = "Автозапуск 101 BPM кача на вкладке «Велик»",
                checked = garageMusicEnabled,
                onCheckedChange = { onToggleGarageMusic() },
                tag = "settings_garage_music_switch"
            )

            SettingToggleRow(
                icon = Icons.Rounded.Vibration,
                title = "Виброотклик",
                subtitle = "Тактильный отклик на переключение КПП и переводы",
                checked = hapticsEnabled,
                onCheckedChange = { onToggleHaptics() },
                tag = "settings_haptics_switch"
            )

            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(tag)
            )
        }
    }
}
