package com.mathia.kids.network

import com.mathia.kids.data.model.HintRequest
import com.mathia.kids.data.model.HintResponse
import com.mathia.kids.data.model.ProgressSummaryRequest
import com.mathia.kids.data.model.ProgressSummaryResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface MathIaApiService {
    @POST("api/hint")
    suspend fun getAdaptiveHint(@Body request: HintRequest): HintResponse

    @POST("api/progress-summary")
    suspend fun getProgressSummary(@Body request: ProgressSummaryRequest): ProgressSummaryResponse

    companion object {
        private const val BASE_URL = "https://ais-dev-56uu5adsvzymzldmttmm2q-566877315859.us-east1.run.app/"

        fun create(): MathIaApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MathIaApiService::class.java)
        }
    }
}
