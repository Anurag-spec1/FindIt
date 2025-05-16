package com.example.cloudinary


import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.Call
import com.google.gson.annotations.SerializedName

object RetrofitClient {
    private const val BASE_URL = "https://api.cloudinary.com/v1_1/dukncxhvq/"

    private val client = OkHttpClient.Builder().addInterceptor { chain ->
        val request = chain.request().newBuilder()
            .header("Authorization", Credentials.basic("231725269526121", "MvS8dpXqR62gyMvuJowom9Z40z4"))
            .build()
        chain.proceed(request)
    }.build()

    val api: CloudinaryApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Attach authentication client
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CloudinaryApi::class.java)
    }
}

interface CloudinaryApi {
    @GET("resources/image/upload") // Correct endpoint
    fun getImages(): Call<CloudinaryResponse> // No need for `authHeader`
}



