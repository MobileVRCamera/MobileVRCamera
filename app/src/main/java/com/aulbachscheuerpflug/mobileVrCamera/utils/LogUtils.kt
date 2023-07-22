package com.aulbachscheuerpflug.mobileVrCamera.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.aulbachscheuerpflug.mobileVrCamera.LOG_ERROR_TAG
import com.aulbachscheuerpflug.mobileVrCamera.LOG_INFO_TAG

fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
    val toast = Toast.makeText(context, message, duration)
    toast.show()
}

fun showDebugInfo(msg: String, tag: String = LOG_INFO_TAG) {
    Log.d(tag, msg)
}

fun showDebugError(msg: String, error: Throwable? = null) {
    Log.e(LOG_ERROR_TAG, msg, error)
}