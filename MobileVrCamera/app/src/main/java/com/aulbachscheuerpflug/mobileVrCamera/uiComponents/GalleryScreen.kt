package com.aulbachscheuerpflug.mobileVrCamera.uiComponents

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aulbachscheuerpflug.mobileVrCamera.GALLERY_ALERT_TITLE
import com.aulbachscheuerpflug.mobileVrCamera.MainViewModel
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.ImmerVrBlue
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.Shapes
import com.aulbachscheuerpflug.mobileVrCamera.utils.deleteImage
import com.aulbachscheuerpflug.mobileVrCamera.utils.loadImagesFromDirectory
import com.aulbachscheuerpflug.mobileVrCamera.utils.playSystemSound
import com.aulbachscheuerpflug.mobileVrCamera.utils.shareImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryScreen(
    context: Context,
    onCloseButton: () -> Unit,
    rotation: Int,
    mainViewModel: MainViewModel,
) {
    val imagePaths = remember { mutableStateOf(listOf<Uri>()) }
    val selectedImage = remember { mutableStateOf<Uri?>(null) }
    val showDialog = remember { mutableStateOf(false) }
    val longPressImage = remember { mutableStateOf<Uri?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        imagePaths.value = loadImagesFromDirectory(context)
    }

    TopBar(
        onCloseButton = onCloseButton,
        rotation = rotation,
        mainViewModel = mainViewModel,
    )

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            text = {
                Column {
                    Text(
                        GALLERY_ALERT_TITLE,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    ) {
                        AsyncImage(
                            model = longPressImage.value,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            },
            buttons = {
                Row(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            playSystemSound(context)
                            coroutineScope.launch {
                                deleteImage(context, longPressImage.value!!)
                                imagePaths.value = loadImagesFromDirectory(context)
                                showDialog.value = false
                                selectedImage.value = null
                            }
                        },
                        modifier = Modifier.padding(end = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = ImmerVrBlue
                        )
                    ) {
                        Text("Delete")
                    }
                    Button(
                        onClick = {
                            playSystemSound(context)
                            shareImage(context, longPressImage.value!!)
                            showDialog.value = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = ImmerVrBlue
                        )
                    ) {
                        Text("Share...")
                    }
                }
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Transparent)
            .padding(top = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selectedImage.value == null) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(imagePaths.value) { imagePath ->
                    AsyncImage(
                        model = imagePath,
                        contentDescription = null,
                        modifier = Modifier
                            .size(128.dp)
                            .padding(3.dp)
                            .clip(Shapes.medium)
                            .clickable {
                                playSystemSound(context)
                                selectedImage.value = imagePath
                            }
                            .combinedClickable(
                                onClick = {
                                    playSystemSound(context)
                                    selectedImage.value = imagePath
                                },
                                onLongClick = {
                                    playSystemSound(context)
                                    longPressImage.value = imagePath
                                    showDialog.value = true
                                }
                            ),
                        contentScale = ContentScale.FillWidth,
                    )
                }
            }
        } else {
            AsyncImage(
                model = selectedImage.value!!,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            playSystemSound(context)
                            selectedImage.value = null
                        },
                        onLongClick = {
                            playSystemSound(context)
                            longPressImage.value = selectedImage.value
                            showDialog.value = true
                        }
                    ),
            )
        }
    }
}

@Preview
@Composable
fun GalleryScreenPreview() {
    val dummyMainViewModel = MainViewModel()
    GalleryScreen(
        context = LocalContext.current,
        rotation = 0,
        onCloseButton = {},
        mainViewModel = dummyMainViewModel
    )
}