package com.aulbachscheuerpflug.mobileVrCamera.utils

import android.content.Intent
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Parcelable

// Source: https://stackoverflow.com/questions/73019160/android-getparcelableextra-deprecated
inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    SDK_INT > Build.VERSION_CODES.TIRAMISU -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}