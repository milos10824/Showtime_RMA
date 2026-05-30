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
import rs.edu.raf.showtime.di.initShowtimeKoin
import rs.edu.raf.showtime.feature.auth.AuthEffect
import rs.edu.raf.showtime.feature.auth.AuthScreen
import rs.edu.raf.showtime.feature.auth.AuthViewModel
import rs.edu.raf.showtime.feature.catalog.MovieCatalogScreen
import rs.edu.raf.showtime.feature.catalog.MovieCatalogViewModel
import rs.edu.raf.showtime.feature.home.HomeScreen
import rs.edu.raf.showtime.navigation.ShowtimeRoute

@Composable
fun ShowtimeApp() {
    val scope = rememberCoroutineScope()
    val koin = remember { initShowtimeKoin() }

    val authRepository = remember {
        koin.get<AuthRepository>()
    }

    val movieRepository = remember {
        koin.get<MovieRepository>()
    }

    val authViewModel = remember {
        AuthViewModel(
            repository = authRepository,
            scope = scope,
        )
    }

    val movieCatalogViewModel = remember {
        MovieCatalogViewModel(
            repository = movieRepository,
            scope = scope,
        )
    }

    val authData by authRepository.authData.collectAsState(AuthData())

    var route by remember {
        mutableStateOf(
            if (authData.isLoggedIn) {
                ShowtimeRoute.HOME
            } else {
                ShowtimeRoute.AUTH
            }
        )
    }

    LaunchedEffect(authData.isLoggedIn) {
        if (!authData.isLoggedIn) {
            route = ShowtimeRoute.AUTH
        }
    }

    LaunchedEffect(authViewModel) {
        authViewModel.effect.collect { effect ->
            when (effect) {
                AuthEffect.OpenHome -> route = ShowtimeRoute.HOME
            }
        }
    }

    when (route) {
        ShowtimeRoute.AUTH -> {
            AuthScreen(
                state = authViewModel.state.collectAsState().value,
                onIntent = authViewModel::onIntent,
            )
        }

        ShowtimeRoute.HOME -> {
            HomeScreen(
                authData = authData,
                onOpenCatalog = {
                    route = ShowtimeRoute.CATALOG
                },
                onLogout = {
                    scope.launch {
                        authRepository.logout()
                        movieRepository.clearUserMovieData()
                        route = ShowtimeRoute.AUTH
                    }
                },
            )
        }

        ShowtimeRoute.CATALOG -> {
            MovieCatalogScreen(
                state = movieCatalogViewModel.state.collectAsState().value,
                onIntent = movieCatalogViewModel::onIntent,
                onBack = {
                    route = ShowtimeRoute.HOME
                },
            )
        }
    }
}
