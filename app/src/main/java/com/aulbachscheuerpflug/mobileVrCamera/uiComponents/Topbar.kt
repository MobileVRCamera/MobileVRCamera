package com.aulbachscheuerpflug.mobileVrCamera.uiComponents

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aulbachscheuerpflug.mobileVrCamera.APP_NAME
import com.aulbachscheuerpflug.mobileVrCamera.EXPO_URL
import com.aulbachscheuerpflug.mobileVrCamera.MainViewModel
import com.aulbachscheuerpflug.mobileVrCamera.R
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.Black
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.Grey
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.ImmerVrGradient
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.RoundShape
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.White
import com.aulbachscheuerpflug.mobileVrCamera.utils.openWebsite
import com.aulbachscheuerpflug.mobileVrCamera.utils.playSystemSound

@Composable
fun TopBar(
    onCloseButton: (() -> Unit)? = null,
    onInfoButton: (() -> Unit)? = null,
    onGuidedModeButton: (() -> Unit)? = null,
    rotation: Int,
    mainViewModel: MainViewModel,
) {
    val context = LocalContext.current
    val animatedRotationDegrees by animateFloatAsState(
        targetValue = rotation.toFloat(),
        animationSpec = TweenSpec(durationMillis = 500, easing = LinearOutSlowInEasing)
    )
    var clickTimes by remember { mutableStateOf(listOf<Long>()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(brush = ImmerVrGradient)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp, start = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = CenterVertically
        ) {
            TextButton(onClick = {
                playSystemSound(context)
                if (onGuidedModeButton == null) {
                    clickTimes = clickTimes.plus(System.currentTimeMillis())
                    while (clickTimes.size >= 5 && clickTimes.last() - clickTimes.first() > 5000) {
                        clickTimes = clickTimes.drop(1).takeLast(5)
                    }
                    if (clickTimes.size >= 5) {
                        openWebsite(
                            context,
                            EXPO_URL
                        )
                    }
                } else {
                    onGuidedModeButton()
                }
            }) {
                Box(
                    modifier = Modifier
                        .clip(shape = RoundShape)
                        .background(color = White)
                        .border(1.dp, Black, RoundShape)) {
                    Text(
                        text = if (onGuidedModeButton != null) "Guide me" else APP_NAME,
                        color = Black,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp),
                        fontStyle = FontStyle.Italic,
                    )
                }
            }
            if (onInfoButton != null) {
                IconButton(onClick = {
                    playSystemSound(context)
                    onInfoButton()
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_info_24),
                        modifier = Modifier
                            .padding(bottom = 5.dp, end = 40.dp)
                            .requiredSize(25.dp)
                            .border(1.dp, Black, RoundShape)
                            .background(Black, shape = RoundShape)
                            .rotate(animatedRotationDegrees),
                        contentDescription = "Info",
                        tint = if (mainViewModel.showInfoPopUps) Grey else White
                    )
                }
            }
            if (onCloseButton != null) {
                IconButton(onClick = {
                    playSystemSound(context)
                    onCloseButton()
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = White,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun TopBarPreview() {
    val dummyMainViewModel = MainViewModel()
    TopBar(
        rotation = 0,
        onInfoButton = {},
        mainViewModel = dummyMainViewModel
    )
}