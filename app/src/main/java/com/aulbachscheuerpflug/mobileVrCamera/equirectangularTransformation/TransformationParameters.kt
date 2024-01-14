package com.aulbachscheuerpflug.mobileVrCamera.equirectangularTransformation

data class TransformationParams(
    var perspectiveWidth: Int,
    var perspectiveHeight: Int,
    var outputWidth: Int,
    var outputHeight: Int,
    var antiAliasingLevel: Int,
    var fieldOfView: Double,
    var antiAliasingSquare: Int = antiAliasingLevel * antiAliasingLevel,
    var inputFormat: Int = 3,
    var debugMode: Int = 0,
    var horizontalCoefficient: Double,
    var verticalCoefficient: Double
)