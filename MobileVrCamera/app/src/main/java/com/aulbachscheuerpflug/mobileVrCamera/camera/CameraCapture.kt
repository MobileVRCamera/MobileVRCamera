package com.aulbachscheuerpflug.mobileVrCamera.camera

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.hardware.camera2.CaptureRequest
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import com.aulbachscheuerpflug.mobileVrCamera.MAXIMUM_QUALITY_COMPRESSION
import com.aulbachscheuerpflug.mobileVrCamera.MINIMUM_QUALITY_COMPRESSION
import com.aulbachscheuerpflug.mobileVrCamera.MainViewModel
import com.aulbachscheuerpflug.mobileVrCamera.Persp2Equi.Persp2Equi
import com.aulbachscheuerpflug.mobileVrCamera.RECEIVE_IMAGE
import com.aulbachscheuerpflug.mobileVrCamera.TAKE_PICTURE_REQUEST
import com.aulbachscheuerpflug.mobileVrCamera.bitmap.BitmapConverter
import com.aulbachscheuerpflug.mobileVrCamera.bitmap.BitmapHelper
import com.aulbachscheuerpflug.mobileVrCamera.bitmap.BitmapMerger
import com.aulbachscheuerpflug.mobileVrCamera.bitmap.BitmapSaver
import com.aulbachscheuerpflug.mobileVrCamera.bluetooth.BluetoothViewModel
import com.aulbachscheuerpflug.mobileVrCamera.utils.getPlaceholderBitmap
import com.aulbachscheuerpflug.mobileVrCamera.utils.showDebugError
import com.aulbachscheuerpflug.mobileVrCamera.utils.showDebugInfo
import com.aulbachscheuerpflug.mobileVrCamera.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class CameraCapture {
    val persp2Equi = Persp2Equi()

    fun takePhoto(
        isHostDevice: Boolean,
        imageCapture: ImageCapture,
        contentResolver: ContentResolver,
        context: Context,
        bluetoothViewModel: BluetoothViewModel,
        mainViewModel: MainViewModel,
    ) {
        //Send request to 2nd smartphone to start photo capture
        @androidx.camera.camera2.interop.ExperimentalCamera2Interop
        if (isHostDevice && mainViewModel.bluetoothImageCapture) {
            bluetoothViewModel.sendMessage(
                TAKE_PICTURE_REQUEST,
                getPlaceholderBitmap(),
                MINIMUM_QUALITY_COMPRESSION,
                focus = CameraDataSingleton.getInstance().activeCamera2CameraControl!!.captureRequestOptions.getCaptureRequestOption(
                    CaptureRequest.LENS_FOCUS_DISTANCE
                ) ?: -1f,
                exposure = CameraDataSingleton.getInstance().activeCamera2CameraControl!!.captureRequestOptions.getCaptureRequestOption(
                    CaptureRequest.SENSOR_EXPOSURE_TIME
                ) ?: -1,
                sensitivity = CameraDataSingleton.getInstance().activeCamera2CameraControl!!.captureRequestOptions.getCaptureRequestOption(
                    CaptureRequest.SENSOR_SENSITIVITY
                ) ?: -1,
                stabilization = mainViewModel.imageStabilization,
                equirectangular = mainViewModel.equirectangularTransformation,
                manualMode = mainViewModel.useManualCameraSettings,
            )
        }

        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        imageCapture.targetRotation = CameraDataSingleton.getInstance().mostRecentPhotoOrientation

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(MobileVRCameraController.getFileFormat(), Locale.GERMANY)
            .format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MobileVRCamera")
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        // Set up image capture listener which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    showDebugError("Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    showToast(context, msg)
                    showDebugInfo(msg)
                }
            }
        )

        //Set up image capture listener which is triggered after photo has been taken but not saved
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    //save rotated bitmap of captured image
                    val bitmap = BitmapConverter.imageProxyToBitmap(image)
                    CameraDataSingleton.getInstance().mostRecentBitmap = BitmapHelper.rotateBitmap(
                        bitmap,
                        CameraDataSingleton.getInstance().mostRecentBitmapOrientation
                    )

                    //case equirectangular conversion is done
                    if (mainViewModel.equirectangularTransformation) {
                        //Create thread for conversion of perspective image to equirectangular
                        CoroutineScope(Dispatchers.Default).launch {
                            //disable photo button as image is processing
                            mainViewModel.isTakingStereoImage = true
                            val equirectangularBitmap = persp2Equi.persp2Equi(
                                BitmapHelper.rotateBitmap(
                                    bitmap,
                                    CameraDataSingleton.getInstance().mostRecentBitmapOrientation
                                ), bitmap.width, mainViewModel
                            )

                            CameraDataSingleton.getInstance().mostRecentEquirectangularBitmap =
                                equirectangularBitmap

                            //Save equirectangular bitmap as jpeg
                            withContext(Dispatchers.Main) {
                                BitmapSaver.createAndSaveJpegFromBitmap(
                                    equirectangularBitmap,
                                    context,
                                    mainViewModel,
                                    toastText = "Equi bitmap saved"
                                )
                                //enable photo button as image has been processed
                                mainViewModel.isTakingStereoImage = false

                                if (mainViewModel.bluetoothImageCapture) {
                                    if (!isHostDevice) {
                                        //Sending bitmap to host
                                        bluetoothViewModel.sendMessage(
                                            RECEIVE_IMAGE,
                                            equirectangularBitmap,
                                            MAXIMUM_QUALITY_COMPRESSION,
                                            5.0f,
                                            20,
                                            5,
                                            false,
                                            mainViewModel.equirectangularTransformation,
                                            mainViewModel.useManualCameraSettings,
                                        )
                                    }
                                } else {
                                    //save bitmap for usage in mono mode
                                    if (mainViewModel.localStereoBitmapHolder == null) {
                                        mainViewModel.localStereoBitmapHolder =
                                            equirectangularBitmap
                                    } else {
                                        BitmapSaver.createAndSaveJpegFromBitmap(
                                            bitmap = BitmapMerger.createSingleImageFromTwoImages(
                                                firstBitmap = mainViewModel.localStereoBitmapHolder!!,
                                                secondBitmap = equirectangularBitmap,
                                                mainViewModel = mainViewModel
                                            ),
                                            context = context,
                                            mainViewModel = mainViewModel,
                                            toastText = "Bitmap merge succeeded"
                                        )
                                        mainViewModel.localStereoBitmapHolder = null
                                    }
                                }
                            }
                        }
                    } else {
                        if (mainViewModel.bluetoothImageCapture) {
                            if (!isHostDevice) {
                                bluetoothViewModel.sendMessage(
                                    RECEIVE_IMAGE,
                                    CameraDataSingleton.getInstance().mostRecentBitmap!!,
                                    MAXIMUM_QUALITY_COMPRESSION,
                                    5.0f,
                                    20,
                                    5,
                                    false,
                                    mainViewModel.equirectangularTransformation,
                                    mainViewModel.useManualCameraSettings,
                                )
                            }
                        } else {
                            if (mainViewModel.localStereoBitmapHolder == null) {
                                mainViewModel.localStereoBitmapHolder =
                                    CameraDataSingleton.getInstance().mostRecentBitmap
                            } else {
                                BitmapSaver.createAndSaveJpegFromBitmap(
                                    bitmap = BitmapMerger.createSingleImageFromTwoImages(
                                        firstBitmap = mainViewModel.localStereoBitmapHolder!!,
                                        secondBitmap = CameraDataSingleton.getInstance().mostRecentBitmap!!,
                                        mainViewModel = mainViewModel
                                    ),
                                    context = context,
                                    mainViewModel = mainViewModel,
                                    toastText = "Photo merge succeeded"
                                )
                                mainViewModel.localStereoBitmapHolder = null
                            }
                        }
                    }
                    if (!mainViewModel.bluetoothImageCapture || !bluetoothViewModel.state.value.isConnected) mainViewModel.isTakingStereoImage =
                        false
                    super.onCaptureSuccess(image)
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                }
            }
        )
    }
}