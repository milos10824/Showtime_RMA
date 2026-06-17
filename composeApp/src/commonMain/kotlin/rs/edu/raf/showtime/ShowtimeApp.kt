package rs.edu.raf.showtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.showtime.core.ui.AppScreen
import rs.edu.raf.showtime.core.ui.LoadingContent
import rs.edu.raf.showtime.feature.auth.AuthEffect
import rs.edu.raf.showtime.feature.auth.AuthScreen
import rs.edu.raf.showtime.feature.auth.AuthViewModel
import rs.edu.raf.showtime.feature.catalog.MovieCatalogScreen
import rs.edu.raf.showtime.feature.catalog.MovieCatalogEffect
import rs.edu.raf.showtime.feature.catalog.MovieCatalogViewModel
import rs.edu.raf.showtime.feature.details.MovieDetailsEffect
import rs.edu.raf.showtime.feature.details.MovieDetailsScreen
import rs.edu.raf.showtime.feature.details.MovieDetailsViewModel
import rs.edu.raf.showtime.feature.home.HomeEffect
import rs.edu.raf.showtime.feature.home.HomeScreen
import rs.edu.raf.showtime.feature.home.HomeViewModel
import rs.edu.raf.showtime.feature.profile.ProfileScreen
import rs.edu.raf.showtime.feature.profile.ProfileEffect
import rs.edu.raf.showtime.feature.profile.ProfileViewModel
import rs.edu.raf.showtime.feature.quiz.QuizEffect
import rs.edu.raf.showtime.feature.quiz.QuizScreen
import rs.edu.raf.showtime.feature.quiz.QuizViewModel
import rs.edu.raf.showtime.feature.saved.SavedMoviesEffect
import rs.edu.raf.showtime.feature.saved.SavedMoviesScreen
import rs.edu.raf.showtime.feature.saved.SavedMoviesViewModel
import rs.edu.raf.showtime.feature.session.AppSessionEffect
import rs.edu.raf.showtime.feature.session.AppSessionViewModel
import rs.edu.raf.showtime.navigation.MOVIE_ID
import rs.edu.raf.showtime.navigation.SAVED_TYPE
import rs.edu.raf.showtime.navigation.ShowtimeRoute

@Composable
fun ShowtimeApp() {
    val navController = rememberNavController()
    val sessionViewModel = koinViewModel<AppSessionViewModel>()
    val sessionState by sessionViewModel.state.collectAsState()

    LaunchedEffect(sessionViewModel) {
        sessionViewModel.effect.collect { effect ->
            when (effect) {
                AppSessionEffect.OpenAuth -> navController.navigateToAuth()
            }
        }
    }

    if (sessionState.isLoading) {
        AppScreen {
            LoadingContent()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = initialShowtimeRoute(sessionState.isLoggedIn),
    ) {
        composable(route = ShowtimeRoute.AUTH) {
            val viewModel = koinViewModel<AuthViewModel>()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(viewModel) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        AuthEffect.OpenHome -> navController.navigateToHome()
                    }
                }
            }

            AuthScreen(
                state = state,
                onIntent = viewModel::onIntent,
            )
        }

        composable(route = ShowtimeRoute.HOME) {
            val viewModel = koinViewModel<HomeViewModel>()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(viewModel) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        HomeEffect.OpenCatalog -> navController.navigate(ShowtimeRoute.CATALOG)
                        is HomeEffect.OpenSaved -> navController.navigate(ShowtimeRoute.saved(effect.type))
                        HomeEffect.OpenProfile -> navController.navigate(ShowtimeRoute.PROFILE)
                        HomeEffect.OpenQuiz -> navController.navigate(ShowtimeRoute.QUIZ)
                    }
                }
            }

            HomeScreen(
                state = state,
                onIntent = viewModel::onIntent,
            )
        }

        composable(route = ShowtimeRoute.CATALOG) {
            val viewModel = koinViewModel<MovieCatalogViewModel>()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(viewModel) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is MovieCatalogEffect.OpenMovieDetails -> {
                            navController.navigate(ShowtimeRoute.details(effect.movieId))
                        }
                        MovieCatalogEffect.Close -> navController.navigateUp()
                    }
                }
            }

            MovieCatalogScreen(
                state = state,
                onIntent = viewModel::onIntent,
            )
        }

        composable(
            route = ShowtimeRoute.SAVED,
            arguments = listOf(
                navArgument(SAVED_TYPE) {
                    type = NavType.StringType
                    nullable = false
                },
            ),
        ) {
            val viewModel = koinViewModel<SavedMoviesViewModel>()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(viewModel) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is SavedMoviesEffect.OpenMovieDetails -> {
                            navController.navigate(ShowtimeRoute.details(effect.movieId))
                        }
                        SavedMoviesEffect.Close -> navController.navigateUp()
                    }
                }
            }

            SavedMoviesScreen(
                state = state,
                onIntent = viewModel::onIntent,
            )
        }

        composable(
            route = ShowtimeRoute.DETAILS,
            arguments = listOf(
                navArgument(MOVIE_ID) {
                    type = NavType.StringType
                    nullable = false
                },
            ),
        ) {
            val viewModel = koinViewModel<MovieDetailsViewModel>()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(viewModel) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        MovieDetailsEffect.Close -> navController.navigateUp()
                    }
                }
            }

            MovieDetailsScreen(
                state = state,
                onIntent = viewModel::onIntent,
            )
        }

        composable(route = ShowtimeRoute.PROFILE) {
            val viewModel = koinViewModel<ProfileViewModel>()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(viewModel) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        ProfileEffect.Close -> navController.navigateUp()
                    }
                }
            }

            ProfileScreen(
                state = state,
                onIntent = viewModel::onIntent,
            )
        }

        composable(route = ShowtimeRoute.QUIZ) {
            val viewModel = koinViewModel<QuizViewModel>()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(viewModel) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        QuizEffect.Close -> navController.navigateUp()
                    }
                }
            }

            QuizScreen(
                state = state,
                onIntent = viewModel::onIntent,
            )
        }
    }
}

internal fun initialShowtimeRoute(isLoggedIn: Boolean): String {
    return if (isLoggedIn) ShowtimeRoute.HOME else ShowtimeRoute.AUTH
}

private fun NavController.navigateToHome() {
    navigate(ShowtimeRoute.HOME) {
        popUpTo(ShowtimeRoute.AUTH) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

private fun NavController.navigateToAuth() {
    navigate(ShowtimeRoute.AUTH) {
        popUpTo(0)
        launchSingleTop = true
    }
}
