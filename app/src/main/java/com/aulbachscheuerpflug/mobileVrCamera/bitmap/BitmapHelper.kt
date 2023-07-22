package com.aulbachscheuerpflug.mobileVrCamera.bitmap

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log

class BitmapHelper {
    companion object {
        //rotate a bitmap around the given angle counterclockwise
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