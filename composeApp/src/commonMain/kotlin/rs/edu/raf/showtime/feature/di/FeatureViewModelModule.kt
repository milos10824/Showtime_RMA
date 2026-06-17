package rs.edu.raf.showtime.feature.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import rs.edu.raf.showtime.feature.auth.AuthViewModel
import rs.edu.raf.showtime.feature.catalog.MovieCatalogViewModel
import rs.edu.raf.showtime.feature.details.MovieDetailsViewModel
import rs.edu.raf.showtime.feature.home.HomeViewModel
import rs.edu.raf.showtime.feature.profile.ProfileViewModel
import rs.edu.raf.showtime.feature.quiz.QuizViewModel
import rs.edu.raf.showtime.feature.saved.SavedMoviesViewModel
import rs.edu.raf.showtime.feature.session.AppSessionViewModel

val featureViewModelModule = module {
    viewModelOf(::AuthViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::MovieCatalogViewModel)
    viewModelOf(::MovieDetailsViewModel)
    viewModelOf(::SavedMoviesViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::QuizViewModel)
    viewModelOf(::AppSessionViewModel)
}
