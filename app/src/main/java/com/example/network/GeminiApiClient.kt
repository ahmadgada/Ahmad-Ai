package com.example.network

import com.example.BuildConfig
import com.example.model.AiLanguage
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "role") val role: String? = null,
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "temperature") val temperature: Float = 0.7f,
    @Json(name = "topP") val topP: Float = 0.95f,
    @Json(name = "topK") val topK: Int = 40
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

interface GeminiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE // Privacy first - do not log sensitive payloads
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiService = retrofit.create(GeminiService::class.java)

    fun getSystemPrompt(language: AiLanguage): String {
        val langInstruction = when (language) {
            AiLanguage.ENGLISH -> "Respond strictly in clear English."
            AiLanguage.HINDI -> "Respond in clear, natural Hindi (Devanagari script)."
            AiLanguage.HINGLISH -> "Respond in natural conversational Hinglish (Roman Hindi mixed with English terms)."
            AiLanguage.AUTO -> "Detect the user's language automatically (English, Hindi, or Hinglish) and match their language naturally without forcing translations."
        }

        return """
            You are Cyber Guard Security Intelligence, an elite personal cybersecurity and cyber-defense assistant integrated into the Cyber Guard application.
            
            Your core mission:
            - Provide clear, expert, practical, and beginner-friendly cybersecurity education.
            - Help users protect their accounts, prevent phishing, detect scams, create secure passwords, enable 2FA/MFA, and safeguard their digital identity.
            - Explain technical concepts in simple terms (e.g., encryption, breach response, phishing, social engineering).
            - Strictly promote defensive security practices. NEVER provide advice, code, or techniques for hacking, credential theft, unauthorized penetration, or evasion.
            - $langInstruction
            - Keep answers structured with clear headings, bullet points, and actionable takeaways when appropriate.
            - Maintain a calm, professional, reassuring, and privacy-first tone.
        """.trimIndent()
    }
}
