package com.aulbachscheuerpflug.mobileVrCamera.uiComponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulbachscheuerpflug.mobileVrCamera.Duration
import com.aulbachscheuerpflug.mobileVrCamera.GuidedModeSteps
import com.aulbachscheuerpflug.mobileVrCamera.INFO_CONTENT_DESC
import com.aulbachscheuerpflug.mobileVrCamera.MAXIMUM_QUALITY_COMPRESSION
import com.aulbachscheuerpflug.mobileVrCamera.MINIMUM_QUALITY_COMPRESSION
import com.aulbachscheuerpflug.mobileVrCamera.MainViewModel
import com.aulbachscheuerpflug.mobileVrCamera.R
import com.aulbachscheuerpflug.mobileVrCamera.TAKE_PHOTO_GUIDE_INFO_DESC
import com.aulbachscheuerpflug.mobileVrCamera.TAKE_PHOTO_INFO_DESC
import com.aulbachscheuerpflug.mobileVrCamera.bluetooth.BluetoothUiState
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.Black
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.Grey
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.RoundShape
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.White
import com.aulbachscheuerpflug.mobileVrCamera.utils.playCustomSound
import com.aulbachscheuerpflug.mobileVrCamera.utils.playSystemSound
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun TakePhotoButton(
    mainViewModel: MainViewModel,
    bluetoothState: BluetoothUiState,
    onTakePhotoClicked: () -> Unit,
    rotation: Int,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val coroutineScope = rememberCoroutineScope()
    val animatedRotationDegrees by animateFloatAsState(
        targetValue = rotation.toFloat(),
        animationSpec = TweenSpec(durationMillis = 500, easing = LinearOutSlowInEasing)
    )

    Surface(
        color = Color.Transparent,
        shape = RectangleShape,
        modifier = Modifier
            .size((configuration.screenWidthDp / 3).dp)
    ) {
        Surface(
            color = if (mainViewModel.isTakingStereoImage) Grey else White,
            shape = RoundShape,
            modifier = Modifier
                .size(128.dp)
                .padding(all = 5.dp)
                .border(1.dp, Black, RoundShape)
        ) {
            IconButton(
                onClick = {
                    playSystemSound(context)
                    if (!mainViewModel.showInfoPopUps) {
                        if (mainViewModel.countdown == null) {
                            val delaySeconds = when (mainViewModel.selectedDuration) {
                                Duration.ZERO -> 0
                                Duration.ONE -> 1
                                Duration.THREE -> 3
                                Duration.FIVE -> 5
                                Duration.TEN -> 10
                                Duration.FIFTEEN -> 15
                            }

                            if (delaySeconds > 0) {
                                val shouldStop = false
                                mainViewModel.countdownJob = coroutineScope.launch {
                                    for (i in delaySeconds downTo 1) {
                                        if (shouldStop) break
                                        mainViewModel.countdown = i
                                        delay(1000)
                                    }
                                    if (!shouldStop) {
                                        mainViewModel.countdown = null
                                        onTakePhotoClicked()
                                        playCustomSound(context, R.raw.phototaken)
                                        mainViewModel.isTakingStereoImage = true
                                    }
                                }
                            } else {
                                onTakePhotoClicked()
                                mainViewModel.isTakingStereoImage = true
                                playCustomSound(context, R.raw.phototaken)
                            }
                        } else {
                            mainViewModel.countdownJob?.cancel()
                            mainViewModel.countdownJob = null
                            mainViewModel.countdown = null
                        }
                    } else {
                        mainViewModel.titleText = "Take Photo:"
                        mainViewModel.descriptionText =
                            if (mainViewModel.guidedModeSteps == null) TAKE_PHOTO_INFO_DESC else TAKE_PHOTO_GUIDE_INFO_DESC
                        mainViewModel.showDescPopUp = true
                    }
                },
                modifier = Modifier
                    .padding(8.dp)
                    .onGloballyPositioned { layoutCoordinates ->
                        val position = layoutCoordinates.localToRoot(Offset(0f, 0f))
                        val size = layoutCoordinates.size
                        val centerX = position.x + size.width / 2
                        val centerY = position.y + size.height / 2
                        mainViewModel.takePhotoButtonPosition =
                            IntOffset(centerX.roundToInt(), centerY.roundToInt())
                    }
                    .rotate(animatedRotationDegrees),
                enabled = (mainViewModel.guidedModeSteps == null || mainViewModel.guidedModeSteps == GuidedModeSteps.PHOTO || (bluetoothState.transferProgress in MINIMUM_QUALITY_COMPRESSION until MAXIMUM_QUALITY_COMPRESSION-1)) && !mainViewModel.isTakingStereoImage
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(65.dp)) {
                    if (mainViewModel.countdown == null) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_camera_alt_48),
                            contentDescription = "TakePhoto-Button",
                            tint = Black,
                            modifier = Modifier.requiredSize(65.dp)
                        )
                    } else {
                        Text(
                            text = mainViewModel.countdown.toString(),
                            fontSize = 36.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = mainViewModel.showInfoPopUps,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_info_24),
                modifier = Modifier
                    .requiredSize(30.dp)
                    .offset(45.dp, 40.dp)
                    .border(1.dp, Black, RoundShape)
                    .background(Black, shape = RoundShape)
                    .rotate(animatedRotationDegrees),
                contentDescription = INFO_CONTENT_DESC,
                tint = if (mainViewModel.descriptionText == TAKE_PHOTO_INFO_DESC && mainViewModel.showDescPopUp) Grey else White,
            )
        }
    }
}

@Preview
@Composable
fun TakePhotoButtonPreview() {
    val mainViewModelDummy = MainViewModel()
    val bluetoothUiStateDummy = BluetoothUiState(
        scannedDevices = listOf(),
        pairedDevices = listOf(),
        isConnected = false,
        isConnecting = false,
        errorMessage = null,
        transferProgress = 50,
        isScanning = false,
    )
    TakePhotoButton(
        mainViewModel = mainViewModelDummy,
        bluetoothState = bluetoothUiStateDummy,
        onTakePhotoClicked = {},
        rotation = 0,
    )
}