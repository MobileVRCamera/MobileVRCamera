package com.aulbachscheuerpflug.mobileVrCamera.bitmap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

class BitmapConverter {
    companion object {
        //Conversion of ImageProxy to Bitmap
        fun imageProxyToBitmap(image: ImageProxy): Bitmap {
            val planeProxy = image.planes[0]
            val buffer: ByteBuffer = planeProxy.buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }
}