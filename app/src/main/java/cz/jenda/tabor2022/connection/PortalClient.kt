package cz.jenda.tabor2022.connection

import `in`.abilng.ndjson.NdJsonObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import cz.jenda.tabor2022.BuildConfig
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.data.JsonStatus
import cz.jenda.tabor2022.data.JsonTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.*
import kotlin.streams.asSequence

interface PortalClient {
    companion object PortalClient {
        private val okhttpBuilder by lazy {
            val builder = OkHttpClient.Builder()
            if (BuildConfig.DEBUG) {
                builder.addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.NONE
                })
            }
            builder.connectTimeout(Constants.PortalConnectionTimeout)
            builder
        }

        fun create(uri: String): cz.jenda.tabor2022.connection.PortalClient {
            val clientRaw = Retrofit.Builder()
                .baseUrl(uri)
                .client(okhttpBuilder.build())
                .addConverterFactory(JacksonConverterFactory.create())
                .build()
                .create(PortalClientRaw::class.java)

            return object : cz.jenda.tabor2022.connection.PortalClient {
                override suspend fun getStatus(): JsonStatus {
                    return clientRaw.getStatus()
                }

                override suspend fun fetchData(): Flow<JsonTransaction> {
                    val s = clientRaw.fetchData().byteStream()

                    return ndJsonObjectMapper.readValue(s, JsonTransaction::class.java)
                        .asSequence()
                        .asFlow()
                }

                override suspend fun deleteData() {
                    clientRaw.deleteData()
                }

                override suspend fun updateTime(unixSecs: Long) {
                    clientRaw.updateTime(unixSecs)
                }

                override suspend fun updateNamesMapping(rawBytes: ByteArray) {
                    val part = MultipartBody.Part.createFormData(
                        "file", "names.json", rawBytes.toRequestBody(
                            "image/*".toMediaTypeOrNull(),
                            0, rawBytes.size
                        )
                    )

                    clientRaw.updateNamesMapping(part)
                }
            }
        }

        private val kotlinModule = KotlinModule.Builder()
            .configure(KotlinFeature.NullIsSameAsDefault, true)
            .configure(KotlinFeature.StrictNullChecks, true)
            .build()

        private val ndJsonObjectMapper: NdJsonObjectMapper by lazy {
            val ndJsonObjectMapper = NdJsonObjectMapper()
            ndJsonObjectMapper.registerModule(kotlinModule)
            ndJsonObjectMapper.registerModule(JavaTimeModule())

            ndJsonObjectMapper
        }
    }

    suspend fun getStatus(): JsonStatus
    suspend fun fetchData(): Flow<JsonTransaction>
    suspend fun deleteData()
    suspend fun updateTime(unixSecs: Long)
    suspend fun updateNamesMapping(rawBytes: ByteArray)
}

private interface PortalClientRaw {
    @GET("/status")
    suspend fun getStatus(): JsonStatus

    @GET("/transactions")
    suspend fun fetchData(): ResponseBody

    @DELETE("/transactions")
    suspend fun deleteData()

    @PUT("/time")
    suspend fun updateTime(@Query("secs") unixSecs: Long)

    @Multipart
    @POST("/resources/names")
    suspend fun updateNamesMapping(@Part payload: MultipartBody.Part)
}