package com.example.filmhub.data.remote

import com.example.filmhub.data.model.GenreResponse
import com.example.filmhub.data.model.MovieResponse
import com.example.filmhub.data.model.Movie
import com.example.filmhub.data.model.VideosResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TMDbApiService {

    // HU13-EP05: Obtener películas populares
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String = ApiConstants.API_KEY,
        @Query("language") language: String = "es-ES",
        @Query("page") page: Int = 1
    ): Response<MovieResponse>

    // Obtener películas mejor calificadas
    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String = ApiConstants.API_KEY,
        @Query("language") language: String = "es-ES",
        @Query("page") page: Int = 1
    ): Response<MovieResponse>

    // Obtener películas en cartelera
    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("api_key") apiKey: String = ApiConstants.API_KEY,
        @Query("language") language: String = "es-ES",
        @Query("page") page: Int = 1
    ): Response<MovieResponse>

    // Obtener próximos estrenos
    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("api_key") apiKey: String = ApiConstants.API_KEY,
        @Query("language") language: String = "es-ES",
        @Query("page") page: Int = 1
    ): Response<MovieResponse>

    // HU23-EP10: Buscar películas por título
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String = ApiConstants.API_KEY,
        @Query("language") language: String = "es-ES",
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): Response<MovieResponse>

    // HU24-EP11 y HU25-EP12: Descubrir películas con filtros
    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("api_key") apiKey: String = ApiConstants.API_KEY,
        @Query("language") language: String = "es-ES",
        @Query("page") page: Int = 1,
        @Query("year") year: Int? = null,
        @Query("with_genres") genreIds: String? = null,
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): Response<MovieResponse>

    // Obtener géneros
    @GET("genre/movie/list")
    suspend fun getGenres(
        @Query("api_key") apiKey: String = ApiConstants.API_KEY,
        @Query("language") language: String = "es-ES"
    ): Response<GenreResponse>

    // HU42-EP22: Obtener videos/trailers de una película
    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String = ApiConstants.API_KEY,
        @Query("language") language: String = "es-ES"
    ): Response<VideosResponse>

    // Obtener detalles de una película por ID
    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String = ApiConstants.API_KEY,
        @Query("language") language: String = "es-ES"
    ): Response<Movie>
}
