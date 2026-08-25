package com.orion.atena.data.api

import com.orion.atena.data.model.QuantumBenchmarkResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface OrionApiService {

    @GET("health")
    suspend fun healthCheck(): Map<String, Any>

    @POST("quantum/benchmark")
    suspend fun runQuantumBenchmark(
        @Query("num_qubits") numQubits: Int = 5,
        @Query("shots") shots: Int = 1000,
        @Query("use_real_hardware") useRealHardware: Boolean = false,
        @Query("ibm_token") ibmToken: String? = null
    ): QuantumBenchmarkResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://projeto-orion-487033451365.us-central1.run.app/"

    val apiService: OrionApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OrionApiService::class.java)
    }
}
