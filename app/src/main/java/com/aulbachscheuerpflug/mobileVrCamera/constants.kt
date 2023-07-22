package com.aulbachscheuerpflug.mobileVrCamera

const val MAXIMUM_QUALITY_COMPRESSION = 100
const val MINIMUM_QUALITY_COMPRESSION = 1

const val TAKE_PICTURE_REQUEST = "TakePictureRequest"
const val RECEIVE_IMAGE = "ReceiveImage"

const val STATE_RECEIVER_CONNECTION_ERROR = "Can't connect to a non-paired device"
const val NO_BLUETOOTH_CONNECTION_PERMISSION = "No Bluetooth Connection permission"
const val BLUETOOTH_CONNECTION_ERROR = "Connection was lost or terminated"
const val FAILED_INCOMING_DATA = "Failed to read incoming data"
const val RESTART_APP = "Restart App"
const val NOT_AVAILABLE_DESC = "This feature is currently not available on your device-type"

const val NO_ACCESS_CAMERA = "cannot access camera"

const val BLUETOOTH_TRANSFER_SIZE = 262144
const val ACCUMULATED_DATA_SEPARATION_CHAR = ";"
const val IMAGE_DATA_SEPARATION_CHAR = "|"
const val DATA_SEPARATION = "!"
const val CAMERA_SETTINGS_DATA_SEPARATION_CHAR = "^"

const val STATE_TIMEOUT_MILLIS = 5000

const val APP_NAME = "Mobile VR Camera"
const val LOG_INFO_TAG = APP_NAME + "InfoLog"
const val LOG_ERROR_TAG = APP_NAME + "ErrorLog"

const val CAMERA_ROUTE = "Camera"
const val SETTINGS_INFO_ROUTE = "Settings"
const val BLUETOOTH_ROUTE = "Bluetooth"
const val GALLERY_ROUTE = "Gallery"

const val IMAGE_TRANSFER = "Image Transfer: "
const val EQUI_TRANSFER = "Equirectangular Transformation: "
const val GALLERY_ALERT_TITLE = "What would you like to do with this image?"

const val GUIDED_MODE_BLOCK = "Disable Guided Mode first"
const val DISABLED_CAMERA = "Camera disabled - Please check your permissions or restart the app"
const val EXPO_URL = "https://games.uni-wuerzburg.de/expo/2023/mobile-vr-camera/"
const val STEREO_PHOTO_MAKER_URL = "https://stereo.jpn.org/eng/stphmkr/index.html"

const val PERMANENTLY_DECLINED_DESC_1 = "You have permanently denied the "
const val PERMANENTLY_DECLINED_DESC_2 = " permission. To grant it, you can go to the app settings."
const val PERMISSION_DIALOG_HEADER = "Permission required"
const val GRANT_PERMISSION = "Grant permission"
const val GRANTED_PERMISSION = "OK"
const val CAMERA_DECLINED_DESC = "This app needs access to your camera so that you can take wide-angle photos."
const val MICROPHONE_DECLINED_DESC = "This app needs access to your microphone because Android requires it when using the camera, even though we only take photos and don't record videos."
const val BLUETOOTH_DECLINED_DESC = "This app needs access to Bluetooth to connect with other devices."

const val INFO_CONTENT_DESC = "Info"
const val BLUETOOTH_INFO_DESC = "Access the Bluetooth menu, where you'll connect your device to another smartphone. Choose whether to host or join a session."
const val TAKE_PHOTO_INFO_DESC = "Initiates a synchronized capture on both connected devices, merging the two images into a single, stereoscopic VR-ready photo with just 1 press"
const val GALLERY_VIEW_INFO_DESC = "Access the in-app Gallery to view, delete, and share your photos taken by $APP_NAME"
const val SETTINGS_INFO_DESC = "Enter the settings menu to tweak settings, access quality of life features and adjust image capture preferences for optimal results."
const val BLUETOOTH_SELECTION_DESC = "Please select your preferred Bluetooth device from the list. Ideally, connect to another smartphone of the same type as yours. Click on a device name to connect. If your device is not listed, please ensure it's turned on and within range."

const val TAKE_PHOTO_GUIDE_INFO_DESC = "Now to the most important step. Make sure that the phone you connected to is the one on the left and hold your smartphones in parallel towards the target. Finally, press the camera button on the left smartphone to create your photo. Note: It may take a while to send an process the images."
const val GALLERY_VIEW_GUIDE_INFO_DESC = "Congratulations! You now mastered the basics of the Mobile VR Camera. You can now view the result in the gallery or toy around with settings in setting menu."
const val BLUETOOTH_SELECTION_GUIDE_DESC = "Here you can find a list of all the devices your smartphone has previously connected to. Furthermore, you can also see all the smartphones with Bluetooth enabled in your environment. To proceed, please click on the smartphone you wish to establish a connection to."

enum class Duration {
    ZERO, ONE, THREE, FIVE, TEN, FIFTEEN
}

enum class GuidedModeSteps {
    BLUETOOTH, PHOTO, GALLERY
}