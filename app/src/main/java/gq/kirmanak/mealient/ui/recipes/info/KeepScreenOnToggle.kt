package gq.kirmanak.mealient.ui.recipes.info

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import gq.kirmanak.mealient.R

/**
 * A chip that turns [KeepScreenOn] on and off, labelled so it is clear what it does.
 */
@Composable
internal fun KeepScreenOnToggle(
    keepScreenOn: Boolean,
    onKeepScreenOnChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        modifier = modifier,
        selected = keepScreenOn,
        onClick = { onKeepScreenOnChange(!keepScreenOn) },
        label = {
            Text(text = stringResource(id = R.string.recipe_info_keep_screen_on))
        },
        leadingIcon = if (keepScreenOn) {
            {
                Icon(
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                    imageVector = Icons.Filled.Done,
                    contentDescription = null,
                )
            }
        } else {
            null
        },
    )
}
