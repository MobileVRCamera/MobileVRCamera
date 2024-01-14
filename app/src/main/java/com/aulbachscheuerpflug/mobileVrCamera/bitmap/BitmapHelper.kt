package com.aulbachscheuerpflug.mobileVrCamera.bitmap

import android.graphics.Bitmap
import android.graphics.Matrix

class BitmapHelper {
    companion object {
        fun rotateBitmap(sourceBitmap: Bitmap, angle: Int): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(angle.toFloat())
            return Bitmap.createBitmap(
                sourceBitmap,
                0,
                0,
                sourceBitmap.width,
                sourceBitmap.height,
                matrix,
                true
            )
        }
    }
}