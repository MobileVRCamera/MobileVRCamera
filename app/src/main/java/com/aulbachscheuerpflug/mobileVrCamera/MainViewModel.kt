package com.aulbachscheuerpflug.mobileVrCamera

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job

class MainViewModel : ViewModel() {
    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    var gridEnabled by mutableStateOf(false)
    var flashlightEnabled by mutableStateOf(false)

    var showInfoPopUps by mutableStateOf(false)
    var showDescPopUp by mutableStateOf(false)
    var titleText by mutableStateOf("title")
    var descriptionText by mutableStateOf("description")

    var hostPhoneLeftPlacement by mutableStateOf(true)
    var imageStabilization by mutableStateOf(false)

    var useManualCameraSettings by mutableStateOf(false)
    var focusSliderValue by mutableStateOf(0f)
    var sensitivitySliderValue by mutableStateOf(0f)
    var exposureSliderValue by mutableStateOf(0f)

    var bluetoothImageCapture by mutableStateOf(true)
    var localStereoBitmapHolder by mutableStateOf<Bitmap?>(null)

    var equirectangularTransformation by mutableStateOf(true)

    var deviceFov by mutableStateOf(120)
    var antiAliasing by mutableStateOf(1)
    var perspectiveToEquirectangularProgression by mutableStateOf(0)

    var selectedDuration by mutableStateOf(Duration.ZERO)
    var countdown by mutableStateOf<Int?>(null)
    var countdownJob by mutableStateOf<Job?>(null)

    var isTakingStereoImage by mutableStateOf(false)

    var isSpotlightVisible by mutableStateOf(false)
    var guidedModeSteps by mutableStateOf<GuidedModeSteps?>(null)
    var bluetoothButtonPosition by mutableStateOf(IntOffset(0, 0))
    var takePhotoButtonPosition by mutableStateOf(IntOffset(0, 0))
    var galleryButtonPosition by mutableStateOf(IntOffset(0, 0))
    val spotlightPosition: IntOffset?
        get() = when (guidedModeSteps) {
            GuidedModeSteps.BLUETOOTH -> bluetoothButtonPosition
            GuidedModeSteps.PHOTO -> takePhotoButtonPosition
            GuidedModeSteps.GALLERY -> galleryButtonPosition
            else -> null
        }

    fun dismissDialog() {
        visiblePermissionDialogQueue.removeFirst()
    }

    fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ) {
        if (!isGranted && !visiblePermissionDialogQueue.contains(permission)) {
            visiblePermissionDialogQueue.add(permission)
        }
    }
}
