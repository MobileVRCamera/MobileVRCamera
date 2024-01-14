package com.aulbachscheuerpflug.mobileVrCamera.camera

class MobileVRCameraController {
    var cameraCapture: CameraCapture = CameraCapture()
    var cameraSetup: CameraSetup = CameraSetup()
    var cameraInitializer: CameraInitializer = CameraInitializer()

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

        fun getFileFormat(): String {
            return FILENAME_FORMAT
        }
    }
}