package com.example.systemdarkoverlay.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.systemdarkoverlay.R
import kotlin.math.roundToInt

/**
 * A FractionalRectangleShape used to clip the filled track perfectly
 * from the start (0%) up to the thumb's current position.
 */
class FractionalRectangleShape(private val endFraction: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        return Outline.Rectangle(
            Rect(
                left = 0f,
                top = 0f,
                right = size.width * endFraction,
                bottom = size.height
            )
        )
    }
}

/**
 * Custom Moon Slider
 *
 * Architecture replicating the final composed UI in the reference box:
 * - A grouped Header Row containing descriptive labels.
 * - Left label: 'Overlay Opacity'
 * - Right label: Percentage value (e.g., '75%')
 * - Both labels use a specific, renderable font style and precise, clean-outline text effect.
 *   The internal text within the composed reference box is clean, small, renderable text 
 *   without large outlines.
 *
 * - Custom Slider Track Box layering the components:
 *    - Base background track: 'ic_slider_track_empty'
 *    - Filled track foreground: 'ic_slider_track_filled' (drawn from start up to thumb position)
 *    - Thumb: 'ic_slider_thumb_moon' with clean outlines, acting as the custom slider thumb.
 */
@Composable
fun CustomMoonSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    var trackWidth by remember { mutableFloatStateOf(1f) }
    var thumbWidth by remember { mutableFloatStateOf(0f) }

    val percentage = (value * 100).roundToInt()

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Labels row replicating the reference UI
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            // Clean, small, renderable text without large outlines
            Text(
                text = "Overlay Opacity",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = com.example.systemdarkoverlay.ui.theme.FredokaFontFamily,
                fontSize = 18.sp,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$percentage%",
                color = Color(0xFF9AA5B1), // Clean grayish-blue match for percentage
                fontWeight = FontWeight.ExtraBold,
                fontFamily = com.example.systemdarkoverlay.ui.theme.FredokaFontFamily,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
            )
        }

        // Custom Slider Track and Thumb Layering
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp) // Generous touch target
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        var currentX = down.position.x
                        
                        val mappedValue = (currentX / trackWidth).coerceIn(0f, 1f)
                        val scaledValue = valueRange.start + mappedValue * (valueRange.endInclusive - valueRange.start)
                        onValueChange(scaledValue.coerceIn(valueRange))
                        
                        do {
                            val event = awaitPointerEvent()
                            event.changes.forEach { change ->
                                if (change.pressed) {
                                    currentX = change.position.x
                                    val newMapped = (currentX / trackWidth).coerceIn(0f, 1f)
                                    val newScaled = valueRange.start + newMapped * (valueRange.endInclusive - valueRange.start)
                                    onValueChange(newScaled.coerceIn(valueRange))
                                    change.consume()
                                }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            // Track background base layer
            Image(
                painter = painterResource(id = R.drawable.ic_slider_track_empty),
                contentDescription = "Empty Track",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .onSizeChanged { trackWidth = it.width.toFloat().coerceAtLeast(1f) },
                contentScale = ContentScale.FillBounds
            )

            // Calculate current fill fraction
            val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)

            // Filled track layered from 0% up to thumb's position
            Image(
                painter = painterResource(id = R.drawable.ic_slider_track_filled),
                contentDescription = "Filled Track",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(FractionalRectangleShape(fraction)),
                contentScale = ContentScale.FillBounds
            )

            // Thumb positioned at current offset
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_slider_thumb_moon),
                    contentDescription = "Slider Thumb Moon",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .onSizeChanged { thumbWidth = it.width.toFloat() }
                        .offset {
                            val maxOffsetX = trackWidth
                            val offsetX = (fraction * maxOffsetX) - (thumbWidth / 2)
                            IntOffset(offsetX.toInt(), 0)
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Adjust the intensity of the mask",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontFamily = com.example.systemdarkoverlay.ui.theme.FredokaFontFamily,
            fontWeight = FontWeight.Normal
        )
    }
}
