package com.aulbachscheuerpflug.mobileVrCamera.utils

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer

fun playSystemSound(
    context: Context, soundId: Int = AudioManager.FX_KEY_CLICK, volume: Float = 0.3f
) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    audioManager.playSoundEffect(soundId, volume)
}

fun playCustomSound(context: Context, resId: Int) {
    val mediaPlayer = MediaPlayer.create(context, resId)
    mediaPlayer.setOnCompletionListener { mp -> mp.release() }
    mediaPlayer.setVolume(0.3f, 0.3f)
    mediaPlayer.start()
}