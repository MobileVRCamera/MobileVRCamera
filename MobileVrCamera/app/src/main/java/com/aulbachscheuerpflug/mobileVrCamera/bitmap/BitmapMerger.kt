package com.aulbachscheuerpflug.mobileVrCamera.bitmap

import android.graphics.Bitmap
import android.graphics.Canvas
import com.aulbachscheuerpflug.mobileVrCamera.MainViewModel
import com.aulbachscheuerpflug.mobileVrCamera.camera.CameraDataSingleton

class BitmapMerger {
    companion object {
        //merges two bitmaps into 1
        //depending on the host phone placement setting the firstBitmap will either be on the left or right
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
