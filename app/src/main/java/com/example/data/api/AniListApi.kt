package com.example.data.api

import com.example.data.model.AniListResponse
import com.example.data.model.GraphQLRequest
import com.example.data.model.MediaContainer
import com.example.data.model.MediaListCollectionContainer
import com.example.data.model.PageContainer
import com.example.data.model.SaveMediaListEntryContainer
import com.example.data.model.ViewerContainer
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface AniListApiService {
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("/")
    suspend fun queryPage(
        @Body request: GraphQLRequest
    ): AniListResponse<PageContainer>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("/")
    suspend fun queryMedia(
        @Body request: GraphQLRequest
    ): AniListResponse<MediaContainer>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("/")
    suspend fun queryViewer(
        @Body request: GraphQLRequest
    ): AniListResponse<ViewerContainer>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("/")
    suspend fun queryMediaListCollection(
        @Body request: GraphQLRequest
    ): AniListResponse<MediaListCollectionContainer>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("/")
    suspend fun updateProgress(
        @Body request: GraphQLRequest
    ): AniListResponse<SaveMediaListEntryContainer>
}

object AniListNetwork {
    private const val BASE_URL = "https://graphql.anilist.co/"

    @Volatile
    var currentToken: String? = null

    var onAuthExpired: (() -> Unit)? = null

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()

        val token = currentToken
        if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }

        val response = chain.proceed(builder.build())
        if (response.code == 401) {
            currentToken = null
            onAuthExpired?.invoke()
        }
        response
    }

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val apiService: AniListApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(AniListApiService::class.java)
}
