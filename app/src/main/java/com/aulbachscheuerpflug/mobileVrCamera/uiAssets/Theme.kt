package com.aulbachscheuerpflug.mobileVrCamera.uiAssets

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview


private val DefaultPalette = lightColors(
    primary = ImmerVrBlueDarker,
    primaryVariant = ImmerVrBlue,
    secondary = Teal700
)

@Composable
fun MobileVrCameraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = DefaultPalette,
        typography = DefaultTypography,
        shapes = Shapes,
        content = content
    )
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun MobileVrCameraThemePreview(
) {
    MobileVrCameraTheme(
        content = {}
    )
}