package com.aulbachscheuerpflug.mobileVrCamera.bitmap

import android.graphics.Bitmap
import android.graphics.Canvas
import com.aulbachscheuerpflug.mobileVrCamera.MainViewModel

class BitmapMerger {
    companion object {
        fun createSingleImageFromTwoImages(
            firstBitmap: Bitmap,
            secondBitmap: Bitmap,
            mainViewModel: MainViewModel
        ): Bitmap {
            val bitmap: Bitmap
            if (mainViewModel.hostPhoneLeftPlacement) {
                bitmap = Bitmap.createBitmap(
                    firstBitmap.width + secondBitmap.width,
                    firstBitmap.height,
                    Bitmap.Config.ARGB_8888
                )

                val canvas = Canvas(bitmap)
                canvas.drawBitmap(firstBitmap, 0.0f, 0.0f, null)
                canvas.drawBitmap(secondBitmap, firstBitmap.width.toFloat(), 0.0f, null)
            } else {
                bitmap = Bitmap.createBitmap(
                    firstBitmap.width + secondBitmap.width,
                    firstBitmap.height,
                    Bitmap.Config.ARGB_8888
                )

                val canvas = Canvas(bitmap)
                canvas.drawBitmap(secondBitmap, 0.0f, 0.0f, null)
                canvas.drawBitmap(firstBitmap, secondBitmap.width.toFloat(), 0.0f, null)
            }

            return bitmap
        }
    }
}
