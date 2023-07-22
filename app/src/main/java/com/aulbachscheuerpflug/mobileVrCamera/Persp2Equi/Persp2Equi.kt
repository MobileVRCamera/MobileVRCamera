package com.aulbachscheuerpflug.mobileVrCamera.Persp2Equi

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import com.aulbachscheuerpflug.mobileVrCamera.MainViewModel
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

//this is our Kotlin adaptation of Paul Bourke`s Perspective to Equirectangular algorithm
//http://paulbourke.net/panorama/sphere2persp/
class Persp2Equi {
    var vars: Vars = Vars()
    var u: Int = 0
    var v: Int = 0

    fun persp2Equi(
        bitmap: Bitmap,
        width: Int,
        mainViewModel: MainViewModel,
    ): Bitmap {
        var x: Double = 0.0
        var y: Double = 0.0

        var latitude: Double = 0.0
        var longitude: Double = 0.0

        var sum: Vector3 = Vector3(0, 0, 0)
        var zero: Vector3 = Vector3(0, 0, 0)


        //Multiply to ensure width is even
        vars.outwidth = 2 * (width / 2)
        vars.outheight = vars.outwidth / 2

        vars.perspheight = bitmap.height
        vars.perspwidth = bitmap.width

        vars.antialias = mainViewModel.antiAliasing
        if (vars.antialias < 1) {
            vars.antialias = 1
        }
        vars.antialias2 = vars.antialias * vars.antialias

        vars.fov = Math.PI / 180 * mainViewModel.deviceFov

        vars.inputformat = 3

        //Create Bitmaps for underlying Canvas an the equirectangular image
        var canvasBitmap =
            Bitmap.createBitmap(vars.outwidth, vars.outheight, Bitmap.Config.ARGB_8888)
        var spherical = Bitmap.createBitmap(vars.outwidth, vars.outheight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(canvasBitmap)
        //Set Color of Canvas to black so it gets filtered out in VR
        canvas.drawColor(Color.BLACK)

        //half frustum width
        vars.dh = tan(0.5 * vars.fov)
        //half frustum height
        vars.dv = vars.perspheight * vars.dh / vars.perspwidth

        //Form the equirectangular one image at a time
        for (j in 0 until vars.outheight) {
            for (i in 0 until vars.outwidth) {
                sum.r = zero.r
                sum.g = zero.g
                sum.b = zero.b
                //Antialiasing loops
                for (ai in 0 until vars.antialias) {
                    for (aj in 0 until vars.antialias) {
                        //Normalized coordinates
                        x = 2 * (i + ai / vars.antialias.toDouble()) / vars.outwidth.toDouble() - 1
                        y = 2 * (j + aj / vars.antialias.toDouble()) / vars.outheight.toDouble() - 1

                        longitude = x * Math.PI
                        latitude = y * 0.5 * Math.PI

                        // Find the corresponding pixel in the perspective image
                        // Sum over the supersampling set
                        if (FindPerspPixel(latitude, longitude)) {
                            sum.r = Color.red(bitmap.getPixel(u, v))
                            sum.g = Color.green(bitmap.getPixel(u, v))
                            sum.b = Color.blue(bitmap.getPixel(u, v))
                        } else {
                            sum.r = 0
                            sum.g = 0
                            sum.b = 0
                        }
                    }
                }
                spherical.setPixel(
                    i,
                    j,
                    Color.rgb(
                        sum.r ,
                        sum.g ,
                        sum.b
                    )
                )
            }
            mainViewModel.persp2EquiProgression = ((j.toFloat() / vars.outheight.toFloat()) * 100f).roundToInt()
        }
        u = 0
        v = 0
        canvas.drawBitmap(spherical, 0.0f, 0.0f, null)

        return spherical

    }


    fun FindPerspPixel(latitude: Double, longitude: Double): Boolean {
        var mu: Double = 0.0
        var x: Double = 0.0
        var z: Double = 0.0
        var p: XYZ = XYZ()
        var q: XYZ = XYZ()

        u = 0
        v = 0

        // p is the ray from the camera position into the scene
        p.x = cos(latitude) * sin(longitude)
        p.y = cos(latitude) * cos(longitude)
        p.z = sin(latitude)

        if (p.y <= 0) {
            return false
        }

        // Intersection point
        mu = 1.0 / p.y
        x = mu * p.x
        z = mu * p.z

        // Is the intersection in the frustum
        if (x <= -vars.dh || x >= vars.dh) {
            return false
        }
        if (z <= -vars.dv || z >= vars.dv) {
            return false
        }

        u = (vars.perspwidth * 0.5 * (x + vars.dh) / vars.dh).toInt()
        v = (vars.perspheight * 0.5 * (z + vars.dv) / vars.dv).toInt()

        return true
    }
}