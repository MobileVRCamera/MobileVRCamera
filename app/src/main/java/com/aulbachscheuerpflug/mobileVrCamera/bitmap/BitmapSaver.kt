package com.aulbachscheuerpflug.mobileVrCamera.bitmap

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import com.aulbachscheuerpflug.mobileVrCamera.MainViewModel
import com.aulbachscheuerpflug.mobileVrCamera.utils.showToast
import java.io.OutputStream

class BitmapSaver {
    companion object {
        fun createAndSaveJpegFromBitmap(bitmap: Bitmap, context: Context, mainViewModel: MainViewModel, toastText: String) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MobileVRCamera")
                put(MediaStore.Images.Media.IS_PENDING, true)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            if (uri != null) {
                saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
                contentValues.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, contentValues, null, null)
                showToast(context, "$toastText: $uri")
            }
            mainViewModel.isTakingStereoImage = false
        }

        private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
            if (outputStream != null) {
                try {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }
}