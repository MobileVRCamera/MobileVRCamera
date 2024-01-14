package com.aulbachscheuerpflug.mobileVrCamera.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

suspend fun base64ImageStringToBitmap(base64Image: String): Bitmap =
    withContext(Dispatchers.Default) {
        val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        return@withContext bitmap ?: getPlaceholderBitmap()
    }

suspend fun Bitmap.toBase64(quality: Int): String = withContext(Dispatchers.Default) {
    val byteArrayOutputStream = ByteArrayOutputStream()
    this@toBase64.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return@withContext Base64.encodeToString(byteArray, Base64.DEFAULT)
}

fun getPlaceholderBitmap(): Bitmap {
    return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply {
        setPixel(0, 0, Color.WHITE)
    }
}