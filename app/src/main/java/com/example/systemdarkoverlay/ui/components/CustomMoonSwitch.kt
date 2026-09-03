package com.example.systemdarkoverlay.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.systemdarkoverlay.R

@Composable
fun CustomMoonSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val horizontalBias by animateFloatAsState(
        targetValue = if (checked) 1f else -1f,
        animationSpec = tween(durationMillis = 400),
        label = "thumbBias"
    )
    val thumbAlignment = BiasAlignment(horizontalBias, 0f)

    val maskAlpha by animateFloatAsState(
        targetValue = if (checked) 0f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "maskAlpha"
    )

    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_track_bg),
            contentDescription = "Track Background",
            contentScale = ContentScale.Fit
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(end = 16.dp), // Increased padding to bring it inward
            contentAlignment = Alignment.CenterEnd
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_mask),
                contentDescription = "Mask Icon",
                modifier = Modifier
                    .alpha(maskAlpha)
                    .fillMaxHeight(0.5f), // Constrain height to shrink the image
                contentScale = ContentScale.Fit
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(8.dp),
            contentAlignment = thumbAlignment
        ) {
            Crossfade(
                targetState = checked,
                animationSpec = tween(durationMillis = 300),
                label = "thumbCrossfade"
            ) { isChecked ->
                if (isChecked) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_moon_masked),
                        contentDescription = "Moon Masked",
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_moon_open),
                        contentDescription = "Moon Open",
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
