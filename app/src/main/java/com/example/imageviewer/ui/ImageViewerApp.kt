package com.example.imageviewer.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.imageviewer.ui.screens.GalleryScreen
import com.example.imageviewer.ui.screens.ViewerScreen

object NavRoutes {
    const val GALLERY = "gallery"
    const val VIEWER = "viewer/{imageUri}/{folderName}"

    fun viewerRoute(imageUri: String, folderName: String): String {
        val encodedUri = Uri.encode(imageUri)
        val encodedFolder = Uri.encode(folderName)
        return "viewer/$encodedUri/$encodedFolder"
    }
}

@Composable
fun ImageViewerApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.GALLERY
    ) {
        composable(NavRoutes.GALLERY) {
            GalleryScreen(
                onImageClick = { uri, folder ->
                    navController.navigate(
                        NavRoutes.viewerRoute(uri.toString(), folder)
                    )
                }
            )
        }

        composable(
            route = NavRoutes.VIEWER,
            arguments = listOf(
                navArgument("imageUri") { type = NavType.StringType },
                navArgument("folderName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val imageUri = Uri.decode(backStackEntry.arguments?.getString("imageUri") ?: "")
            val folderName = Uri.decode(backStackEntry.arguments?.getString("folderName") ?: "")
            ViewerScreen(
                imageUri = imageUri,
                folderName = folderName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
