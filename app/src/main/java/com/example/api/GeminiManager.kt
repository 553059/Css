package com.example.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiManager {
    private const val TAG = "GeminiManager"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun getStrategicAdvice(gameContext: String, statsSnippet: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "API Key is empty or placeholder configuration.")
            return@withContext "Error: No se ha configurado la API Key de Gemini en el panel de secretos. Por favor introduce una clave válida para activar el Pensador de IA en tiempo real."
        }

        try {
            // Build the JSON payload structurally using org.json
            val root = JSONObject()
            
            // System instruction
            val systemInstruction = JSONObject()
            val systemParts = JSONArray().put(JSONObject().put("text", 
                "Eres el 'Pensador Automático', un analizador táctico y estadístico de alta precisión integrado en Casino Analyzer. " +
                "Recibirás estadísticas de apuestas actuales del usuario (ej. manos de cartas, rachas, giros de ruleta, rollover de bonos). " +
                "Debes generar consejos sumamente analíticos, rápidos y estratégicos en español de manera directa sin rodeos. " +
                "Incluye: 1. Un resumen probabilístico. 2. Una recomendación de acción inmediata (ej. doblar, mantener, cambiar de mesa, pausar). " +
                "3. Advertencia de riesgo. Sé conciso y profesional, sin adornos excesivos."
            ))
            systemInstruction.put("parts", systemParts)
            root.put("systemInstruction", systemInstruction)

            // Content item
            val contentObj = JSONObject()
            val userText = "Datos de Sesión Actual:\n$gameContext\n\nEstadísticas Métricas:\n$statsSnippet\n\nGenera de inmediato el análisis y recomendación táctica:"
            val partsArr = JSONArray().put(JSONObject().put("text", userText))
            contentObj.put("parts", partsArr)
            
            val contentsArr = JSONArray().put(contentObj)
            root.put("contents", contentsArr)

            // Optional settings
            val config = JSONObject()
            config.put("temperature", 0.4) // Slightly lower temp for more statistical precision
            root.put("generationConfig", config)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = root.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val code = response.code
                    val errorBody = response.body?.string() ?: "Empty body"
                    Log.e(TAG, "API call failed with code $code: $errorBody")
                    return@withContext "Error del servidor Gemini (Código $code). Asegúrate de que la API Key es válida e intenta de nuevo."
                }

                val bodyString = response.body?.string() ?: return@withContext "Error: Respuesta vacía de la API"
                val responseJson = JSONObject(bodyString)
                
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "No se recibió texto de respuesta.")
                        }
                    }
                }
                "No se pudo procesar la respuesta táctica de la IA. Por favor, intente de nuevo."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during advice generation", e)
            "Error de Conectividad: ${e.localizedMessage ?: "No se pudo contactar a la inteligencia artificial."}"
        }
    }
}
