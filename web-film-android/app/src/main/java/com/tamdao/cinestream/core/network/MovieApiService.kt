package com.tamdao.cinestream.core.network

import com.tamdao.cinestream.data.model.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApiService {
    
    @GET("v1/movies/latest")
    suspend fun getLatestMovies(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 24
    ): ApiResponse<PageResult<MovieDto>>

    @GET("v1/movies/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 24
    ): ApiResponse<PageResult<MovieDto>>

    @GET("v1/movies/{slug}")
    suspend fun getMovieDetail(
        @Path("slug") slug: String
    ): ApiResponse<MovieDetailDto>

    @GET("v1/movies/search")
    suspend fun searchMovies(
        @Query("q") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 24
    ): ApiResponse<PageResult<MovieDto>>

    @GET("v1/movies/filter")
    suspend fun filterMovies(
        @Query("type") type: String? = null,
        @Query("category") category: String? = null,
        @Query("country") country: String? = null,
        @Query("year") year: Int? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 24
    ): ApiResponse<PageResult<MovieDto>>
}
