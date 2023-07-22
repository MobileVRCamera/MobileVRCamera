package com.aulbachscheuerpflug.mobileVrCamera.uiComponents

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.White

@Composable
fun OverlayGrid(visible: Boolean, modifier: Modifier) {
    if (visible) {
        Box(
            modifier = modifier
        ) {

            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val lineColor = White
                val strokeWidth = 2.dp.toPx()

                val sectionWidth = canvasWidth / 3

                for (i in 1..2) {
                    drawLine(
                        color = lineColor,
                        start = Offset(x = sectionWidth * i, y = 0f),
                        end = Offset(x = sectionWidth * i, y = canvasHeight),
                        strokeWidth = strokeWidth
                    )
                }

                val sectionHeight = canvasHeight / 3
                for (i in 1..2) {
                    drawLine(
                        color = lineColor,
                        start = Offset(x = 0f, y = sectionHeight * i),
                        end = Offset(x = canvasWidth, y = sectionHeight * i),
                        strokeWidth = strokeWidth
                    )
                }
            }
        }
    }
}
