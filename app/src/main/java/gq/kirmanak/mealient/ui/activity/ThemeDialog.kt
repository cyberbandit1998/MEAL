package gq.kirmanak.mealient.ui.activity

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import com.google.android.material.color.DynamicColors
import gq.kirmanak.mealient.R
import gq.kirmanak.mealient.ui.Dimens
import gq.kirmanak.mealient.ui.ThemeMode
import gq.kirmanak.mealient.ui.ThemeSettings

@Composable
internal fun ThemeDialog(
    settings: ThemeSettings,
    onEvent: (AppEvent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onEvent(AppEvent.DismissThemeDialog) },
        confirmButton = {
            TextButton(
                onClick = { onEvent(AppEvent.DismissThemeDialog) },
            ) {
                Text(text = stringResource(id = R.string.theme_dialog_close))
            }
        },
        title = {
            Text(text = stringResource(id = R.string.theme_dialog_title))
        },
        text = {
            Column {
                Column(
                    modifier = Modifier.selectableGroup(),
                ) {
                    ThemeMode.entries.forEach { mode ->
                        ThemeModeRow(
                            nameRes = mode.nameRes,
                            selected = settings.mode == mode,
                            onClick = { onEvent(AppEvent.SetThemeMode(mode)) },
                        )
                    }
                }

                // Dynamic color needs Android 12, there is nothing to choose from before that
                if (DynamicColors.isDynamicColorAvailable()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = Dimens.Small),
                    )

                    DynamicColorRow(
                        checked = settings.useDynamicColor,
                        onCheckedChange = { onEvent(AppEvent.SetUseDynamicColor(it)) },
                    )
                }
            }
        },
    )
}

@Composable
private fun ThemeModeRow(
    @StringRes nameRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(vertical = Dimens.Small),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )

        Text(
            text = stringResource(id = nameRes),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun DynamicColorRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch,
            )
            .padding(vertical = Dimens.Small),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(id = R.string.theme_dialog_dynamic_color),
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = stringResource(id = R.string.theme_dialog_dynamic_color_summary),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = null,
        )
    }
}

@get:StringRes
private val ThemeMode.nameRes: Int
    get() = when (this) {
        ThemeMode.System -> R.string.theme_dialog_mode_system
        ThemeMode.Light -> R.string.theme_dialog_mode_light
        ThemeMode.Dark -> R.string.theme_dialog_mode_dark
    }
