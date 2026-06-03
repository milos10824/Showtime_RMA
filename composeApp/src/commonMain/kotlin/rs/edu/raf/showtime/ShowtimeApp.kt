package rs.edu.raf.showtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

import rs.edu.raf.showtime.core.auth.AuthData
import rs.edu.raf.showtime.data.auth.AuthRepository
import rs.edu.raf.showtime.data.movie.MovieRepository
import rs.edu.raf.showtime.data.quiz.QuizRepository
import rs.edu.raf.showtime.di.initShowtimeKoin
import rs.edu.raf.showtime.feature.auth.AuthEffect
import rs.edu.raf.showtime.feature.auth.AuthScreen
import rs.edu.raf.showtime.feature.auth.AuthViewModel
import rs.edu.raf.showtime.feature.catalog.MovieCatalogScreen
import rs.edu.raf.showtime.feature.catalog.MovieCatalogViewModel
import rs.edu.raf.showtime.feature.details.MovieDetailsScreen
import rs.edu.raf.showtime.feature.details.MovieDetailsViewModel
import rs.edu.raf.showtime.feature.home.HomeScreen
import rs.edu.raf.showtime.feature.profile.ProfileEffect
import rs.edu.raf.showtime.feature.profile.ProfileScreen
import rs.edu.raf.showtime.feature.profile.ProfileViewModel
import rs.edu.raf.showtime.feature.quiz.QuizScreen
import rs.edu.raf.showtime.feature.quiz.QuizViewModel
import rs.edu.raf.showtime.feature.saved.SavedListType
import rs.edu.raf.showtime.feature.saved.SavedMoviesScreen
import rs.edu.raf.showtime.feature.saved.SavedMoviesViewModel
import rs.edu.raf.showtime.navigation.ShowtimeRoute

@Composable
fun ShowtimeApp() {
    val scope = rememberCoroutineScope()
    val koin = remember { initShowtimeKoin() }

    val authRepository = remember { koin.get<AuthRepository>() }
    val movieRepository = remember { koin.get<MovieRepository>() }
    val quizRepository = remember { koin.get<QuizRepository>() }

    val authViewModel = remember { AuthViewModel(authRepository, scope) }
    val movieCatalogViewModel = remember { MovieCatalogViewModel(movieRepository, scope) }
    val favoriteMoviesViewModel = remember {
        SavedMoviesViewModel(SavedListType.FAVORITES, movieRepository, scope)
    }
    val watchlistMoviesViewModel = remember {
        SavedMoviesViewModel(SavedListType.WATCHLIST, movieRepository, scope)
    }
    val profileViewModel = remember {
        ProfileViewModel(authRepository, movieRepository, quizRepository, scope)
    }
    val quizViewModel = remember { QuizViewModel(quizRepository, scope) }

    val authData by authRepository.authData.collectAsState(AuthData())

    var route by remember {
        mutableStateOf(if (authData.isLoggedIn) ShowtimeRoute.HOME else ShowtimeRoute.AUTH)
    }

    var selectedMovieId by remember { mutableStateOf<String?>(null) }
    var detailsBackRoute by remember { mutableStateOf(ShowtimeRoute.CATALOG) }

    LaunchedEffect(authData.isLoggedIn) {
        route = if (authData.isLoggedIn) ShowtimeRoute.HOME else ShowtimeRoute.AUTH
    }

    LaunchedEffect(authViewModel) {
        authViewModel.effect.collect { effect ->
            when (effect) {
                AuthEffect.OpenHome -> route = ShowtimeRoute.HOME
            }
        }
    }

    LaunchedEffect(profileViewModel) {
        profileViewModel.effect.collect { effect ->
            when (effect) {
                ProfileEffect.LoggedOut -> route = ShowtimeRoute.AUTH
            }
        }
    }

    when (route) {
        ShowtimeRoute.AUTH -> AuthScreen(
            state = authViewModel.state.collectAsState().value,
            onIntent = authViewModel::onIntent,
        )

        ShowtimeRoute.HOME -> HomeScreen(
            authData = authData,
            onOpenCatalog = { route = ShowtimeRoute.CATALOG },
            onOpenFavorites = { route = ShowtimeRoute.FAVORITES },
            onOpenWatchlist = { route = ShowtimeRoute.WATCHLIST },
            onOpenProfile = { route = ShowtimeRoute.PROFILE },
            onOpenQuiz = { route = ShowtimeRoute.QUIZ },
            onLogout = {
                scope.launch {
                    movieRepository.clearUserMovieData()
                    authRepository.logout()
                    route = ShowtimeRoute.AUTH
                }
            },
        )

        ShowtimeRoute.CATALOG -> MovieCatalogScreen(
            state = movieCatalogViewModel.state.collectAsState().value,
            onIntent = movieCatalogViewModel::onIntent,
            onMovieClick = { movieId ->
                selectedMovieId = movieId
                detailsBackRoute = ShowtimeRoute.CATALOG
                route = ShowtimeRoute.DETAILS
            },
            onBack = { route = ShowtimeRoute.HOME },
        )

        ShowtimeRoute.FAVORITES -> SavedMoviesScreen(
            state = favoriteMoviesViewModel.state.collectAsState().value,
            onIntent = favoriteMoviesViewModel::onIntent,
            onMovieClick = { movieId ->
                selectedMovieId = movieId
                detailsBackRoute = ShowtimeRoute.FAVORITES
                route = ShowtimeRoute.DETAILS
            },
            onBack = { route = ShowtimeRoute.HOME },
        )

        ShowtimeRoute.WATCHLIST -> SavedMoviesScreen(
            state = watchlistMoviesViewModel.state.collectAsState().value,
            onIntent = watchlistMoviesViewModel::onIntent,
            onMovieClick = { movieId ->
                selectedMovieId = movieId
                detailsBackRoute = ShowtimeRoute.WATCHLIST
                route = ShowtimeRoute.DETAILS
            },
            onBack = { route = ShowtimeRoute.HOME },
        )

        ShowtimeRoute.DETAILS -> {
            val movieId = selectedMovieId
            if (movieId == null) {
                route = ShowtimeRoute.CATALOG
            } else {
                val detailsViewModel = remember(movieId) {
                    MovieDetailsViewModel(movieId, movieRepository, scope)
                }

                MovieDetailsScreen(
                    state = detailsViewModel.state.collectAsState().value,
                    onIntent = detailsViewModel::onIntent,
                    onBack = { route = detailsBackRoute },
                )
            }
        }

        ShowtimeRoute.PROFILE -> ProfileScreen(
            state = profileViewModel.state.collectAsState().value,
            onIntent = profileViewModel::onIntent,
            onBack = { route = ShowtimeRoute.HOME },
        )

        ShowtimeRoute.QUIZ -> QuizScreen(
            state = quizViewModel.state.collectAsState().value,
            onIntent = quizViewModel::onIntent,
            onBack = { route = ShowtimeRoute.HOME },
        )
    }
}
