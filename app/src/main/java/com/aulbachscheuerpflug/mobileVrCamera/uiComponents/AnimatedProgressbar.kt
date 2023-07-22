package com.aulbachscheuerpflug.mobileVrCamera.uiComponents

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.Black
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.LightGrey
import com.aulbachscheuerpflug.mobileVrCamera.MAXIMUM_QUALITY_COMPRESSION
import com.aulbachscheuerpflug.mobileVrCamera.MINIMUM_QUALITY_COMPRESSION

@Composable
fun AnimatedProgressBar(progress: Int, progressText: String) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress / 100f,
        animationSpec = tween(durationMillis = 50)
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (progress in MINIMUM_QUALITY_COMPRESSION until MAXIMUM_QUALITY_COMPRESSION) 1f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    val color by animateColorAsState(
        targetValue = lerp(Color.Red, Color.Green, progress / 100f),
        animationSpec = tween(durationMillis = 500)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .alpha(animatedAlpha)
    ) {
        LinearProgressIndicator(
            backgroundColor = LightGrey,
            color = color,
            progress = animatedProgress,
            modifier = Modifier.fillMaxWidth().height(30.dp).border(1.dp, Black)
        )
        Text(
            fontSize = 20.sp,
            text = "$progressText${progress}%",
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
fun AnimatedProgressBarPreview() {

    AnimatedProgressBar(
        50,
        "Progress: "
    )
}