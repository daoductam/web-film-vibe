package com.tamdao.cinestream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tamdao.cinestream.core.components.CineBottomBar
import com.tamdao.cinestream.core.navigation.Screen
import com.tamdao.cinestream.feature.detail.MovieDetailScreen
import com.tamdao.cinestream.feature.home.HomeScreen
import com.tamdao.cinestream.feature.library.LibraryScreen
import com.tamdao.cinestream.feature.player.VideoPlayerScreen
import com.tamdao.cinestream.feature.search.SearchScreen
import com.tamdao.cinestream.feature.profile.*
import com.tamdao.cinestream.feature.movielist.MovieListScreen
import com.tamdao.cinestream.ui.theme.CineStreamTheme
import com.tamdao.cinestream.ui.theme.Obsidian
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Cài đặt Splash Screen trước khi gọi super.onCreate
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            CineStreamTheme {
                val navController = rememberNavController()

                Scaffold(
                    bottomBar = { CineBottomBar(navController) },
                    containerColor = Obsidian
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                onMovieClick = { slug ->
                                    navController.navigate(Screen.MovieDetail.createRoute(slug))
                                },
                                onSeeAllClick = { title, type ->
                                    navController.navigate(Screen.MovieList.createRoute(title, type))
                                },
                                onSearchClick = {
                                    navController.navigate(Screen.Search.route)
                                }
                            )
                        }

                        composable(Screen.Search.route) {
                            SearchScreen(
                                onMovieClick = { slug ->
                                    navController.navigate(Screen.MovieDetail.createRoute(slug))
                                },
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        
                        composable(Screen.Library.route) {
                            LibraryScreen(
                                onMovieClick = { slug ->
                                    navController.navigate(Screen.MovieDetail.createRoute(slug))
                                }
                            )
                        }
                        
                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                onLoginClick = { navController.navigate(Screen.Login.route) },
                                onRegisterClick = { navController.navigate(Screen.Register.route) },
                                onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                                onChangePasswordClick = { navController.navigate(Screen.ChangePassword.route) },
                                onDownloadedMoviesClick = { navController.navigate(Screen.DownloadedMovies.route) }
                            )
                        }

                        composable(Screen.DownloadedMovies.route) {
                            DownloadedMoviesScreen(
                                onBackClick = { navController.popBackStack() },
                                onPlayClick = { movieSlug, epSlug ->
                                    navController.navigate(Screen.Player.createRoute(movieSlug, epSlug))
                                }
                            )
                        }

                        composable(Screen.Login.route) {
                            LoginScreen(
                                onBackClick = { navController.popBackStack() },
                                onSuccess = { navController.popBackStack() },
                                onRegisterClick = { 
                                    navController.navigate(Screen.Register.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.Register.route) {
                            RegisterScreen(
                                onBackClick = { navController.popBackStack() },
                                onSuccess = { navController.popBackStack() },
                                onLoginClick = {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Register.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.EditProfile.route) {
                            EditProfileScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.ChangePassword.route) {
                            ChangePasswordScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Screen.MovieList.route,
                            arguments = listOf(
                                navArgument("title") { type = NavType.StringType },
                                navArgument("type") { type = NavType.StringType },
                                navArgument("category") { 
                                    type = NavType.StringType
                                    nullable = true 
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val title = backStackEntry.arguments?.getString("title") ?: ""
                            MovieListScreen(
                                title = title,
                                onMovieClick = { slug ->
                                    navController.navigate(Screen.MovieDetail.createRoute(slug))
                                },
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Screen.MovieDetail.route,
                            arguments = listOf(navArgument("slug") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val slug = backStackEntry.arguments?.getString("slug") ?: ""
                            MovieDetailScreen(
                                slug = slug,
                                onBackClick = { navController.popBackStack() },
                                onPlayClick = { movieSlug, epSlug ->
                                    navController.navigate(Screen.Player.createRoute(movieSlug, epSlug))
                                }
                            )
                        }

                        composable(
                            route = Screen.Player.route,
                            arguments = listOf(
                                navArgument("slug") { type = NavType.StringType },
                                navArgument("episodeSlug") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val slug = backStackEntry.arguments?.getString("slug") ?: ""
                            val epSlug = backStackEntry.arguments?.getString("episodeSlug") ?: ""
                            VideoPlayerScreen(
                                movieSlug = slug,
                                episodeSlug = epSlug,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

//    override fun onUserLeaveHint() {
//        super.onUserLeaveHint()
//        // Kiểm tra nếu đang ở màn hình trình phát thì có thể vào PiP
//        // Lưu ý: Việc kiểm tra chính xác navigation state có thể phức tạp,
//        // ở đây ta có thể phát ra một tín hiệu hoặc để VideoPlayerScreen tự xử lý.
//        // Một cách đơn giản là gọi enterPictureInPictureMode() nếu SDK >= 26
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            // Chỉ vào PiP nếu đang trong quá trình xem phim (logic này thường được điều khiển bởi PlayerViewModel)
//        }
//    }


@androidx.compose.runtime.Composable
fun PlaceholderScreen(title: String, subtitle: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(Obsidian),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, color = Color.White, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
            Text(text = subtitle, color = Color.Gray, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
        }
    }
}
