package com.aulbachscheuerpflug.mobileVrCamera.uiComponents

import androidx.compose.animation.core.EaseInSine
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.toOffset
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.Black

@Composable
fun Spotlight(
    intOffset: IntOffset,
    isSpotlightVisible: Boolean,
    isAnimationEnabled: Boolean = true,
    startRadius: Float = 1000f,
    endRadius: Float = 200f
) {
    var targetRadius by remember { mutableStateOf(startRadius) }

    LaunchedEffect(isSpotlightVisible) {
        targetRadius = if (isSpotlightVisible) {
            endRadius
        } else {
            startRadius
        }
    }

    val animatedRadius by animateFloatAsState(
        targetValue = if (isAnimationEnabled) targetRadius else endRadius,
        animationSpec = tween(
            durationMillis = if (isAnimationEnabled) 500 else 0,
            easing = EaseInSine
        )
    )

    if (isSpotlightVisible) {
        Canvas(
            modifier = Modifier.fillMaxSize(),
            onDraw = {
                val spotlightPath = Path().apply {
                    addOval(
                        oval = Rect(
                            center = intOffset.toOffset(),
                            radius = animatedRadius
                        )
                    )
                }
                clipPath(
                    path = spotlightPath,
                    clipOp = ClipOp.Difference
                ) {
                    drawRect(SolidColor(Black.copy(alpha = 0.5f)))
                }
            }
        )
    }
}