package com.aulbachscheuerpflug.mobileVrCamera.equirectangularTransformation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import com.aulbachscheuerpflug.mobileVrCamera.MainViewModel
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.max
import kotlin.math.tan

class PerspectiveToEquirectangular {
    private var horizontalPixel: Int = 0
    private var verticalPixel: Int = 0

    fun convertToEquirectangular(
        inputBitmap: Bitmap,
        imageWidth: Int,
        viewModel: MainViewModel,
    ): Bitmap {
        val params = buildTransformationParams(inputBitmap.width, inputBitmap.height, imageWidth, viewModel)
        val outputBitmap = Bitmap.createBitmap(params.outputWidth, params.outputHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)

        for (h in 0 until params.outputHeight) {
            for (w in 0 until params.outputWidth) {
                var rgbValue = IntArray(3)
                for (aaH in 0 until params.antiAliasingLevel) {
                    for (aaW in 0 until params.antiAliasingLevel) {
                        val x = 2 * (w + aaW / params.antiAliasingLevel.toDouble()) / params.outputWidth.toDouble() - 1
                        val y = 2 * (h + aaH / params.antiAliasingLevel.toDouble()) / params.outputHeight.toDouble() - 1

                        val longitude = x * Math.PI
                        val latitude = y * 0.5 * Math.PI

                        if (mapCoordinates(latitude, longitude, params)) {
                            rgbValue[0] = Color.red(inputBitmap.getPixel(horizontalPixel, verticalPixel))
                            rgbValue[1] = Color.green(inputBitmap.getPixel(horizontalPixel, verticalPixel))
                            rgbValue[2] = Color.blue(inputBitmap.getPixel(horizontalPixel, verticalPixel))
                        } else {
                            rgbValue = IntArray(3)
                        }
                    }
                }
                outputBitmap.setPixel(
                    w,
                    h,
                    Color.rgb(
                        rgbValue[0],
                        rgbValue[1],
                        rgbValue[2]
                    )
                )
            }
            viewModel.perspectiveToEquirectangularProgression =
                ((h.toFloat() / params.outputHeight.toFloat()) * 100f).roundToInt()
        }

        canvas.drawBitmap(outputBitmap, 0.0f, 0.0f, null)

        return outputBitmap
    }

    private fun mapCoordinates(lat: Double, long: Double, params: TransformationParams): Boolean {
        val coordinates = doubleArrayOf(cos(lat) * sin(long), cos(lat) * cos(long), sin(lat))

        if (coordinates[1] <= 0) {
            return false
        }

        val muFactor = 1.0 / coordinates[1]
        val x = muFactor * coordinates[0]
        val z = muFactor * coordinates[2]

        if (x <= -params.horizontalCoefficient || x >= params.horizontalCoefficient) {
            return false
        }
        if (z <= -params.verticalCoefficient || z >= params.verticalCoefficient) {
            return false
        }

        horizontalPixel = (params.perspectiveWidth * 0.5 * (x + params.horizontalCoefficient) / params.horizontalCoefficient).toInt()
        verticalPixel = (params.perspectiveHeight * 0.5 * (z + params.verticalCoefficient) / params.verticalCoefficient).toInt()

        return true
    }

    private fun buildTransformationParams(
        pWidth: Int,
        pHeight: Int,
        imageWidth: Int,
        viewModel: MainViewModel
    ): TransformationParams {
        val fov = Math.PI / 180 * viewModel.deviceFov
        return TransformationParams(
            perspectiveWidth = pWidth,
            perspectiveHeight = pHeight,
            outputWidth = 2 * (imageWidth / 2),
            outputHeight = imageWidth / 2,
            antiAliasingLevel = max(viewModel.antiAliasing, 1),
            fieldOfView = fov,
            horizontalCoefficient = tan(0.5 * fov),
            verticalCoefficient = pHeight * tan(0.5 * fov) / pWidth
        )
    }
}