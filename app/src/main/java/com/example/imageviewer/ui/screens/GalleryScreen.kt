package com.example.imageviewer.ui.screens

import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.imageviewer.ui.components.ImageCard
import com.example.imageviewer.util.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ImageItem(
    val uri: Uri,
    val name: String,
    val folderName: String,
    val dateTaken: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onImageClick: (Uri, String) -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(PermissionHelper.hasStoragePermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = permissions.values.all { it }
    }

    var images by remember { mutableStateOf<List<ImageItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var folders by remember { mutableStateOf<List<String>>(emptyList()) }

    // Load images
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            withContext(Dispatchers.IO) {
                val loadedImages = loadImagesFromDevice(context)
                val folderList = loadedImages
                    .map { it.folderName }
                    .distinct()
                    .sorted()

                withContext(Dispatchers.Main) {
                    images = loadedImages
                    folders = folderList
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedFolder != null) selectedFolder!! else "图片库",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    if (folders.isNotEmpty()) {
                        var showFolderMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showFolderMenu = true }) {
                            Icon(Icons.Default.Folder, contentDescription = "选择文件夹")
                        }
                        DropdownMenu(
                            expanded = showFolderMenu,
                            onDismissRequest = { showFolderMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("全部图片") },
                                onClick = {
                                    selectedFolder = null
                                    showFolderMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                }
                            )
                            folders.forEach { folder ->
                                DropdownMenuItem(
                                    text = { Text(folder) },
                                    onClick = {
                                        selectedFolder = folder
                                        showFolderMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Folder, contentDescription = null)
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        when {
            !hasPermission -> {
                // Permission request screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "需要存储权限",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "为了显示您的图片，我们需要访问您设备上的图片文件。",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            permissionLauncher.launch(
                                PermissionHelper.getRequiredPermissions()
                            )
                        }
                    ) {
                        Text("授予权限")
                    }
                }
            }
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            images.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "未找到图片",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                val filteredImages = if (selectedFolder != null) {
                    images.filter { it.folderName == selectedFolder }
                } else {
                    images
                }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = paddingValues.calculateTopPadding() + 8.dp,
                        bottom = paddingValues.calculateBottomPadding() + 8.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredImages,
                        key = { it.uri.toString() }
                    ) { image ->
                        ImageCard(
                            imageUri = image.uri.toString(),
                            imageName = image.name,
                            onClick = { onImageClick(image.uri, image.folderName) }
                        )
                    }
                }
            }
        }
    }
}

private fun loadImagesFromDevice(context: android.content.Context): List<ImageItem> {
    val images = mutableListOf<ImageItem>()

    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Images.Media.DATE_TAKEN
    )

    val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

    val cursor = context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        sortOrder
    )

    cursor?.use {
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        val bucketColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        val dateColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)

        while (it.moveToNext()) {
            val id = it.getLong(idColumn)
            val name = it.getString(nameColumn) ?: "unknown"
            val folderName = it.getString(bucketColumn) ?: "Unknown"
            val dateTaken = it.getLong(dateColumn)

            val contentUri = Uri.withAppendedPath(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id.toString()
            )

            images.add(
                ImageItem(
                    uri = contentUri,
                    name = name,
                    folderName = folderName,
                    dateTaken = dateTaken
                )
            )
        }
    }

    return images
}
