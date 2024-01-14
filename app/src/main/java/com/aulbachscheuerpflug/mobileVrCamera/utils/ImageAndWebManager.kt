package com.aulbachscheuerpflug.mobileVrCamera.utils

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore

fun loadImagesFromDirectory(context: Context): List<Uri> {
    val contentResolver = context.contentResolver
    val images = mutableListOf<Uri>()

    val projection = arrayOf(MediaStore.Images.Media._ID)

    contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        null,
    )?.use { cursor ->

        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )
            images.add(contentUri)
        }
    }
    return images
}

fun deleteImage(context: Context, uri: Uri) {
    context.contentResolver.delete(
        uri,
        "${MediaStore.Images.Media._ID} = ?",
        arrayOf(ContentUris.parseId(uri).toString())
    )
}

fun shareImage(context: Context, uri: Uri) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        type = "image/*"
    }
    context.startActivity(Intent.createChooser(sendIntent, null))
}

fun openWebsite(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}