package com.aulbachscheuerpflug.mobileVrCamera.uiComponents

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aulbachscheuerpflug.mobileVrCamera.GALLERY_ROUTE
import com.aulbachscheuerpflug.mobileVrCamera.GALLERY_VIEW_GUIDE_INFO_DESC
import com.aulbachscheuerpflug.mobileVrCamera.GALLERY_VIEW_INFO_DESC
import com.aulbachscheuerpflug.mobileVrCamera.GuidedModeSteps
import com.aulbachscheuerpflug.mobileVrCamera.INFO_CONTENT_DESC
import com.aulbachscheuerpflug.mobileVrCamera.MainViewModel
import com.aulbachscheuerpflug.mobileVrCamera.R
import com.aulbachscheuerpflug.mobileVrCamera.SETTINGS_INFO_DESC
import com.aulbachscheuerpflug.mobileVrCamera.SETTINGS_INFO_ROUTE
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.Black
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.Grey
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.RoundShape
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.White
import com.aulbachscheuerpflug.mobileVrCamera.utils.loadImagesFromDirectory
import com.aulbachscheuerpflug.mobileVrCamera.utils.playSystemSound
import kotlin.math.roundToInt

@Composable
fun SettingsGalleryButtons(
    mainViewModel: MainViewModel,
    onSettingsInfoClicked: () -> Unit,
    onGalleryClicked: () -> Unit,
    context: Context,
    rotation: Int,
) {
    val configuration = LocalConfiguration.current
    val animatedRotationDegrees by animateFloatAsState(
        targetValue = rotation.toFloat(),
        animationSpec = TweenSpec(durationMillis = 500, easing = LinearOutSlowInEasing)
    )
    val images = loadImagesFromDirectory(context)

    Surface(
        color = Color.Transparent,
        shape = RectangleShape,
        modifier = Modifier
            .width((configuration.screenWidthDp / 3).dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = White,
                shape = RoundShape,
                modifier = Modifier
                    .padding(bottom = 15.dp)
                    .border(1.dp, Black, RoundShape)
            ) {
                IconButton(
                    onClick = {
                        playSystemSound(context)
                        if (!mainViewModel.showInfoPopUps) {
                            onSettingsInfoClicked()
                        } else {
                            mainViewModel.titleText = SETTINGS_INFO_ROUTE
                            mainViewModel.descriptionText =
                                SETTINGS_INFO_DESC
                            mainViewModel.showDescPopUp = true
                        }
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .rotate(animatedRotationDegrees),
                    enabled = mainViewModel.guidedModeSteps == null
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_settings_24),
                        contentDescription = "Settings-Button",
                        tint = Black,
                        modifier = Modifier.requiredSize(48.dp)
                    )
                }
            }
            Surface(
                color = White,
                shape = RoundShape,
                modifier = Modifier
                    .border(1.dp, Black, RoundShape)
            ) {
                IconButton(
                    onClick = {
                        playSystemSound(context)
                        if (!mainViewModel.showInfoPopUps) {
                            onGalleryClicked()
                        } else {
                            mainViewModel.titleText = GALLERY_ROUTE
                            mainViewModel.descriptionText =
                                if (mainViewModel.guidedModeSteps == null) GALLERY_VIEW_INFO_DESC else GALLERY_VIEW_GUIDE_INFO_DESC
                            mainViewModel.showDescPopUp = true
                        }
                    },
                    modifier = Modifier
                        .onGloballyPositioned { layoutCoordinates ->
                            val position = layoutCoordinates.localToRoot(Offset(0f, 0f))
                            val size = layoutCoordinates.size
                            val centerX = position.x + size.width / 2
                            val centerY = position.y + size.height / 2
                            mainViewModel.galleryButtonPosition =
                                IntOffset(centerX.roundToInt(), centerY.roundToInt())
                        }
                        .rotate(animatedRotationDegrees)
                        .size(62.dp),
                    enabled = mainViewModel.guidedModeSteps == null || mainViewModel.guidedModeSteps == GuidedModeSteps.GALLERY
                ) {
                    if (images.isNotEmpty()) {
                        AsyncImage(
                            model = images.last(),
                            contentDescription = null,
                            modifier = Modifier.size(62.dp).clip(RoundShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_photo_library_24),
                            contentDescription = "Gallery-Button",
                            tint = Black,
                            modifier = Modifier.size(48.dp).padding(8.dp)
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
                    .offset(35.dp, 40.dp)
                    .border(1.dp, Black, RoundShape)
                    .background(Black, shape = RoundShape)
                    .rotate(animatedRotationDegrees),
                contentDescription = INFO_CONTENT_DESC,
                tint = if (mainViewModel.descriptionText == SETTINGS_INFO_DESC && mainViewModel.showDescPopUp) Grey else White,

                )
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
                    .offset(35.dp, 110.dp)
                    .border(1.dp, Black, RoundShape)
                    .background(Black, shape = RoundShape)
                    .rotate(animatedRotationDegrees),
                contentDescription = INFO_CONTENT_DESC,
                tint = if (mainViewModel.descriptionText == GALLERY_VIEW_INFO_DESC && mainViewModel.showDescPopUp) Grey else White,
                )
        }
    }
}

@Preview
@Composable
fun SettingsGalleryScreenPreview() {
    val mainViewModelDummy = MainViewModel()
    SettingsGalleryButtons(
        mainViewModel = mainViewModelDummy,
        onGalleryClicked = {},
        onSettingsInfoClicked = {},
        context = LocalContext.current,
        rotation = 0,
    )
}