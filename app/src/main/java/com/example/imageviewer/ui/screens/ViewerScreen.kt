package com.example.imageviewer.ui.screens

import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(
    imageUri: String,
    folderName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Load all images from the same folder for swipe navigation
    var folderImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(-1) }

    // Zoom and pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // UI visibility
    var isUiVisible by remember { mutableStateOf(true) }

    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            val images = loadFolderImages(context, folderName)
            withContext(Dispatchers.Main) {
                folderImages = images
                currentIndex = images.indexOf(imageUri).coerceAtLeast(0)
            }
        }
    }

    // Reset zoom when image changes
    LaunchedEffect(currentIndex) {
        scale = 1f
        offset = Offset.Zero
    }

    BackHandler(enabled = scale <= 1.01f) {
        onBack()
    }

    // Derive current image URI from state
    val currentImageUri = if (currentIndex in folderImages.indices)
        folderImages[currentIndex] else imageUri

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = isUiVisible,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = if (folderImages.isNotEmpty())
                                "${currentIndex + 1} / ${folderImages.size}"
                            else "图片浏览",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    },
                    actions = {
                        // Share button
                        IconButton(onClick = {
                            shareImage(context, currentImageUri)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "分享")
                        }
                        // Delete button
                        IconButton(onClick = {
                            val uriToDelete = currentImageUri
                            deleteImage(context, uriToDelete)
                            if (folderImages.size <= 1) {
                                onBack()
                            } else {
                                val mutableList = folderImages.toMutableList()
                                mutableList.removeAt(currentIndex)
                                folderImages = mutableList
                                if (currentIndex >= folderImages.size) {
                                    currentIndex = folderImages.size - 1
                                }
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                        // Info button
                        var showInfo by remember { mutableStateOf(false) }
                        IconButton(onClick = { showInfo = true }) {
                            Icon(Icons.Default.Info, contentDescription = "详情")
                        }
                        if (showInfo) {
                            ImageInfoDialog(
                                imageUri = currentImageUri,
                                onDismiss = { showInfo = false }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isUiVisible && folderImages.size > 1,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                BottomAppBar(
                    containerColor = Color.Black.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        folderImages.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(if (index == currentIndex) 10.dp else 8.dp)
                                    .background(
                                        if (index == currentIndex) Color.White
                                        else Color.White.copy(alpha = 0.5f),
                                        shape = MaterialTheme.shapes.extraLarge
                                    )
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (currentIndex >= 0 && currentIndex < folderImages.size) {
                // Combined gesture handler: zoom/pan + tap + swipe
                SubcomposeAsyncImage(
                    model = currentImageUri,
                    contentDescription = "图片浏览",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        }
                        .pointerInput(currentIndex) {
                            detectTransformGestures { centroid, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(1f, 5f)

                                if (newScale > 1f) {
                                    val scaleChange = newScale / scale
                                    offset = Offset(
                                        x = offset.x * scaleChange + pan.x,
                                        y = offset.y * scaleChange + pan.y
                                    )
                                    val maxOffsetX = (newScale - 1f) * 500f
                                    val maxOffsetY = (newScale - 1f) * 500f
                                    offset = Offset(
                                        x = offset.x.coerceIn(-maxOffsetX, maxOffsetX),
                                        y = offset.y.coerceIn(-maxOffsetY, maxOffsetY)
                                    )
                                } else {
                                    offset = Offset.Zero
                                }
                                scale = newScale
                            }
                        }
                        .pointerInput(folderImages.size, currentIndex) {
                            detectTapGestures(
                                onTap = {
                                    isUiVisible = !isUiVisible
                                },
                                onHorizontalDragEnd = {
                                    if (scale <= 1.01f) {
                                        val swipeThreshold = 150f
                                        if (it < -swipeThreshold && currentIndex < folderImages.size - 1) {
                                            currentIndex++
                                        } else if (it > swipeThreshold && currentIndex > 0) {
                                            currentIndex--
                                        }
                                    }
                                }
                            )
                        },
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.BrokenImage,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "加载失败",
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ImageInfoDialog(
    imageUri: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var imageInfo by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            imageInfo = getImageInfo(context, imageUri)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("图片详情") },
        text = {
            Column {
                if (imageInfo.isEmpty()) {
                    Text("加载中…", style = MaterialTheme.typography.bodyMedium)
                } else {
                    imageInfo.forEach { (key, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "$key: ",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}

private fun loadFolderImages(context: android.content.Context, folderName: String): List<String> {
    val images = mutableListOf<String>()

    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Images.Media.DATE_TAKEN
    )

    val selection = if (folderName.isNotEmpty()) {
        "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
    } else null

    val selectionArgs = if (folderName.isNotEmpty()) {
        arrayOf(folderName)
    } else null

    val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

    val cursor = context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        sortOrder
    )

    cursor?.use {
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        while (it.moveToNext()) {
            val id = it.getLong(idColumn)
            val uri = Uri.withAppendedPath(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id.toString()
            )
            images.add(uri.toString())
        }
    }

    return images
}

private fun getImageInfo(
    context: android.content.Context,
    imageUri: String
): Map<String, String> {
    val info = mutableMapOf<String, String>()

    try {
        val uri = Uri.parse(imageUri)
        val projection = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)
                val widthIdx = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH)
                val heightIdx = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT)
                val dateIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
                val mimeIdx = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)
                val bucketIdx = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

                if (nameIdx >= 0) info["文件名"] = cursor.getString(nameIdx) ?: "未知"
                if (sizeIdx >= 0) {
                    val size = cursor.getLong(sizeIdx)
                    info["大小"] = when {
                        size < 1024 -> "${size} B"
                        size < 1024 * 1024 -> "${size / 1024} KB"
                        else -> "%.1f MB".format(size / (1024.0 * 1024.0))
                    }
                }
                if (widthIdx >= 0 && heightIdx >= 0) {
                    info["尺寸"] = "${cursor.getInt(widthIdx)} × ${cursor.getInt(heightIdx)} px"
                }
                if (dateIdx >= 0) {
                    val date = java.text.SimpleDateFormat(
                        "yyyy-MM-dd HH:mm",
                        java.util.Locale.getDefault()
                    ).format(java.util.Date(cursor.getLong(dateIdx)))
                    info["拍摄日期"] = date
                }
                if (mimeIdx >= 0) info["类型"] = cursor.getString(mimeIdx) ?: "未知"
                if (bucketIdx >= 0) info["文件夹"] = cursor.getString(bucketIdx) ?: "未知"
            }
        }
    } catch (e: Exception) {
        info["错误"] = e.message ?: "无法获取图片信息"
    }

    return info
}

private fun shareImage(context: android.content.Context, imageUri: String) {
    try {
        val uri = Uri.parse(imageUri)
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            android.content.Intent.createChooser(shareIntent, "分享图片")
        )
    } catch (e: Exception) {
        android.widget.Toast.makeText(
            context,
            "分享失败: ${e.message}",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}

private fun deleteImage(context: android.content.Context, imageUri: String) {
    try {
        val uri = Uri.parse(imageUri)
        val deleted = context.contentResolver.delete(uri, null, null)
        if (deleted > 0) {
            android.widget.Toast.makeText(
                context,
                "图片已删除",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    } catch (e: Exception) {
        android.widget.Toast.makeText(
            context,
            "删除失败: ${e.message}",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}
