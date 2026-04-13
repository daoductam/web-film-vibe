package com.tamdao.cinestream.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String? = null,
    val icon: ImageVector? = null
) {
    object Home : Screen("home", "Trang chủ", Icons.Default.Home)
    object Search : Screen("search", "Tìm kiếm", Icons.Default.Search)
    object Library : Screen("library", "Thư viện", Icons.Default.VideoLibrary)
    object Profile : Screen("profile", "Cá nhân", Icons.Default.Person)
    
    object MovieList : Screen("movie_list/{title}/{type}?category={category}") {
        fun createRoute(title: String, type: String, category: String? = null) = 
            "movie_list/$title/$type" + (category?.let { "?category=$it" } ?: "")
    }

    object MovieDetail : Screen("movie_detail/{slug}") {
        fun createRoute(slug: String) = "movie_detail/$slug"
    }

    object Player : Screen("player/{slug}/{episodeSlug}") {
        fun createRoute(slug: String, episodeSlug: String) = "player/$slug/$episodeSlug"
    }

    // Auth & Profile
    object Login : Screen("login")
    object Register : Screen("register")
    object EditProfile : Screen("edit_profile")
    object ChangePassword : Screen("change_password")
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Search,
    Screen.Library,
    Screen.Profile
)
