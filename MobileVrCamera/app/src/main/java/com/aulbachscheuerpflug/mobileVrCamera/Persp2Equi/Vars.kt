package com.aulbachscheuerpflug.mobileVrCamera.Persp2Equi

class Vars {
    var perspwidth = 0
    var perspheight = 0
    var outwidth = 4000
    var antialias = 2                           //1 = no antialias, 3 = as high as necessary
    var antialias2 = 4
    var fov = Math.PI / 180 * 100               //horizontal FOV
    var ntransform = 0

    var outheight = 2000
    var inputformat = 1
    var dv: Double = 0.0
    var dh: Double = 0.0
    var debug = 0

    init {
        perspwidth = 0
        perspheight = 0
        outwidth = 2048
        antialias = 2
        antialias2 = 4
        fov = (Math.PI / 180) * 100
        ntransform = 0

        outheight = 1024
        inputformat = 3
        dv = 0.0
        dh = 0.0
        debug = 0
    }

}