package com.tacticaldev.trazabilidad.datos.remoto

import com.tacticaldev.trazabilidad.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object ClienteRetrofit {

    private val okHttp: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            // Muestra en Logcat (filtro "OkHttp") cada petición y respuesta: muy útil para depurar.
            builder.addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        }
        builder.build()
    }

    private fun crear(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)                                   // debe terminar en "/"
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create()) // JSON <-> clases Kotlin
        .build()

    val apiUsuarios: ApiUsuarios by lazy { crear(BuildConfig.URL_USUARIOS).create(ApiUsuarios::class.java) }
    val apiVisitas: ApiVisitas by lazy { crear(BuildConfig.URL_VISITAS).create(ApiVisitas::class.java) }

    const val URL_CLIMA = "https://api.open-meteo.com/"
    val apiClima: ApiClima by lazy { crear(URL_CLIMA).create(ApiClima::class.java) }
}
